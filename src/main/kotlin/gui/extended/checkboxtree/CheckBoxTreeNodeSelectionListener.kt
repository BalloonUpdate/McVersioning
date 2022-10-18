package gui.extended.checkboxtree

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTree
import javax.swing.tree.DefaultTreeModel

class CheckBoxTreeNodeSelectionListener : MouseAdapter() {
    override fun mouseClicked(event: MouseEvent) {
        val tree = event.source as JTree
        val x = event.x
        val y = event.y
        val row = tree.getRowForLocation(x, y)
        val path = tree.getPathForRow(row)
        if (path != null) {
            val node = path.lastPathComponent as CheckBoxTreeNode
            val isSelected = !node.isSelected()
            node.setSelected(isSelected)
            (tree.model as DefaultTreeModel).nodeStructureChanged(node)
        }
    }
}