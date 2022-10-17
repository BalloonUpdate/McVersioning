import org.json.JSONArray
import org.json.JSONObject

class VersionRecord
{
    val oldFiles: MutableSet<String> = mutableSetOf()
    val newFiles: MutableSet<String> = mutableSetOf()
    val oldFolders: MutableSet<String> = mutableSetOf()
    val newFolders: MutableSet<String> = mutableSetOf()

//    fun apply(diff: VersionRecord)
//    {
//        newFiles.removeIf { it in diff.oldFiles }
//        oldFiles.addAll(diff.oldFiles)
//
//        oldFiles.removeIf { it in diff.newFiles }
//        newFiles.addAll(diff.newFiles)
//
//        newFolders.removeIf { it in diff.oldFolders }
//        oldFolders.addAll(diff.oldFolders)
//
//        oldFolders.removeIf { it in diff.newFolders }
//        newFolders.addAll(diff.newFolders)
//    }

    fun serializeToJson(): JSONObject
    {
        val json = JSONObject()

        json.put("old_files", JSONArray(oldFiles))
        json.put("new_files", JSONArray(newFiles))
        json.put("old_folders", JSONArray(oldFolders))
        json.put("new_folders", JSONArray(newFolders))

        return json
    }
}