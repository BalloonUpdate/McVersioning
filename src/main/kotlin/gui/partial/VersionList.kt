package gui.partial

import utils.File2
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

/**
 * 负责显示版本号列表的UI控件
 */
class VersionList : JPanel()
{
    /**
     * 选择使用某个版本号补全时
     */
    var onChoosingSomeVersion: ((version: String) -> Unit)? = null

    /**
     * 所有的版本号列表
     */
    val versions = mutableListOf<String>()

    val versionList = JList<String>()
    val changelogs = JTextArea()

    val modelForVersionList = DefaultListModel<String>()

    init {
        layout = GridLayout(1, 2)
        add(JPanel(BorderLayout()).apply {
            add(JScrollPane(versionList), BorderLayout.CENTER)
            add(JLabel(" 历史版本列表"), BorderLayout.NORTH)
        })
        add(JPanel(BorderLayout()).apply {
            add(JScrollPane(changelogs), BorderLayout.CENTER)
            add(JLabel(" 版本更新日志"), BorderLayout.NORTH)
        })

        versionList.model = modelForVersionList

        versionList.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
//                if (e.clickCount == 1)
                    onChoosingSomeVersion?.invoke(versionList.selectedValue)
            }
        })
    }

    /**
     * 从文件重新加载版本号列表
     */
    fun refresh(versionsFile: File2)
    {
        versions.clear()

        versions.addAll(if (versionsFile.exists)
            versionsFile.content.trim().split("\n").filter { it.isNotEmpty() }
        else
            listOf())

        modelForVersionList.clear()
        for (version in versions.reversed())
            modelForVersionList.addElement(version)
    }

    /**
     * 过滤版本号列表的显示
     * @param pendingVersion 过滤字符串。如果为null则重置过滤器
     */
    fun setPendingNewVersion(pendingVersion: String?)
    {
        modelForVersionList.clear()

        val vs = if (pendingVersion != null)
            versions.filter { it.startsWith(pendingVersion) }
        else
            versions

        for (version in vs.reversed())
            modelForVersionList.addElement(version)
    }

    /**
     * 检查版本号是否冲突
     */
    fun isVersionDuplicated(version: String): Boolean
    {
        return version in versions
    }
}