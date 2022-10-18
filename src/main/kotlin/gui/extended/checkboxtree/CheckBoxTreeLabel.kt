package gui.extended.checkboxtree

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JLabel
import javax.swing.UIManager
import javax.swing.plaf.ColorUIResource

class CheckBoxTreeLabel : JLabel() {
    private var isSelected = false
    private var hasFocus = false
    override fun setBackground(color: Color) {
        var color: Color? = color
        if (color is ColorUIResource) color = null
        super.setBackground(color)
    }

    override fun paint(g: Graphics) {
        var str: String
        if (text.also { str = it } != null) {
            if (str.isNotEmpty()) {
                if (isSelected) g.color = UIManager.getColor("Tree.selectionBackground") else g.color =
                    UIManager.getColor("Tree.textBackground")
                val d = preferredSize
                var imageOffset = 0
                val currentIcon = icon
                if (currentIcon != null) imageOffset = currentIcon.iconWidth + 0.coerceAtLeast(iconTextGap - 1)
                g.fillRect(imageOffset, 0, d.width - 1 - imageOffset, d.height)
                if (hasFocus) {
                    g.color = UIManager.getColor("Tree.selectionBorderColor")
                    g.drawRect(imageOffset, 0, d.width - 1 - imageOffset, d.height - 1)
                }
            }
        }
        super.paint(g)
    }

    override fun getPreferredSize(): Dimension {
        var retDimension = super.getPreferredSize()
        if (retDimension != null) retDimension = Dimension(retDimension.width + 3, retDimension.height)
        return retDimension
    }

    fun setSelected(isSelected: Boolean) {
        this.isSelected = isSelected
    }

    fun setFocus(hasFocus: Boolean) {
        this.hasFocus = hasFocus
    }
}