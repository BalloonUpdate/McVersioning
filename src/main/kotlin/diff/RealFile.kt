package diff

import utils.File2
import utils.HashUtils

/**
 * 真实文件对象
 */
class RealFile(val file: File2, parent: RealFile?, childrenLinkThis: Boolean) : ComparableFile()
{
    override val name: String by lazy { file.name }
    override val length: Long by lazy { file.length }
    override val hash: String by lazy { HashUtils.crc32(file.file) }
    override val modified: Long by lazy { file.modified }
    override val files: List<RealFile> by lazy { file.files.map { RealFile(it, if (childrenLinkThis) this else null, true) } }
    override val isFile: Boolean by lazy { file.isFile }
    override val relativePath: String = (if (parent != null) parent.relativePath + "/" else "") + name

    override fun get(path: String): RealFile? = getFileInternal(path) as RealFile?

    companion object {
        /**
         * 从磁盘文件对象创建
         */
        @JvmStatic
        fun CreateFromRealFile(file: File2): RealFile
        {
            return RealFile(file, null, false)
        }
    }
}