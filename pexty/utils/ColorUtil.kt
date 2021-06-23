package pexty.utils

import android.graphics.Color

object ColorUtil {
    fun toArgb(color: Int): IntArray {
        val colorBase = Color.valueOf(color).components
        return intArrayOf((colorBase[colorBase.size - 1] * 255).toInt(), (colorBase[0] * 255).toInt(), (colorBase[1] * 255).toInt(), (colorBase[2] * 255).toInt())
    }

    fun setAlpha(color: Int, alpha: Int): Int {
        val colorBase = toArgb(color)
        return Color.argb(alpha, colorBase[1], colorBase[2], colorBase[3])
    }

    fun invert(color: Int): Int {
        val colorBase = toArgb(color).apply {
            forEachIndexed { index, item -> if (index > 0) set(index, 255 - this[index]) }
        }
        return Color.argb(colorBase[0], colorBase[1], colorBase[2], colorBase[3])
    }

    fun toGray(color: Int): Int {
        val colorBase = toArgb(color)
        val L: Int = (colorBase[1].toFloat() * 299f/1000f + colorBase[2].toFloat() * 587f/1000f + colorBase[3].toFloat() * 114f/1000f).toInt()
        return Color.argb(colorBase[0], L, L, L)
    }

    fun toBlackWhite(color: Int): Int {
        val colorBase = toArgb(toGray(color)).apply {
            forEachIndexed { index, item -> if (index > 0) set(index, if (item < 127) 0 else 255) }
        }
        return Color.argb(colorBase[0], colorBase[1], colorBase[2], colorBase[3])
    }

    fun toTextColorByBackground(color: Int): Int {
        return toBlackWhite(invert(color))
    }
}