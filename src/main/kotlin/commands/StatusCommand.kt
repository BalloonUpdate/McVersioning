package commands

import diff.DirectoryDiff
import diff.RealFile
import diff.VirtualFile
import utils.File2

/**
 * status命令用来列出当client下已有哪些文件更改
 */
class StatusCommand(
    private val clientDir: File2,
    private val versionsDir: File2,
    private val snapshotDir: File2,
    private val snapshotFile: File2,
    private val versionsFile: File2,
    private val newestVersionFile: File2,
) {
    fun status()
    {
        val c = RealFile.CreateFromRealFile(clientDir)
        val e = VirtualFile.WrapWithFolder("no_name", VirtualFile.FromJsonFile(snapshotFile) ?: listOf())

        val diff = DirectoryDiff()
        val hasDiff = diff.compare(existing = e.files, contrast = c.files)

        if (hasDiff)
        {
            for (f in diff.oldFiles)
                println("缺失的文件: $f")

            for (f in diff.oldFolders)
                println("缺失的目录: $f")

            for (f in diff.newFiles)
                println("新增的文件: $f")

            for (f in diff.newFolders)
                println("新增的目录: $f")
        } else {
            println("client目录没有任何改动")
        }
    }
}