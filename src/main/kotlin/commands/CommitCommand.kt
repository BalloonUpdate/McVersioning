package commands

import VersionRecord
import diff.DirectoryDiff
import diff.RealFile
import diff.VirtualFile
import org.json.JSONArray
import utils.File2
import utils.HashUtils
import utils.MiscUtils
import javax.naming.InvalidNameException
import javax.naming.OperationNotSupportedException

class CommitCommand(
    private val clientDir: File2,
    private val versionsDir: File2,
    private val snapshotDir: File2,
    private val snapshotFile: File2,
    private val versionsFile: File2,
    private val newestVersionFile: File2,
) {
    fun commit(versionName: String)
    {
        val versionRecordFile = versionsDir + "v-$versionName.json"
        val versionsFileContent = versionsFile.content
        val versions = versionsFileContent.split("\n").filter { it.isNotEmpty() }

        if (versionName in versions || versionRecordFile.exists)
            throw InvalidNameException("The version $versionName is existing, it not allowed to create a version with the same label.");

        val real = RealFile.CreateFromRealFile(clientDir)
        val virual = VirtualFile.WrapWithFolder("no_name", VirtualFile.FromJsonFile(snapshotFile) ?: listOf())

        val diff = DirectoryDiff()
        val hasDiff = diff.compare(existing = virual.files, contrast = real.files)

        if (!hasDiff)
            throw OperationNotSupportedException("No any changes can be recorded.")

        // 更新缓存
        for (f in diff.oldFiles)
        {
            virual.removeFile(f)
        }

        for (f in diff.oldFolders)
        {
            virual.removeFile(f)
        }

        for (f in diff.newFolders)
        {
            val parent = MiscUtils.getDirPathPart(f)
            val filename = MiscUtils.getFileNamePart(f)

            val dir = if (parent != null) virual[parent]!! else virual
            dir.files += VirtualFile(filename, mutableListOf(), dir)
        }

        for (f in diff.newFiles)
        {
            val parent = MiscUtils.getDirPathPart(f)
            val filename = MiscUtils.getFileNamePart(f)

            val dir = if (parent != null) virual[parent]!! else virual
            val file = clientDir + f
            val length = file.length
            val modified = file.modified
            val hash = HashUtils.crc32(file.file)

            dir.files += VirtualFile(filename, length, hash, modified, dir)
        }

        // 更新快照文件
        val json = JSONArray()

        for (vf in virual.files)
            json.put(vf.toJsonObject())

        snapshotFile.content = json.toString(4)

        // 创建版本记录文件
        val versionRecord = VersionRecord()
        versionRecord.oldFiles.addAll(diff.oldFiles)
        versionRecord.newFiles.addAll(diff.newFiles)
        versionRecord.oldFolders.addAll(diff.oldFolders)
        versionRecord.newFolders.addAll(diff.newFolders)

        versionRecordFile.touch(versionRecord.serializeToJson().toString(4))

        // 更新版本列表文件
        versionsFile.content += "$versionName\n"

        // 更新最新版本文件
        newestVersionFile.content = versionName

        // 同步快照目录
        syncSnapshot()
    }

    private fun syncSnapshot()
    {
        val diff = DirectoryDiff()
        val e = RealFile.CreateFromRealFile(snapshotDir)
        val c = RealFile.CreateFromRealFile(clientDir)
        val hasDiff = diff.compare(existing = e.files, contrast = c.files)

        for (f in diff.oldFiles)
            (snapshotDir + f).delete()

        for (f in diff.oldFolders)
            (snapshotDir + f).delete()

        for (f in diff.newFolders)
            (snapshotDir + f).mkdirs()

        for (f in diff.newFiles)
            (clientDir + f).copy(snapshotDir + f)
    }
}