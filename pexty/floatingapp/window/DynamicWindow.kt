package pexty.floatingapp.window

import android.content.Context

open class DynamicWindow : Window {
    protected constructor(
        context: Context,
        original: DynamicWindow
    ): super(
        context,
        original.minWidth,
        original.minHeight,
        original.title,
        original.flags or FLAG_NOT_OPENING_ANIMATION
    ) {
        dynamicWindow = original
        isClone = true
    }

    constructor(
        context: Context,
        minWidth: Int = 0,
        minHeight: Int = 0,
        title: String = "Floating Window",
        __flags: Int = 0
    ) : super(
        context,
        minWidth,
        minHeight,
        title,
        FLAG_NOT_UNFOCUS_BY_REPLACE_ON_IMAGE or __flags
    ) {
        isClone = false

        dynamicWindow = createDynamicWindow()
    }

    protected val isClone: Boolean
    protected val dynamicWindow: DynamicWindow

//    private var lockDynamicUnfocus = false
//    private var lockDynamicFocus = false
//    private var isClosingByUser = true

    protected open fun createDynamicWindow(): DynamicWindow {
        return DynamicWindow(context, this)
    }

//    override fun _focus() {
//        super._focus()
//
//        if (haveFlags(FLAG_NOT_UNFOCUS_BY_REPLACE_ON_IMAGE) && !lockDynamicFocus) {
//            if (isClone) {
//                dynamicWindow.focus()
//            }
//            else {
//                lockDynamicFocus = true
//
//                if (dynamicWindow.isExists()) {
//                    x = dynamicWindow.x
//                    y = dynamicWindow.y
//                    width = dynamicWindow.width
//                    height = dynamicWindow.height
//                }
//
//                val have = haveFlags(FLAG_NOT_OPENING_ANIMATION)
//
//                addFlags(FLAG_NOT_OPENING_ANIMATION)
//                open()
//
//                dynamicWindow.close()
//
//                if (!have) removeFlags(FLAG_NOT_OPENING_ANIMATION)
//            }
//        } else lockDynamicFocus = false
//    }

//    override fun _unfocus() {
//        super._unfocus()
//
//        if (haveFlags(FLAG_NOT_UNFOCUS_BY_REPLACE_ON_IMAGE) && !isClone && !lockDynamicUnfocus) {
//            lockDynamicUnfocus = true
//            isClosingByUser = false
//
//            dynamicWindow.isClosingByUser = false
//
//            dynamicWindow.x = x
//            dynamicWindow.y = y
//            dynamicWindow.minWidth = minWidth
//            dynamicWindow.minHeight = minHeight
//            dynamicWindow.width = width
//            dynamicWindow.height = height
//            dynamicWindow.title = title
//            dynamicWindow.frameColor = frameColor
//            dynamicWindow.actionBarColor = actionBarColor
//            dynamicWindow.borderColor = borderColor
//            dynamicWindow.titleColor = titleColor
//            dynamicWindow.mode = mode
//            dynamicWindow.flags = flags or FLAG_NOT_OPENING_ANIMATION
//
//            dynamicWindow.open()
//            close()
//        } else lockDynamicUnfocus = false
//    }

//    override fun _close() {
//        if (isExists() && isClosingByUser) dynamicWindow.close()
//
//        isClosingByUser = true
//
//        super._close()
//
//        println("Close: DynamicWindow{isClone=$isClone}")
//    }

    var isClosingByUser = true

    override fun _onStopDragging(element: Int, touchX: Float, touchY: Float) {
        super._onStopDragging(element, touchX, touchY)

        if (element == ELEMENT_WINDOW && isClone) dynamicWindow.focus()
    }

    override fun _focus() {
        if (!isClone) {
            val lastIsFocused = isFocused()
            super._focus()

            if (!lastIsFocused && haveFlags(FLAG_NOT_UNFOCUS_BY_REPLACE_ON_IMAGE)) {
                val have = haveFlags(FLAG_NOT_OPENING_ANIMATION)
                addFlags(FLAG_NOT_OPENING_ANIMATION)

                x = dynamicWindow.x
                y = dynamicWindow.y
                width = dynamicWindow.width
                height = dynamicWindow.height

                open()
                dynamicWindow.close()

                if (!have) removeFlags(FLAG_NOT_OPENING_ANIMATION)
            }
        }
    }

    override fun _unfocus() {
        if (!isClone) super._unfocus()

        if (haveFlags(FLAG_NOT_UNFOCUS_BY_REPLACE_ON_IMAGE) && isExists() && !isClone) {
            dynamicWindow.x = x
            dynamicWindow.y = y
            dynamicWindow.minWidth = minWidth
            dynamicWindow.minHeight = minHeight
            dynamicWindow.width = width
            dynamicWindow.height = height
            dynamicWindow.title = title
            dynamicWindow.frameColor = frameColor
            dynamicWindow.actionBarColor = actionBarColor
            dynamicWindow.borderColor = borderColor
            dynamicWindow.titleColor = titleColor
            dynamicWindow.mode = mode
            dynamicWindow.flags = flags or FLAG_NOT_OPENING_ANIMATION

            dynamicWindow.open()

            isClosingByUser = false
            close()
        }
    }

    override fun _close() {
        if (isExists() && isClosingByUser && !isClone) dynamicWindow.close()

        isClosingByUser = true

        super._close()
    }
}