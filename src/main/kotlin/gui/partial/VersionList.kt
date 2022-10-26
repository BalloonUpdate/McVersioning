package gui.partial

import utils.File2
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

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

    val list = JList<String>()
    val model = DefaultListModel<String>()

    init {
        layout = BorderLayout()
        add(JScrollPane(list), BorderLayout.CENTER)
        val tipPanel = JPanel()
        tipPanel.border = CompoundBorder(LineBorder(Color.DARK_GRAY), EmptyBorder(2,0,2,0))
        tipPanel.add(JLabel("Tips: 双击现有的版本号可以快速填入输入框"))
        add(tipPanel, BorderLayout.SOUTH)

        list.model = model

        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2)
                    onChoosingSomeVersion?.invoke(list.selectedValue)
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

        model.clear()
        for (version in versions.reversed())
            model.addElement(version)
    }

    /**
     * 过滤版本号列表的显示
     * @param pendingVersion 过滤字符串。如果为null则重置过滤器
     */
    fun setPendingNewVersion(pendingVersion: String?)
    {
        model.clear()

        val vs = if (pendingVersion != null)
            versions.filter { it.startsWith(pendingVersion) }
        else
            versions

        for (version in vs.reversed())
            model.addElement(version)
    }

    /**
     * 检查版本号是否冲突
     */
    fun isVersionDuplicated(version: String): Boolean
    {
        return version in versions
    }
}