package gui.extended.checkboxtree

import gui.extended.SvgIcons
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.UIManager
import javax.swing.plaf.ColorUIResource
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeNode

class CheckBoxTreeCellRenderer : JPanel(), TreeCellRenderer
{
    private var check: JCheckBox
    private var label: CheckBoxTreeLabel

    init {
        layout = null
        add(JCheckBox().also { check = it })
        add(CheckBoxTreeLabel().also { label = it })
        check.background = UIManager.getColor("Tree.textBackground")
        label.foreground = UIManager.getColor("Tree.textForeground")
    }

    /**
     * 返回的是一个`JPanel`对象，该对象中包含一个`JCheckBox`对象
     * 和一个`JLabel`对象。并且根据每个结点是否被选中来决定`JCheckBox`
     * 是否被选中。
     */
    override fun getTreeCellRendererComponent(
        tree: JTree, value: Any,
        selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int,
        hasFocus: Boolean
    ): Component
    {
        val stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus)
        isEnabled = tree.isEnabled
        check.isSelected = (value as CheckBoxTreeNode).isSelected()
        label.font = tree.font
        label.text = stringValue
        label.setSelected(selected)
        label.setFocus(hasFocus)
        if ((value as TreeNode).allowsChildren) {
            label.icon = SvgIcons.DIR_ICON
            //            label.setIcon(UIManager.getIcon("Tree.closedIcon"));
//        } else if (expanded) {
//            label.setIcon(UIManager.getIcon("Tree.openIcon"));
        } else {
            setFileIcon()
        }
        return this
    }

    override fun getPreferredSize(): Dimension
    {
        val dCheck = check.preferredSize
        val dLabel = label.preferredSize
        return Dimension(dCheck.width + dLabel.width, dCheck.height.coerceAtLeast(dLabel.height))
    }

    override fun doLayout()
    {
        val dCheck = check.preferredSize
        val dLabel = label.preferredSize
        var yCheck = 0
        var yLabel = 0
        if (dCheck.height < dLabel.height) yCheck = (dLabel.height - dCheck.height) / 2 else yLabel =
            (dCheck.height - dLabel.height) / 2
        check.setLocation(0, yCheck)
        check.setBounds(0, yCheck, dCheck.width, dCheck.height)
        label.setLocation(dCheck.width, yLabel)
        label.setBounds(dCheck.width, yLabel, dLabel.width, dLabel.height)
    }

    override fun setBackground(color: Color)
    {
        var color: Color? = color
        if (color is ColorUIResource) color = null
        super.setBackground(color)
    }

    fun setFileIcon()
    {
        val fileName = label.text

        val splited = fileName.split(".")
        val suffix = splited[splited.size - 1]
        val svgIcon = SvgIcons.svgIconMap[suffix + "_file_icon"]

        label.icon = svgIcon ?: SvgIcons.svgIconMap["file_icon"]
    }
}