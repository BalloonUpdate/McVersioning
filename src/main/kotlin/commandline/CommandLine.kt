package commandline

import data.VersionRecord
import diff.DirectoryDiff
import diff.RealFile
import diff.VirtualFile
import org.json.JSONArray
import utils.EnvironmentUtils
import utils.File2
import utils.HashUtils
import utils.PathUtils
import javax.naming.InvalidNameException
import javax.naming.OperationNotSupportedException
import kotlin.system.exitProcess

class CommandLine
{
    val workdir = if (EnvironmentUtils.isPackaged) File2(System.getProperty("user.dir")) else File2("testdir")
    val clientDir = workdir + "client"
    val publicDir = workdir + "public"
    val snapshotDir = workdir + "public/snapshot"
    val snapshotFile = workdir + "public/snapshot.json"
    val versionsFile = workdir + "public/all-versions.txt"
    val newestVersionFile = workdir + "public/current-version.txt"

    fun main(args: Array<String>)
    {
        clientDir.mkdirs()
        publicDir.mkdirs()
        snapshotDir.mkdirs()
        versionsFile.touch()

        val currentVersion = if (newestVersionFile.exists) newestVersionFile.content else "无"

        val arg0 = args.firstOrNull()
        val arg1 = if (args.size > 1) args[1] else null
        when(arg0)
        {
            "record" -> {
                if (arg1 == null)
                {
                    println("请输入一个版本号（版本号不需要以v开头），当前版本号: $currentVersion")
                    exitProcess(1)
                }

                try {
                    record(arg1)
                } catch (e: InvalidNameException) {
                    println("版本号 $arg1 已经存在，不能重复创建")
                    exitProcess(1)
                }

                println("版本 $arg1 创建成功")
            }

            "revert" -> {
                if (!newestVersionFile.exists)
                {
                    println("现在还没有任何版本，因此无法使用revert命令")
                    exitProcess(1)
                }

                println("当前版本号: $currentVersion")

                revert()
            }

            "status" -> {
                println("当前版本号: $currentVersion")

                status()
            }

            else -> {
                println("请输入正确的参数：[record, revert, status]")
                println("""使用方法:
                    |    record                 -   查看当前版本号
                    |    record <new-version>   -   创建一个新的版本
                    |    status                 -   列出对client所做的一切更改
                    |    revert                 -   还原对client所做的一切更改
                """.trimMargin())

                exitProcess(1)
            }
        }
    }

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

        if (!hasDiff)
            println("注意，$versionName 是一个不包含任何更改的空版本")

        // 更新缓存
        for (f in diff.oldFiles)
        {
            println("旧文件: $f")
            virual.removeFile(f)
        }

        for (f in diff.oldFolders)
        {
            println("旧目录: $f")
            virual.removeFile(f)
        }

        for (f in diff.newFolders)
        {
            println("新目录: $f")
            val parent = PathUtils.getDirPathPart(f)
            val filename = PathUtils.getFileNamePart(f)

            val dir = if (parent != null) virual[parent]!! else virual
            dir.files += VirtualFile(filename, mutableListOf(), dir)
        }

        for (f in diff.newFiles)
        {
            println("新文件: $f")
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