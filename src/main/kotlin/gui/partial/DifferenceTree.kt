package gui.partial

import diff.DirectoryDiff
import gui.extended.checkboxtree.CheckBoxTreeCellRenderer
import gui.extended.checkboxtree.FileModificationNode
import utils.File2
import utils.PathUtils
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

/**
 * 负责显示文件差异的UI控件
 */
class DifferenceTree(val clientDir: File2) : JPanel()
{
    var rootNode = DefaultMutableTreeNode()
    val treeview = JTree()

    /**
     * JTree的事件监听器
     */
    val mouseListenerForTreeview = object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            val tv = (e.component as JTree)
            val path = tv.getClosestPathForLocation(e.x, e.y)
            val node = path.lastPathComponent
            if (node is FileModificationNode && node.isNew)
            {
                if (e.button == MouseEvent.BUTTON1 && e.clickCount == 2)
                    locateFileInExplorer(node.rawPathDescription)
            }
        }
    }

    init {
        layout = BorderLayout()

        add(JScrollPane(treeview), BorderLayout.CENTER)
        val tipPanel = JPanel()
        tipPanel.border = CompoundBorder(LineBorder(Color.DARK_GRAY), EmptyBorder(2,0,2,0))
        tipPanel.add(JLabel("Tips: 双击新增的文件可在资源管理器里快速定位到对应目录"))
        add(tipPanel, BorderLayout.SOUTH)

        treeview.model = DefaultTreeModel(rootNode)
        treeview.cellRenderer = CheckBoxTreeCellRenderer()
        treeview.toggleClickCount = 1
        treeview.addMouseListener(mouseListenerForTreeview)
    }

    /**
     * 创建子节点
     * @param path 节点路径。文件以非/结尾，目录以/结尾
     * @param isNew 新文件还旧文件
     * @param isFile 文件还是目录
     */
    private fun makesureNode(path: String, isNew: Boolean, isFile: Boolean, root: DefaultMutableTreeNode)
    {
        fun makesureChild(name: String, path: String, isNew: Boolean, isFile: Boolean, node: DefaultMutableTreeNode): FileModificationNode
        {
            val n = node.children().toList()
                .firstOrNull { (it as DefaultMutableTreeNode).userObject == name } as FileModificationNode?

            return n ?: FileModificationNode(name, path, isNew, isFile)
                .also { node.add(it) }
        }

        val split = path.split("/").filter { it.isNotEmpty() }
        var currentPath = ""
        var current = root

        for ((index, frag) in split.withIndex())
        {
            val thisPath = (if (currentPath != "") "/" else "") + frag
            val _isFile = if (index != split.size - 1) false else isFile
            current = makesureChild(frag, currentPath + thisPath, isNew, _isFile, current)
            currentPath += thisPath
        }
    }

    /**
     * 清空Treeview
     */
    fun clearDiff()
    {
        rootNode = DefaultMutableTreeNode()
        treeview.model = DefaultTreeModel(rootNode)
        updateUI()
    }

    /**
     * 从DirectoryDiff来刷新视图
     */
    fun refreshFromDiff(diff: DirectoryDiff)
    {
        val diffs = diff.newFiles.size + diff.newFolders.size + diff.oldFiles.size + diff.oldFolders.size

        // 清空Treeview
        rootNode = DefaultMutableTreeNode(if (diffs > 0) "(${diffs}处文件修改)" else "没有文件修改")
        treeview.model = DefaultTreeModel(rootNode)

        /**
         * 检查是否有新文件或者目录存在于这个directory下
         */
        fun checkWithinNewFilesOrDirs(directory: String?): String?
        {
            if (directory == null)
                return null

            val nd = diff.newFolders.any { it.startsWith(directory) }
            val nf = diff.newFiles.any { "/" in it && PathUtils.getDirPathPart(it)!!.startsWith(directory) }
            return if (nf || nd) directory else null
        }

        for (f in diff.oldFolders)
        {
            checkWithinNewFilesOrDirs(PathUtils.getDirPathPart(f))
                .also { if (it != null) makesureNode(it, isNew = true, isFile = false, rootNode) }

            println("oldFolders: $f")
            makesureNode(f, isNew = false, isFile = false, rootNode)
        }

        for (f in diff.oldFiles)
        {
            checkWithinNewFilesOrDirs(PathUtils.getDirPathPart(f))
                .also { if (it != null) makesureNode(it, isNew = true, isFile = false, rootNode) }

            println("oldFiles: $f")
            makesureNode(f, isNew = false, isFile = true, rootNode)
        }

        for (f in diff.newFolders)
        {
            println("newFolders: $f")
            makesureNode(f, isNew = true, isFile = false, rootNode)
        }

        for (f in diff.newFiles)
        {

            println("newFiles: $f")
            makesureNode(f, isNew = true, isFile = true, rootNode)
        }

        // 展开所有节点
        fun expand(node: DefaultMutableTreeNode)
        {
            for (child in node.children().toList().map { it as FileModificationNode })
            {
                treeview.expandPath(TreePath(child.path))
                expand(child)
            }
        }

        treeview.expandPath(TreePath(rootNode.path))
        expand(rootNode)
        updateUI()
    }

    /**
     * 在资源管理器中定位一个文件
     */
    private fun locateFileInExplorer(path: String)
    {
        val absPath = (clientDir + path).platformPath
        println(path)
        Runtime.getRuntime().exec("explorer /select,\"$absPath\"")
    }
}