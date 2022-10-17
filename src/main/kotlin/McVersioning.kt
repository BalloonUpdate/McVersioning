import commands.CommitCommand
import commands.RevertCommand
import commands.StatusCommand
import utils.EnvironmentUtils
import utils.File2
import javax.naming.InvalidNameException
import javax.naming.OperationNotSupportedException
import kotlin.system.exitProcess

object McVersioning
{
    @JvmStatic
    fun main(args: Array<String>)
    {
        val workdir = if (EnvironmentUtils.isPackaged) EnvironmentUtils.jarFile.parent else File2("testdir")
        val clientDir = workdir + "client"
        val versionsDir = workdir + "public"
        val snapshotDir = workdir + "public/snapshot"
        val snapshotFile = workdir + "public/snapshot.json"
        val versionsFile = workdir + "public/all-versions.txt"
        val newestVersionFile = workdir + "public/current-version.txt"

        clientDir.mkdirs()
        versionsDir.mkdirs()
        snapshotDir.mkdirs()
        versionsFile.touch()

        val arg0 = args.firstOrNull()
        val arg1 = if (args.size > 1) args[1] else null
        when(arg0)
        {
            "commit" -> {
                if (arg1 == null)
                {
                    println("请输入一个版本号（版本号不需要以v开头）")
                    exitProcess(1)
                }

                val commit = CommitCommand(clientDir, versionsDir, snapshotDir, snapshotFile, versionsFile, newestVersionFile)

                try {
                    commit.commit(arg1)
                } catch (e: InvalidNameException) {
                    println("版本号 $arg1 已经存在，不能重复创建")
                    exitProcess(1)
                } catch (e: OperationNotSupportedException) {
                    println("没有任何文件修改，不需要创建新版本")
                    exitProcess(1)
                }

                println("版本 $arg1 创建成功")
            }

            "revert" -> {
                val revert = RevertCommand(clientDir, versionsDir, snapshotDir, snapshotFile, versionsFile, newestVersionFile)
                revert.revert()
            }

            "status" -> {
                val status = StatusCommand(clientDir, versionsDir, snapshotDir, snapshotFile, versionsFile, newestVersionFile)
                status.status()
            }

            else -> {
                println("请输入正确的参数：[commit, revert, status]")
                exitProcess(1)
            }
        }
    }
}