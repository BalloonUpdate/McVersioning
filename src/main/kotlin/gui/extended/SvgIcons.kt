package gui.extended

import com.formdev.flatlaf.extras.FlatSVGIcon

/**
 * SVG 格式贴图集合
 */
object SvgIcons {
    @JvmField
    val svgIconMap = HashMap<String, FlatSVGIcon>()

    /**
     * 载入 Svg 格式图像
     */
    @JvmStatic
    fun loadIcon()
    {
        svgIconMap["server_list_icon"] = SERVER_LIST_ICON
        svgIconMap["default_server_icon"] = DEFAULT_SERVER_ICON
        svgIconMap["custom_server_icon"] = CUSTOM_SERVER_ICON

        svgIconMap["setting_icon"] = SETTINGS_ICON
        svgIconMap["info_icon"] = ABOUT_ICON
        svgIconMap["plus_icon"] = PLUS_ICON

        svgIconMap["edit_icon"] = EDIT_ICON
        svgIconMap["remove_icon"] = REMOVE_ICON
        svgIconMap["delete_icon"] = DELETE_ICON
        svgIconMap["reload_icon"] = RELOAD_ICON

        svgIconMap["terminal_icon"] = TERMINAL_ICON
        svgIconMap["resource_icon"] = RESOURCE_ICON

        svgIconMap["play_icon"] = PLAY_ICON
        svgIconMap["stop_icon"] = STOP_ICON

        svgIconMap["dir_icon"] = DIR_ICON
        svgIconMap["file_icon"] = FILE_ICON

        svgIconMap["exe_file_icon"] = EXE_FILE_ICON
        svgIconMap["jar_file_icon"] = JAR_FILE_ICON
        svgIconMap["java_file_icon"] = JAVA_FILE_ICON
        svgIconMap["class_file_icon"] = CLASS_FILE_ICON

        svgIconMap["jpg_file_icon"] = JPG_FILE_ICON
        svgIconMap["png_file_icon"] = JPG_FILE_ICON
        svgIconMap["jpeg_file_icon"] = JPG_FILE_ICON
        svgIconMap["gif_file_icon"] = JPG_FILE_ICON

        svgIconMap["md_file_icon"] = MD_FILE_ICON

        svgIconMap["ppt_file_icon"] = PPT_FILE_ICON
        svgIconMap["pptx_file_icon"] = PPT_FILE_ICON
        svgIconMap["xls_file_icon"] = XLS_FILE_ICON
        svgIconMap["xlsx_file_icon"] = XLS_FILE_ICON
        svgIconMap["doc_file_icon"] = DOC_FILE_ICON
        svgIconMap["docx_file_icon"] = DOC_FILE_ICON

        svgIconMap["txt_file_icon"] = TXT_FILE_ICON
        svgIconMap["json_file_icon"] = JSON_FILE_ICON
        svgIconMap["xml_file_icon"] = XML_FILE_ICON
        svgIconMap["yml_file_icon"] = YML_FILE_ICON

        svgIconMap["zip_file_icon"] = ZIP_FILE_ICON
        svgIconMap["rar_file_icon"] = ZIP_FILE_ICON
        svgIconMap["7z_file_icon"] = ZIP_FILE_ICON
    }

    @JvmField
    val SERVER_LIST_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/serverList.svg"))
    @JvmField
    val DEFAULT_SERVER_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/default_server.svg"))
    @JvmField
    val CUSTOM_SERVER_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/custom_server.svg"))
    @JvmField
    val SETTINGS_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/settings.svg"))
    @JvmField
    val ABOUT_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/info.svg"))
    @JvmField
    val EDIT_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/edit.svg"))
    @JvmField
    val PLUS_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/plus.svg"))
    @JvmField
    val REMOVE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/remove.svg"))
    @JvmField
    val DELETE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/delete.svg"))
    @JvmField
    val RELOAD_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/reload.svg"))
    @JvmField
    val TERMINAL_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/terminal.svg"))
    @JvmField
    val STOP_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/stop.svg"))
    @JvmField
    val RESOURCE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/resource.svg"))
    @JvmField
    val PLAY_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/play.svg"))
    @JvmField
    val DIR_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/dir.svg"))
    @JvmField
    val FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/file_default.svg"))
    @JvmField
    val CLASS_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/class.svg"))
    @JvmField
    val DOC_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/doc_docx.svg"))
    @JvmField
    val EXE_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/exe.svg"))
    @JvmField
    val JAR_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/jar.svg"))
    @JvmField
    val JAVA_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/java.svg"))
    @JvmField
    val JPG_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/jpg.svg"))
    @JvmField
    val JSON_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/json.svg"))
    @JvmField
    val MD_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/md.svg"))
    @JvmField
    val PPT_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/ppt_pptx.svg"))
    @JvmField
    val TXT_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/txt.svg"))
    @JvmField
    val XLS_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/xls_xlsx.svg"))
    @JvmField
    val XML_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/xml.svg"))
    @JvmField
    val YML_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/yml.svg"))
    @JvmField
    val ZIP_FILE_ICON = FlatSVGIcon(SvgIcons::class.java.getResource("/icons/file_types/zip.svg"))
}