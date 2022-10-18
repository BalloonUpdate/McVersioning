package gui.extended.checkboxtree

import javax.swing.tree.DefaultMutableTreeNode

open class CheckBoxTreeNode @JvmOverloads constructor(
    userObject: Any? = null,
    allowsChildren: Boolean = true,
    protected var isSelected: Boolean = false
) : DefaultMutableTreeNode(userObject, allowsChildren) {
    override fun getChildAt(index: Int): CheckBoxTreeNode {
        if (children == null) {
            throw ArrayIndexOutOfBoundsException("node has no children")
        }
        return children.elementAt(index) as CheckBoxTreeNode
    }

    @JvmName("isSelected1")
    fun isSelected(): Boolean {
        return isSelected
    }

    @JvmName("setSelected1")
    fun setSelected(_isSelected: Boolean) {
        isSelected = _isSelected
        if (_isSelected) {
            // 如果选中，则将其所有的子结点都选中
            if (children != null) {
                for (obj in children) {
                    val node = obj as CheckBoxTreeNode
                    if (!node.isSelected()) node.setSelected(true)
                }
            }
            // 向上检查，如果父结点的所有子结点都被选中，那么将父结点也选中
            val pNode = parent as CheckBoxTreeNode
            // 开始检查pNode的所有子节点是否都被选中
            var index = 0
            while (index < pNode.children.size) {
                val pChildNode = pNode.children[index] as CheckBoxTreeNode
                if (!pChildNode.isSelected()) break
                index++
            }
            /*
         * 表明pNode所有子结点都已经选中，则选中父结点，
         * 该方法是一个递归方法，因此在此不需要进行迭代，因为
         * 当选中父结点后，父结点本身会向上检查的。
         */if (index == pNode.children.size) {
            if (!pNode.isSelected()) pNode.setSelected(true)
        }
        } else {
            /*
             * 如果是取消父结点导致子结点取消，那么此时所有的子结点都应该是选择上的；
             * 否则就是子结点取消导致父结点取消，然后父结点取消导致需要取消子结点，但
             * 是这时候是不需要取消子结点的。
             */
            if (children != null) {
                var index = 0
                while (index < children.size) {
                    val childNode = children[index] as CheckBoxTreeNode
                    if (!childNode.isSelected()) break
                    ++index
                }
                // 从上向下取消的时候
                if (index == children.size) {
                    for (child in children) {
                        val node = child as CheckBoxTreeNode
                        if (node.isSelected()) node.setSelected(false)
                    }
                }
            }

            // 向上取消，只要存在一个子节点不是选上的，那么父节点就不应该被选上。
            val pNode = parent as CheckBoxTreeNode
            if (pNode.isSelected()) pNode.setSelected(false)
        }
    }
}