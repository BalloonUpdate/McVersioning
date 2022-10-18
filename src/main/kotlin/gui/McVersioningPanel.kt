package gui

import gui.extended.SvgIcons
import gui.extended.SwingThemeLoader
import javax.swing.JFrame
import javax.swing.JPanel

object McVersioningPanel
{
    init {
        SvgIcons.loadIcon()
        SwingThemeLoader.init()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val mcVersioningPanel = McVersioningPanel
        mcVersioningPanel.initialize()
    }

    val frame = JFrame("McVersioning GUI")
    val mainPanel = JPanel()

    fun initialize() {
        frame.add(mainPanel)

        frame.setSize(1400,815)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}