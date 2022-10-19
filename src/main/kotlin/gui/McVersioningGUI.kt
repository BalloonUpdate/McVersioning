package gui

import commands.RecordCommand
import diff.DirectoryDiff
import diff.RealFile
import diff.VirtualFile
import gui.extended.SvgIcons
import gui.extended.SwingThemeLoader
import gui.partial.ActionBar
import gui.partial.DifferenceTree
import gui.partial.VersionList
import utils.EnvironmentUtils
import utils.File2
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel

/**
 * 图形界面主窗口类
 */
object McVersioningGUI
{
    val workdir = if (EnvironmentUtils.isPackaged) File2(System.getProperty("user.dir")) else File2("testdir")
    val clientDir = workdir + "client"
    val publicDir = workdir + "public"
    val snapshotDir = workdir + "public/snapshot"
    val snapshotFile = workdir + "public/snapshot.json"
    val versionsFile = workdir + "public/all-versions.txt"
    val newestVersionFile = workdir + "public/current-version.txt"

    init {
        SvgIcons.loadIcon()
        SwingThemeLoader.init()

        clientDir.mkdirs()
        publicDir.mkdirs()
        snapshotDir.mkdirs()
        versionsFile.touch()
    }

    val frame = JFrame("McVersioning GUI")
    val mainPanel = JPanel()
    val actionBar = ActionBar(clientDir, snapshotDir)
    val diffTree = DifferenceTree(clientDir)
    val versionList = VersionList()

    fun run() {
        frame.add(mainPanel)

        frame.setSize(700, 550)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.isVisible = true

        mainPanel.layout = BorderLayout()
        mainPanel.add(actionBar, BorderLayout.NORTH)
        mainPanel.add(diffTree, BorderLayout.CENTER)

        actionBar.onRefreshDiff =  { onFinish ->
            Thread {
                diffTree.clearDiff()
                diffTree.refreshFromDiff(calculateDiff().second)
                onFinish()
            }.start()
        }

        actionBar.onReadyToCreatingNewVersion = {
            mainPanel.remove(diffTree)
            mainPanel.add(versionList, BorderLayout.CENTER)
            versionList.refresh(versionsFile)
            mainPanel.updateUI()
        }

        actionBar.onCreatingNewVersion = { ver, onfinish ->
            Thread {
                versionList.refresh(versionsFile)
                if (versionList.isVersionDuplicated(ver))
                {
                    JOptionPane.showMessageDialog(null,
                        "版本冲突，请选择一个未被使用的版本号", "",
                        JOptionPane.ERROR_MESSAGE)

                    onfinish()
                    return@Thread
                }

                if (!calculateDiff().first)
                {
                    val choice = JOptionPane.showConfirmDialog(null,
                        "当前未检查到任何文件修改，是否执意创建一个空内容的版本号", "",
                        JOptionPane.YES_NO_OPTION) == 0

                    if (!choice)
                    {
                        onfinish()
                        return@Thread
                    }
                }

                createNewVersion(ver)
                actionBar.setInputFieldContent("")
                versionList.refresh(versionsFile)

                JOptionPane.showMessageDialog(null, "版本 $ver 已创建", "", JOptionPane.INFORMATION_MESSAGE)
                onfinish()
            }.start()
        }

        actionBar.onCancelingNewVersion = {
            mainPanel.remove(versionList)
            mainPanel.add(diffTree, BorderLayout.CENTER)
            mainPanel.updateUI()
        }

        actionBar.onTypingNewVersion = {
            versionList.setPendingNewVersion(it)
        }

        versionList.onChoosingSomeVersion = {
            actionBar.setInputFieldContent(it)
        }

        actionBar.doRefreshDiff()
    }

    fun calculateDiff(): Pair<Boolean, DirectoryDiff>
    {
        val c = RealFile.CreateFromRealFile(clientDir)
        val e = VirtualFile.WrapWithFolder("no_name", VirtualFile.FromJsonFile(snapshotFile) ?: listOf())
        val diff = DirectoryDiff()
        val hasDiff = diff.compare(existing = e.files, contrast = c.files)
        return Pair(hasDiff, diff)
    }

    fun createNewVersion(version: String)
    {
        val commit = RecordCommand(clientDir, publicDir, snapshotDir, snapshotFile, versionsFile, newestVersionFile)
        commit.record(version)
    }
}