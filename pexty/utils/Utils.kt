package pexty.utils

import android.content.Context
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity


object Utils {
    const val ORIENTATION_SQUARE = 0
    const val ORIENTATION_PORTRAIT = 1
    const val ORIENTATION_LANDSCAPE = 2

    fun getStatusBarHeight(context: Context): Int {
        return Rect().apply {
            (context as AppCompatActivity).window.getDecorView().getWindowVisibleDisplayFrame(this)
        }.top
    }

    fun by_interval(value: Int, min: Int? = null, max: Int? = null): Int {
        if (min != null && value < min) return min
        if (max != null && value > max) return max
        return value
    }

    fun by_interval(value: Float, min: Float? = null, max: Float? = null): Float {
        if (min != null && value < min) return min
        if (max != null && value > max) return max
        return value
    }

    fun by_interval(value: Double, min: Double? = null, max: Double? = null): Double {
        if (min != null && value < min) return min
        if (max != null && value > max) return max
        return value
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap, cornerRadius: Float): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val paint = Paint().apply {
            isAntiAlias = true
            color = 0xff424242.toInt()
        }
        val canvas = Canvas(output).apply {
            drawARGB(0, 0, 0, 0)
            drawRoundRect(RectF(rect), cornerRadius, cornerRadius, paint)
        }

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    fun getScreenOrientation(context: Context): Int {
        val displayMetrics = context.applicationContext.resources.displayMetrics

        return when {
            displayMetrics.heightPixels > displayMetrics.widthPixels -> ORIENTATION_PORTRAIT
            displayMetrics.heightPixels < displayMetrics.widthPixels -> ORIENTATION_LANDSCAPE
            else -> ORIENTATION_SQUARE
        }
    }
}