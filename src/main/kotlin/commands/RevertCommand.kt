package commands

import diff.DirectoryDiff
import diff.RealFile
import utils.File2

class RevertCommand(
    private val clientDir: File2,
    private val versionsDir: File2,
    private val snapshotDir: File2,
    private val snapshotFile: File2,
    private val versionsFile: File2,
    private val newestVersionFile: File2,
) {
    fun revert()
    {
        val diff = DirectoryDiff()
        val e = RealFile.CreateFromRealFile(clientDir)
        val c = RealFile.CreateFromRealFile(snapshotDir)
        val hasDiff = diff.compare(existing = e.files, contrast = c.files)

        if (hasDiff)
        {
            for (f in diff.oldFiles)
            {
                (clientDir + f).delete()
                println("已还原多余文件: $f")
            }

            for (f in diff.oldFolders)
            {
                (clientDir + f).delete()
                println("已还原多余目录: $f")
            }

            for (f in diff.newFiles)
            {
                println("已还原缺失文件: $f")
                (snapshotDir + f).copy(clientDir + f)
            }

            for (f in diff.newFolders)
            {
                println("已还原缺失目录: $f")
                (clientDir + f).mkdirs()
            }
        } else {
            println("client目录没有任何改动")
        }
    }
}