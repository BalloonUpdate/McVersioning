package gui.extended

import cn.hutool.core.thread.ThreadUtil
import cn.hutool.core.util.StrUtil
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkContrastIJTheme
import java.awt.Color
import java.awt.Font
import java.awt.Insets
import javax.swing.UIManager
import javax.swing.border.LineBorder
import javax.swing.plaf.FontUIResource

/**
 * @author Kasumi_Nova
 * 一个工具类，用于初始化 Swing（即美化）
 */
object SwingThemeLoader
{
    @JvmStatic
    fun init()
    {
        val start1 = System.currentTimeMillis()
        //抗锯齿字体
        System.setProperty("awt.useSystemAAFontSettings", "lcd")
        System.setProperty("swing.aatext", "true")

        //UI 配置线程
        val uiThread = Thread {
            val start = System.currentTimeMillis()
            //设置圆角弧度
            UIManager.put("Button.arc", 7)
            UIManager.put("Component.arc", 7)
            UIManager.put("ProgressBar.arc", 7)
            UIManager.put("TextComponent.arc", 5)
            UIManager.put("CheckBox.arc", 3)
            //设置滚动条
            UIManager.put("ScrollBar.showButtons", false)
            UIManager.put("ScrollBar.thumbArc", 7)
            UIManager.put("ScrollBar.width", 12)
            UIManager.put("ScrollBar.thumbInsets", Insets(2, 2, 2, 2))
            UIManager.put("ScrollBar.track", Color(0, 0, 0, 0))
            //选项卡分隔线
            UIManager.put("TabbedPane.showTabSeparators", true)
            UIManager.put("TabbedPane.tabSeparatorsFullHeight", true)
            //菜单
            UIManager.put("MenuItem.selectionType", "underline")
            UIManager.put("MenuItem.underlineSelectionHeight", 3)
            UIManager.put("MenuItem.margin", Insets(5, 8, 3, 5))
            UIManager.put("MenuBar.underlineSelectionHeight", 3)
            //窗口标题居中
            UIManager.put("TitlePane.centerTitle", true)
            //进度条
            UIManager.put("ProgressBar.repaintInterval", 16)
            UIManager.put("ProgressBar.cycleTime", 7500)
            //提示框
            UIManager.put("ToolTip.border", LineBorder(Color(55, 60, 70), 2, true))
            println(StrUtil.format("UIThread Completed, Used {}ms", System.currentTimeMillis() - start))
        }
        uiThread.start()

        //字体更换线程
        val fontThread = Thread {
            val start = System.currentTimeMillis()
            //设置字体
            try
            {
                val HMOSSansAndJBMono = SwingThemeLoader::class.java.getResourceAsStream(
                    "/font/HarmonyOS_Sans_SC+JetBrains_Mono.ttf")
                val font: Font
                if (HMOSSansAndJBMono != null)
                {
                    font = Font.createFont(Font.TRUETYPE_FONT, HMOSSansAndJBMono).deriveFont(18f)
                    initGlobalFont(font)
                    UIManager.put("TitlePane.font", font)
                }
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
            println(StrUtil.format("FontThread Completed, Used {}ms", System.currentTimeMillis() - start))
        }
        fontThread.start()
        val themeThread = Thread {
            val start = System.currentTimeMillis()
            //更新 UI
            try
            {
                UIManager.setLookAndFeel(FlatAtomOneDarkContrastIJTheme())
            } catch (e: Exception)
            {
                System.err.println("Failed to initialize LaF")
                e.printStackTrace()
            }
            println(StrUtil.format("ThemeThread Completed, Used {}ms", System.currentTimeMillis() - start))
        }
        themeThread.start()
        ThreadUtil.waitForDie(uiThread)
        ThreadUtil.waitForDie(fontThread)
        ThreadUtil.waitForDie(themeThread)
        println(StrUtil.format("Swing Setup Completed, Used {}ms", System.currentTimeMillis() - start1))
    }

    /**
     * 载入全局字体
     *
     * @param font 字体
     */
    private fun initGlobalFont(font: Font)
    {
        val fontResource = FontUIResource(font)
        val keys = UIManager.getDefaults().keys()
        while (keys.hasMoreElements())
        {
            val key = keys.nextElement()
            val value = UIManager.get(key)
            if (value is FontUIResource) UIManager.put(key, fontResource)
        }
    }
}