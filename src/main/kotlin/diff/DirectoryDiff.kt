package diff

/**
 * 目录差异对比
 */
class DirectoryDiff
{
    val oldFolders: MutableList<String> = mutableListOf()
    val oldFiles: MutableList<String> = mutableListOf()
    val newFolders: MutableList<String> = mutableListOf()
    val newFiles: MutableList<String> = mutableListOf()

    /**
     * 对比文件差异
     * @param existing 被对比的目录下的所有文件
     * @param contrast 参照目录下的所有文件
     * @return 有无差异
     */
    fun compare(existing: List<ComparableFile>, contrast: List<ComparableFile>): Boolean
    {
        findNews(existing, contrast)
        findOlds(existing, contrast)

        return oldFolders.size + oldFiles.size + newFolders.size + newFiles.size > 0
    }

    /** 扫描需要下载的文件(不包括被删除的)
     * @param existing 要拿来进行对比的目录下的所有文件
     * @param contrast 要拿来进行对比的目录下的所有文件
     */
    private fun findNews(existing: List<ComparableFile>, contrast: List<ComparableFile>) {
        for (c in contrast)
        {
            val corresponding = existing.firstOrNull { it.name == c.name } // 此文件可能不存在

            if(corresponding == null) // 如果文件不存在的话，就不用校验了，可以直接进行下载
            {
                markAsNew(c)
                continue
            }

            if(c.isFile)
            {
                if(corresponding.isFile)
                {
                    if (!compareSingleFile(corresponding, c))
                    {
                        markAsOld(corresponding)
                        markAsNew(c)
                    }
                } else {
                    markAsOld(corresponding)
                    markAsNew(c)
                }
            } else  {
                if(corresponding.isFile)
                {
                    markAsOld(corresponding)
                    markAsNew(c)
                } else {
                    findNews(corresponding.files, c.files)
                }
            }
        }
    }

    /** 扫描需要删除的文件
     * @param existing 要拿来进行对比的目录下的所有文件
     * @param contrast 要拿来进行对比的目录下的所有文件
     */
    private fun findOlds(existing: List<ComparableFile>, contrast: List<ComparableFile>)
    {
        for (f in existing)
        {
            val corresponding = contrast.firstOrNull { it.name == f.name }

            if(corresponding != null)
            {
                if(!f.isFile && !corresponding.isFile)
                    findOlds(f.files, corresponding.files)
            } else {
                markAsOld(f)
            }
        }
    }

    /**
     * 对比两个路径相同的文件是否一致
     * @param a 参与对比的a文件
     * @param b 参与对比的b文件
     */
    private fun compareSingleFile(a: ComparableFile, b: ComparableFile): Boolean
    {
        return a.modified == b.modified || b.hash == a.hash
    }

    /**
     * 将一个文件文件或者目录标记为旧文件
     * @param existing 被标记的文件或者目录
     */
    private fun markAsOld(existing: ComparableFile)
    {
        if(!existing.isFile)
        {
            for (f in existing.files)
            {
                if(f.isFile)
                    oldFiles += f.relativePath
                else
                    markAsOld(f)
            }

            oldFolders += existing.relativePath
        } else {
            oldFiles += existing.relativePath
        }
    }

    /**
     * 将一个文件文件或者目录标记为新文件
     * @param constrast 被标记的文件或者目录
     */
    private fun markAsNew(constrast: ComparableFile)
    {
        if (constrast.isFile)
        {
            newFiles += constrast.relativePath
        } else {
            newFolders += constrast.relativePath
            for (n in constrast.files)
                markAsNew(n)
        }
    }
}