import org.json.JSONArray
import org.json.JSONObject

class VersionRecord
{
    val oldFiles: MutableSet<String> = mutableSetOf()
    val newFiles: MutableSet<String> = mutableSetOf()
    val oldFolders: MutableSet<String> = mutableSetOf()
    val newFolders: MutableSet<String> = mutableSetOf()
    val newFilesLengthes: MutableMap<String, Long> = mutableMapOf()

    fun serializeToJson(): JSONObject
    {
        val json = JSONObject()

        json.put("old_files", JSONArray(oldFiles))
        json.put("new_files", JSONArray(newFiles))
        json.put("old_folders", JSONArray(oldFolders))
        json.put("new_folders", JSONArray(newFolders))
        json.put("new_files_lengthes", JSONObject(newFilesLengthes))

        return json
    }
}