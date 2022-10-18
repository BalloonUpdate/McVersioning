package gui.layoutmanager

import java.awt.*

/**
 * MyVFlowLayout is similar to FlowLayout except it lays out components
 * vertically. Extends FlowLayout because it mimics much of the behavior of the
 * FlowLayout class, except vertically. An additional feature is that you can
 * specify a fill to edge flag, which causes the MyVFlowLayout manager to
 * resize all components to expand to the column width Warning: This causes
 * problems when the main panel has less space that it needs, and it seems to
 * prohibit multi-column output. Additionally, there is a vertical fill flag,
 * which fills the last component to the remaining height of the container.
 */
class VFlowLayout @JvmOverloads constructor(
    verticalAlignment: Int = TOP,
    horizontalAlignment: Int = MIDDLE,
    horizontalGap: Int = 5,
    verticalGap: Int = 5,
    topVerticalGap: Int = 5,
    bottomVerticalGap: Int = 5,
    isHorizontalFill: Boolean = true,
    isVerticalFill: Boolean = false
) : FlowLayout() {
    private var horizontalAlignment = 0
    var topVerticalGap = 0
    var bottomVerticalGap = 0
    /**
     * Returns true if the layout horizontally fills.
     *
     * Set to true to enable horizontally fill.
     *
     * @return true if horizontally fills.
     */
    var horizontalFill = false

    /**
     * Returns true if the layout vertically fills.
     *
     * Set true to fill vertically.
     *
     * @return true if vertically fills the layout using the specified.
     */
    var verticalFill = false

    constructor(isHorizontalFill: Boolean, isVerticalFill: Boolean) : this(
        TOP,
        MIDDLE,
        5,
        5,
        5,
        5,
        isHorizontalFill,
        isVerticalFill
    )

    constructor(align: Int, isHorizontalFill: Boolean, isVerticalFill: Boolean) : this(
        align,
        MIDDLE,
        5,
        5,
        5,
        5,
        isHorizontalFill,
        isVerticalFill
    )

    constructor(
        align: Int,
        horizontalGap: Int,
        verticalGap: Int,
        isHorizontalFill: Boolean,
        isVerticalFill: Boolean
    ) : this(align, MIDDLE, horizontalGap, verticalGap, verticalGap, verticalGap, isHorizontalFill, isVerticalFill)

    constructor(
        align: Int,
        horizontalGap: Int,
        verticalGap: Int,
        topVerticalGap: Int,
        bottomVerticalGap: Int,
        isHorizontalFill: Boolean,
        isVerticalFill: Boolean
    ) : this(
        align,
        MIDDLE,
        horizontalGap,
        verticalGap,
        topVerticalGap,
        bottomVerticalGap,
        isHorizontalFill,
        isVerticalFill
    )

    /**
     * Construct a new MyVFlowLayout.
     *
     * @param verticalAlignment   the alignment value
     * @param horizontalAlignment the horizontal alignment value
     * @param horizontalGap       the horizontal gap variable
     * @param verticalGap         the vertical gap variable
     * @param topVerticalGap      the top vertical gap variable
     * @param bottomVerticalGap   the bottom vertical gap variable
     * @param isHorizontalFill    the fill to edge flag
     * @param isVerticalFill      true if the panel should vertically fill.
     */
    init {
        this.alignment = verticalAlignment
        setHorizontalAlignment(horizontalAlignment)
        hgap = horizontalGap
        vgap = verticalGap
        this.topVerticalGap = topVerticalGap
        this.bottomVerticalGap = bottomVerticalGap
        horizontalFill = isHorizontalFill
        verticalFill = isVerticalFill
    }

    fun getHorizontalAlignment(): Int
    {
        return horizontalAlignment
    }

    fun setHorizontalAlignment(horizontalAlignment: Int)
    {
        if (LEFT == horizontalAlignment) {
            this.horizontalAlignment = LEFT
        } else if (RIGHT == horizontalAlignment) {
            this.horizontalAlignment = RIGHT
        } else {
            this.horizontalAlignment = MIDDLE
        }
    }

    /**
     * Returns the preferred dimensions given the components in the target
     * container.
     *
     * @param container the component to lay out
     */
    override fun preferredLayoutSize(container: Container): Dimension
    {
        val rs = Dimension(0, 0)
        val components = getVisibleComponents(container)
        val dimension = preferredComponentsSize(components)
        rs.width += dimension.width
        rs.height += dimension.height
        val insets = container.insets
        rs.width += insets.left + insets.right
        rs.height += insets.top + insets.bottom
        if (components.isNotEmpty())
        {
            rs.width += hgap * 2
            rs.height += topVerticalGap
            rs.height += bottomVerticalGap
        }
        return rs
    }

    /**
     * Returns the minimum size needed to layout the target container.
     *
     * @param container the component to lay out.
     * @return the minimum layout dimension.
     */
    override fun minimumLayoutSize(container: Container): Dimension
    {
        val rs = Dimension(0, 0)
        val components = getVisibleComponents(container)
        val dimension = minimumComponentsSize(components)
        rs.width += dimension.width
        rs.height += dimension.height
        val insets = container.insets
        rs.width += insets.left + insets.right
        rs.height += insets.top + insets.bottom
        if (components.isNotEmpty())
        {
            rs.width += hgap * 2
            rs.height += topVerticalGap
            rs.height += bottomVerticalGap
        }
        return rs
    }

    override fun layoutContainer(container: Container) {
        val horizontalGap = hgap
        val verticalGap = vgap
        val insets = container.insets
        val maxWidth = container.size.width - insets.left + insets.right + horizontalGap * 2
        val maxHeight = container.size.height - (insets.top + insets.bottom + topVerticalGap + bottomVerticalGap)
        val components = getVisibleComponents(container)
        val preferredComponentsSize = preferredComponentsSize(components)
        val alignment = this.alignment
        var y = insets.top + topVerticalGap
        if (!verticalFill && preferredComponentsSize.height < maxHeight)
        {
            if (MIDDLE == alignment) {
                y += (maxHeight - preferredComponentsSize.height) / 2
            } else if (BOTTOM == alignment) {
                y += maxHeight - preferredComponentsSize.height
            }
        }
        for ((index, component) in components.withIndex())
        {
            var x = insets.left + horizontalGap
            val dimension = component.preferredSize
            if (horizontalFill) {
                dimension.width = maxWidth
            } else {
                dimension.width = maxWidth.coerceAtMost(dimension.width)
            }

            if (MIDDLE == horizontalAlignment) {
                x += (maxWidth - dimension.width) / 2
            } else if (RIGHT == horizontalAlignment) {
                x += maxWidth - dimension.width
            }

            if (verticalFill && index == components.size - 1) {
                val height = maxHeight + topVerticalGap + insets.top - y
                dimension.height = height.coerceAtLeast(dimension.height)
            }
            component.size = dimension
            component.setLocation(x, y)
            y += dimension.height + verticalGap
        }
    }

    private fun preferredComponentsSize(components: List<Component>): Dimension
    {
        val rs = Dimension(0, 0)
        for (component in components) {
            val dimension = component.preferredSize
            rs.width = rs.width.coerceAtLeast(dimension.width)
            rs.height += dimension.height
        }
        if (components.isNotEmpty()) {
            rs.height += vgap * (components.size - 1)
        }
        return rs
    }

    private fun minimumComponentsSize(components: List<Component>): Dimension
    {
        val rs = Dimension(0, 0)
        for (component in components) {
            val dimension = component.minimumSize
            rs.width = rs.width.coerceAtLeast(dimension.width)
            rs.height += dimension.height
        }
        if (components.isNotEmpty()) {
            rs.height += vgap * (components.size - 1)
        }
        return rs
    }

    private fun getVisibleComponents(container: Container): List<Component>
    {
        val rs: MutableList<Component> = ArrayList()
        for (component in container.components) {
            if (component.isVisible) {
                rs.add(component)
            }
        }
        return rs
    }

    companion object
    {
        /**
         * Specify alignment top.
         */
        const val TOP = 0

        /**
         * Specify a middle alignment.
         */
        const val MIDDLE = 1

        /**
         * Specify the alignment to be bottom.
         */
        const val BOTTOM = 2

        /**
         * Specify the alignment to be left.
         */
        const val LEFT = 0

        /**
         * Specify the alignment to be right.
         */
        const val RIGHT = 2
        private const val serialVersionUID = 1L
    }
}