package gui.extended.checkboxtree

import javax.swing.tree.DefaultMutableTreeNode

open class FileModificationNode(
    val name: String,
    val rawPathDescription: String,
    var isNew: Boolean,
    val isFile: Boolean,
) : DefaultMutableTreeNode(name, !isFile)