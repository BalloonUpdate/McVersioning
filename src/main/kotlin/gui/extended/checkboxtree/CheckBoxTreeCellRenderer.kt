package gui.extended.checkboxtree

import gui.extended.SvgIcons
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.UIManager
import javax.swing.plaf.ColorUIResource
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeNode

class CheckBoxTreeCellRenderer : JPanel(), TreeCellRenderer
{
    private var label: CheckBoxTreeLabel

    init {
        layout = null
        add(CheckBoxTreeLabel().also { label = it })
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

        if (value is FileModificationNode)
            label.isNewFile = value.isNew
        else
            label.isNewFile = true

        return this
    }

    override fun getPreferredSize(): Dimension
    {
        val labelSize = label.preferredSize
        return Dimension(labelSize.width, labelSize.height)
    }

    override fun doLayout()
    {
        val dLabel = label.preferredSize
        label.setBounds(4, 4, dLabel.width, dLabel.height)
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