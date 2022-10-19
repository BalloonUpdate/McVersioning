package gui.partial

import diff.DirectoryDiff
import diff.RealFile
import gui.McVersioningGUI
import utils.File2
import java.awt.FlowLayout
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * 负责显示各种按钮的操作栏UI控件
 */
class ActionBar(private val clientDir: File2, private val snapshotDir: File2) : JPanel()
{
    /**
     * 刷新文件差异事件
     */
    var onRefreshDiff: ((onFinish: () -> Unit) -> Unit)? = null

    /**
     * 准备创建新版本事件
     */
    var onReadyToCreatingNewVersion: (() -> Unit)? = null

    /**
     * 取消创建新版本事件
     */
    var onCancelingNewVersion: (() -> Unit)? = null

    /**
     * 创建新版本事件
     */
    var onCreatingNewVersion: ((version: String, onFinish: () -> Unit) -> Unit)? = null

    /**
     * 当在新版本号输入框里打字事件
     */
    var onTypingNewVersion: ((pendingInput: String) -> Unit)? = null

    private val newVersionInput = JTextField("", 20)
    private val versionsButton = JButton("版本列表")
    private val createVersionsButton = JButton("创建版本")
    private val backButton = JButton("返回")
    private val refreshDiffButton = JButton("扫描更改")
    private val revertDiffButton = JButton("还原更改")

    init {
        layout = FlowLayout()

        add(newVersionInput)
        add(versionsButton)
        add(createVersionsButton)
        add(backButton)
        add(refreshDiffButton)
        add(revertDiffButton)

        setMode(ActionBarMode.Normal)

        versionsButton.addActionListener {
            setMode(ActionBarMode.CreatingNewVersion)
            onReadyToCreatingNewVersion?.invoke()
        }

        refreshDiffButton.addActionListener {
            if (onRefreshDiff != null)
            {
                refreshDiffButton.text = "正在扫描"
                onRefreshDiff!! {
                    refreshDiffButton.text = "扫描更改"
                }
            }
        }

        createVersionsButton.addActionListener {
            createVersionsButton.text = "正在创建版本..."

            onCreatingNewVersion?.invoke(newVersionInput.text) {
                createVersionsButton.text = "创建版本"

                doRefreshDiff()
            }
        }

        backButton.addActionListener {
            onCancelingNewVersion?.invoke()
            setMode(ActionBarMode.Normal)
        }

        newVersionInput.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {}
            override fun keyPressed(e: KeyEvent?) { }
            override fun keyReleased(e: KeyEvent) {
                onTypingNewVersion?.invoke(newVersionInput.text)
            }
        })

        revertDiffButton.addActionListener {
            val choice = JOptionPane.showConfirmDialog(null,
                "是否还原对client目录所做的所有更改?", "",
                JOptionPane.YES_NO_OPTION) == 0

            if (!choice)
                return@addActionListener

            val diff = DirectoryDiff()
            val e = RealFile.CreateFromRealFile(clientDir)
            val c = RealFile.CreateFromRealFile(snapshotDir)
            val hasDiff = diff.compare(existing = e.files, contrast = c.files)

            if (!hasDiff)
            {
                JOptionPane.showMessageDialog(null, "client目录没有任何改动，不需要还原", "", JOptionPane.INFORMATION_MESSAGE)
                return@addActionListener
            }

            for (f in diff.oldFiles)
                (clientDir + f).delete()

            for (f in diff.oldFolders)
                (clientDir + f).delete()

            for (f in diff.newFiles)
                (snapshotDir + f).copy(clientDir + f)

            for (f in diff.newFolders)
                (clientDir + f).mkdirs()

            val count = diff.oldFolders.size + diff.oldFiles.size + diff.newFolders.size + diff.newFiles.size
            JOptionPane.showMessageDialog(null, "已还原 $count 个文件", "", JOptionPane.INFORMATION_MESSAGE)

            doRefreshDiff()
        }
    }

    /**
     * 设置新版本号输入框里的文字
     */
    fun setInputFieldContent(text: String)
    {
        newVersionInput.text = text
    }

    /**
     * 刷新文件差异
     */
    fun doRefreshDiff()
    {
        refreshDiffButton.doClick()
    }

    /**
     * 设置ActionBar的显示模式
     */
    fun setMode(mode: ActionBarMode)
    {
        if (mode == ActionBarMode.Normal)
        {
            newVersionInput.isVisible = false
            versionsButton.isVisible = true
            createVersionsButton.isVisible = false
            backButton.isVisible = false
            refreshDiffButton.isVisible = true
            revertDiffButton.isVisible = true
        } else {
            newVersionInput.isVisible = true
            versionsButton.isVisible = false
            createVersionsButton.isVisible = true
            backButton.isVisible = true
            refreshDiffButton.isVisible = false
            revertDiffButton.isVisible = false
        }
    }

    /**
     * ActionBar的显示模式
     */
    enum class ActionBarMode
    {
        Normal, CreatingNewVersion
    }
}