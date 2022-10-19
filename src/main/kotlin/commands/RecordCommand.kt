package commands

import data.VersionRecord
import diff.DirectoryDiff
import diff.RealFile
import diff.VirtualFile
import org.json.JSONArray
import utils.File2
import utils.HashUtils
import utils.PathUtils
import javax.naming.InvalidNameException

class RecordCommand(
    private val clientDir: File2,
    private val publicDir: File2,
    private val snapshotDir: File2,
    private val snapshotFile: File2,
    private val versionsFile: File2,
    private val newestVersionFile: File2,
) {
    fun record(versionName: String)
    {
        val versionRecordFile = publicDir + "v-$versionName.json"
        val versionsFileContent = versionsFile.content
        val versions = versionsFileContent.split("\n").filter { it.isNotEmpty() }

        if (versionName in versions || versionRecordFile.exists)
            throw InvalidNameException("The version $versionName is existing, it not allowed to create a version with the same label.");

        val real = RealFile.CreateFromRealFile(clientDir)
        val virual = VirtualFile.WrapWithFolder("no_name", VirtualFile.FromJsonFile(snapshotFile) ?: listOf())

        val diff = DirectoryDiff()
        val hasDiff = diff.compare(existing = virual.files, contrast = real.files)

//        if (!hasDiff)
//            throw OperationNotSupportedException("No any changes can be recorded.")

        // 更新缓存
        for (f in diff.oldFiles)
        {
//            println("旧文件: $f")
            virual.removeFile(f)
        }

        for (f in diff.oldFolders)
        {
//            println("旧目录: $f")
            virual.removeFile(f)
        }

        for (f in diff.newFolders)
        {
//            println("新目录: $f")
            val parent = PathUtils.getDirPathPart(f)
            val filename = PathUtils.getFileNamePart(f)

            val dir = if (parent != null) virual[parent]!! else virual
            dir.files += VirtualFile(filename, mutableListOf(), dir)
        }

        for (f in diff.newFiles)
        {
//            println("新文件: $f")
            val parent = PathUtils.getDirPathPart(f)
            val filename = PathUtils.getFileNamePart(f)

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
        versionRecord.newFilesLengthes.putAll(diff.newFiles.associateWith { (clientDir + it).length })

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