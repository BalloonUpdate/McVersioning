import commandline.CommandLine
import gui.McVersioningGUI
import java.awt.Desktop

object McVersioning
{
    @JvmStatic
    fun main(args: Array<String>)
    {
        if (!Desktop.isDesktopSupported() || args.isNotEmpty())
            CommandLine().main(args)
        else
            McVersioningGUI.run()
    }
}