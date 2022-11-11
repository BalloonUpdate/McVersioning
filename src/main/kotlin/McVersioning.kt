import data.VersionRecord
import diff.DirectoryDiff
import diff.RealFile
import diff.VirtualFile
import org.json.JSONArray
import utils.EnvironmentUtils
import utils.File2
import utils.HashUtils
import utils.PathUtils
import java.util.*

object McVersioning
{
    val workdir = if (EnvironmentUtils.isPackaged) File2(System.getProperty("user.dir")) else File2("testdir")
    val clientDir = workdir + "client"
    val publicDir = workdir + "public"
    val snapshotDir = workdir + "public/snapshot"
    val snapshotFile = workdir + "public/snapshot.json"
    val versionsFile = workdir + "public/all-versions.txt"
    val newestVersionFile = workdir + "public/current-version.txt"

    /**
     * 所有的版本号列表
     */
    val versions = mutableListOf<String>()

    /**
     * 从文件重新加载版本号列表
     */
    fun reloadVersionList()
    {
        versions.clear()
        versions.addAll(if (versionsFile.exists)
            versionsFile.content.trim().split("\n").filter { it.isNotEmpty() }
        else
            listOf())
    }

    /**
     * 从命令行读取一段文字输入并解析成数字
     */
    fun readInput(reg: String, typeDesc: String): String
    {
        System.`in`.skip(System.`in`.available().toLong())

        while (true)
        {
            val input = readlnOrNull() ?: continue

//            println("输入: <$input>")

            if (reg.isEmpty() || Regex(reg).matches(input))
                return input

            if (typeDesc.isNotEmpty())
                println("输入的 <$input> 不是 $typeDesc")
        }
    }

    /**
     * 从命令行读取任意输入
     */
    fun readAnyString(): String
    {
        return readInput(".+", "")
    }

    /**
     * 从命令行读取任意一行字符串
     */
    fun waitForEnterKey(): String
    {
        return readInput("", "")
    }

    /**
     * 同步client目录到public/snapshot
     */
    fun syncSnapshot()
    {
        val diff = DirectoryDiff()
        val e = RealFile.CreateFromRealFile(snapshotDir)
        val c = RealFile.CreateFromRealFile(clientDir)
        diff.compare(existing = e.files, contrast = c.files)

        for (f in diff.oldFiles)
            (snapshotDir + f).delete()

        for (f in diff.oldFolders)
            (snapshotDir + f).delete()

        for (f in diff.newFolders)
            (snapshotDir + f).mkdirs()

        for (f in diff.newFiles)
            (clientDir + f).copy(snapshotDir + f)
    }

    /**
     * 打印diff对象
     */
    fun printDiff(diff: DirectoryDiff)
    {
        for (f in diff.oldFolders)
            println("旧目录: $f")

        if (diff.oldFolders.isNotEmpty() && diff.newFolders.isNotEmpty())
            println()

        for (f in diff.newFolders)
            println("新目录: $f")

        if (diff.newFolders.isNotEmpty() && diff.oldFiles.any { it !in diff.newFiles })
            println()

        for (f in diff.oldFiles.filter { it !in diff.newFiles })
            println("旧文件: $f")

        if (diff.oldFiles.isNotEmpty() && diff.newFiles.isNotEmpty())
            println()

        for (f in diff.newFiles)
            println("新文件: $f")
    }

    /**
     * 读取一个Y或者N输入
     */
    fun readYesOrNot(default: Boolean): Boolean
    {
        val choice = readInput("[yYnN]?", "y 或者 n").lowercase(Locale.getDefault())
        if (choice.isEmpty())
            return default
        return choice == "y"
    }

    fun createVersion()
    {
        println("输入你要创建的版本号名称...")
        val newVersion = readAnyString().trim()
        val versionRecordFile = publicDir + "v-$newVersion.json"
        reloadVersionList()
        if (newVersion in versions || versionRecordFile.exists)
        {
            println("版本 $newVersion 已经存在，不能重复创建")
            return
        }
        println("新版本名称： $newVersion")
        println("请将版本 $newVersion 的更新记录粘贴到changelogs.txt文件里然后按Enter，如果没有更新记录请直接按Enter")
        val fromFile = workdir + "changelogs.txt"
//            fromFile.delete()
        fromFile.touch()
        waitForEnterKey()
        val changelogs = (if (fromFile.exists) fromFile.content.trim() else "").ifEmpty { "" }
        if (changelogs.isNotEmpty())
            println("更新记录已经检测到")
        println("正在检查文件修改，可能需要一点时间")
        val real = RealFile.CreateFromRealFile(clientDir)
        val virual = VirtualFile.WrapWithFolder("no_name", VirtualFile.FromJsonFile(snapshotFile) ?: listOf())
        val diff = DirectoryDiff()
        val hasDiff = diff.compare(existing = virual.files, contrast = real.files)
        if (hasDiff)
        {
            // 更新virual对象
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
                val parent = PathUtils.getDirPathPart(f)
                val filename = PathUtils.getFileNamePart(f)

                val dir = if (parent != null) virual[parent]!! else virual
                dir.files += VirtualFile(filename, mutableListOf(), dir)
            }
            for (f in diff.newFiles)
            {
                val parent = PathUtils.getDirPathPart(f)
                val filename = PathUtils.getFileNamePart(f)

                val dir = if (parent != null) virual[parent]!! else virual
                val file = clientDir + f
                val length = file.length
                val modified = file.modified
                val hash = HashUtils.crc32(file.file)

                dir.files += VirtualFile(filename, length, hash, modified, dir)
            }

            println("以下为文件修改列表")
            println("--------------------")
            printDiff(diff)
            println("--------------------")
            println("以上为文件修改列表")
        } else {
            println("注意，$newVersion 是一个不包含任何更改的空版本，执意要继续吗？ y/N")
            if (!readYesOrNot(false))
            {
                println("创建版本 $newVersion 过程中断")
                return
            }
        }

        println("正在创建版本 $newVersion 可能需要一点时间")

        // 写入快照文件
        val json = JSONArray().apply { for (vf in virual.files) put(vf.toJsonObject()) }
        snapshotFile.content = json.toString(4)

        // 创建版本记录文件
        val versionRecord = VersionRecord()
        if (changelogs.isNotEmpty())
            versionRecord.changeLogs.addAll(changelogs.split("\n"))
        versionRecord.oldFiles.addAll(diff.oldFiles)
        versionRecord.newFiles.addAll(diff.newFiles)
        versionRecord.oldFolders.addAll(diff.oldFolders)
        versionRecord.newFolders.addAll(diff.newFolders)
        versionRecord.newFilesLengthes.putAll(diff.newFiles.associateWith { (clientDir + it).length })
        versionRecordFile.content = versionRecord.serializeToJson().toString(4)

        versionsFile.content += "$newVersion\n"
        newestVersionFile.content = newVersion
        syncSnapshot()
        fromFile.delete()
        println("创建 $newVersion 完成")
    }

    fun listVersion()
    {
        reloadVersionList()
        println("当前有 ${versions.size} 个版本")
        println(versions.joinToString("\n"))
        println("当前有 ${versions.size} 个版本")
    }

    fun checkStatus()
    {
        println("正在计算文件修改，可能需要一点时间")
        val c = RealFile.CreateFromRealFile(clientDir)
        val e = VirtualFile.WrapWithFolder("no_name", VirtualFile.FromJsonFile(snapshotFile) ?: listOf())
        val diff = DirectoryDiff()
        val hasDiff = diff.compare(existing = e.files, contrast = c.files)

        if (hasDiff)
        {
            println("以下为文件修改列表")
            println("--------------------")
            printDiff(diff)
            println("--------------------")
            println("以上为文件修改列表")
        } else {
            println("client目录没有任何改动")
        }
    }

    fun revertStatus()
    {
        println("正在计算文件修改，可能需要一点时间")
        val diff = DirectoryDiff()
        val e = RealFile.CreateFromRealFile(clientDir)
        val c = RealFile.CreateFromRealFile(snapshotDir)
        val hasDiff = diff.compare(existing = e.files, contrast = c.files)

        if (hasDiff)
        {
            println("即将还原以下所有文件修改")
            println("--------------------")
            printDiff(diff)
            println("--------------------")
            println("即将还原以上所有文件修改")

            println("执意要继续吗？ y/N")
            if (!readYesOrNot(false))
            {
                println("还原文件修改过程中断")
                return
            }

            println("请再次确认，要继续吗？ y/N")
            if (!readYesOrNot(false))
            {
                println("还原文件修改过程中断")
                return
            }

            println("正在还原文件修改，可能需要一点时间")

            for (f in diff.oldFiles)
                (clientDir + f).delete()

            for (f in diff.oldFolders)
                (clientDir + f).delete()

            for (f in diff.newFiles)
                (snapshotDir + f).copy(clientDir + f)

            for (f in diff.newFolders)
                (clientDir + f).mkdirs()

            println("所有文件修改已还原")
        } else {
            println("client目录没有任何改动")
        }
    }

    fun mainMenuLoop()
    {
        while (true)
        {
            reloadVersionList()
            val newest3 = if (versions.isNotEmpty()) versions.takeLast(3).reversed() else listOf()

            println("当前位置：主菜单")
            println("1.创建新版本")
            println("2.查看所有版本号（最新三个版本为: $newest3）")
            println("3.检查文件修改状态")
            println("4.还原所有文件修改")
            println()
            println("输入序号来进行你想要的操作，按Ctrl + C退出...")

            when(readInput("\\d+", "").toInt())
            {
                1 -> createVersion()
                2 -> listVersion()
                3 -> checkStatus()
                4 -> revertStatus()
                else -> break
            }

            waitForEnterKey()
            println("========================================")
        }
    }

    @JvmStatic
    fun main(args: Array<String>)
    {
        clientDir.mkdirs()
        publicDir.mkdirs()
        snapshotDir.mkdirs()
        versionsFile.touch()

        println("McVersioningServer程序 ${EnvironmentUtils.version}")
        mainMenuLoop()
        println("结束运行")
    }
}