package pexty.floatingapp

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import pexty.utils.Utils
import pexty.utils.Utils.getStatusBarHeight


class FloatingView(
    private val context: Context,
    private val view: View,
    private var _width: Int,
    private var _height: Int,
    private var _flags: Int = 0
) {
    interface DragListener {
        fun onStartDragging(floatingView: FloatingView, touchX: Float, touchY: Float)
        fun onStopDragging(floatingView: FloatingView, touchX: Float, touchY: Float)
    }

    interface DragFilter {
        fun applyChanges(floatingView: FloatingView, touchX: Float, touchY: Float, newX: Int, newY: Int): Boolean
    }

    companion object {
        const val FLAG_DRAGGABLE = 1 shl 0
        const val FLAG_LAYOUT_NO_LIMITS = 1 shl 1
        const val FLAG_LAYOUT_IN_SCREEN = 1 shl 2
    }

    var dragFilter: DragFilter? = null
    var dragListener: DragListener? = null

    private var _isExists: Boolean = false

    private val windowManager = context.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
    private val displayMetrics = context.applicationContext.resources.displayMetrics
    private val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
        _width,
        _height,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.LEFT or Gravity.TOP
        this.x = 0
        this.y = 0
    }

    init {
        view.setOnTouchListener(object: View.OnTouchListener {
            var updatedParams: WindowManager.LayoutParams = layoutParams

            var x = 0
            var y = 0

            var touchX = 0f
            var touchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (haveFlags(FLAG_DRAGGABLE)) {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            this.x = updatedParams.x
                            this.y = updatedParams.y
                            touchX = event.rawX
                            touchY = event.rawY

                            dragListener?.onStartDragging(this@FloatingView, event.rawX, event.rawY)
                        }

                        MotionEvent.ACTION_MOVE -> {
                            updatedParams.x = (this.x + (event.rawX - touchX)).toInt()
                            updatedParams.y = (this.y + (event.rawY - touchY)).toInt()

                            if (dragFilter == null || dragFilter?.applyChanges(this@FloatingView, event.rawX, event.rawY, updatedParams.x, updatedParams.y) == true) {
                                this@FloatingView.x = updatedParams.x
                                this@FloatingView.y = updatedParams.y
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            dragListener?.onStopDragging(this@FloatingView, event.rawX, event.rawY)
                        }
                    }
                }

                return false
            }
        })
    }

    var x: Int
        get() = layoutParams.x
        set(value) {
            layoutParams.x = if (haveFlags(FLAG_LAYOUT_NO_LIMITS)) value else Utils.by_interval(
                value,
                0,
                displayMetrics.widthPixels - width
            )

            applyChanges()
        }

    var y: Int
        get() = layoutParams.y
        set(value) {
            if (haveFlags(FLAG_LAYOUT_NO_LIMITS))
                layoutParams.y = value
            else
                layoutParams.y = Utils.by_interval(
                    value,
                    if (haveFlags(FLAG_LAYOUT_IN_SCREEN)) - getStatusBarHeight(context) else 0,
                    displayMetrics.heightPixels - height - getStatusBarHeight(context)
                )

            applyChanges()
        }

    var width: Int
        get() = layoutParams.width
        set(value) {
            layoutParams.width = value

            applyChanges()
        }

    var height: Int
        get() = layoutParams.height
        set(value) {
            layoutParams.height = value

            applyChanges()
        }

    var flags: Int
        get() = _flags
        set(value) {
            _flags = value

            applyChanges()
        }

    fun haveFlags(__flags: Int): Boolean {
        return (flags and __flags) >= 1
    }

    fun addFlags(__flags: Int) {
        flags = flags or __flags
    }

    fun removeFlags(__flags: Int) {
        flags = flags and __flags.inv()
    }

    fun clearFlags() {
        flags = 0
    }

    fun havePermissions(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun getPermissions() {
        if (!havePermissions()) context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
    }

    fun isExists(): Boolean {
        return _isExists
    }

    private fun applyChanges() {
        if (isExists()) windowManager.updateViewLayout(view, layoutParams)
    }

    fun create() {
        if (havePermissions()) {
            if (!isExists()) windowManager.addView(view, layoutParams)

            _isExists = true
        }
    }

    fun destroy(immediate: Boolean = false) {
        if (isExists()) {
            if (immediate)
                windowManager.removeViewImmediate(view)
            else
                windowManager.removeView(view)
        }

        _isExists = false
    }
}
