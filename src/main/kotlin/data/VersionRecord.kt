package data

import org.json.JSONArray
import org.json.JSONObject

/**
 * 代表一个版本文件所记录的文件差异信息
 */
class VersionRecord
{
    val oldFiles: MutableSet<String> = mutableSetOf()
    val newFiles: MutableSet<String> = mutableSetOf()
    val oldFolders: MutableSet<String> = mutableSetOf()
    val newFolders: MutableSet<String> = mutableSetOf()
    val newFilesLengthes: MutableMap<String, Long> = mutableMapOf()
    var changeLogs: MutableList<String> = mutableListOf()

    fun serializeToJson(): JSONObject
    {
        val json = JSONObject()

        json.put("old_files", JSONArray(oldFiles))
        json.put("new_files", JSONArray(newFiles))
        json.put("old_folders", JSONArray(oldFolders))
        json.put("new_folders", JSONArray(newFolders))
        json.put("new_files_lengthes", JSONObject(newFilesLengthes))
        json.put("change_logs", JSONArray(changeLogs))

        return json
    }
}