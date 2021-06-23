package pexty.floatingapp.window

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.*
import android.graphics.drawable.shapes.Shape
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import android.view.*
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.drawable.toBitmap
import com.pexty.studios.floating.windows.R
import pexty.floatingapp.FloatingView
import pexty.utils.ColorUtil
import pexty.utils.Utils
import java.util.*


open class Window(
    protected val context: Context,
    private var _minWidth: Int = 0,
    private var _minHeight: Int = 0,
    title: String = "Floating Window",
    private var _flags: Int = 0
) {
    interface EventListener {
        fun onOpen(window: Window) {}
        fun onClose(window: Window) {}
        fun onFocus(window: Window) {}
        fun onUnfocus(window: Window) {}
        fun onMainLoop(window: Window) {}
    }

    companion object {
        private const val padding = 8

        const val FLAG_NO_ICON = 1 shl 0
        const val FLAG_NO_TITLE = 1 shl 1
        const val FLAG_NO_MINIMIZE_BUTTON = 1 shl 2
        const val FLAG_NO_QUIT_BUTTON = 1 shl 3
        const val FLAG_NO_MAXIMIZE_RESTORE_BUTTON = 1 shl 4
        const val FLAG_NO_CONTROLS = FLAG_NO_MINIMIZE_BUTTON or FLAG_NO_QUIT_BUTTON or FLAG_NO_MAXIMIZE_RESTORE_BUTTON
        const val FLAG_NO_ACTIONBAR = 1 shl 5
        const val FLAG_NO_BORDER = 1 shl 6
        const val FLAG_ONLY_CONTENT = FLAG_NO_ACTIONBAR or FLAG_NO_BORDER
        const val FLAG_RESIZABLE = 1 shl 7
        const val FLAG_NOT_RESIZABLE_BY_MOTION = 1 shl 8
        const val FLAG_NOT_DRAGGABLE = 1 shl 9
        const val FLAG_DRAGGABLE_BY_CONTENT = 1 shl 10
        const val FLAG_NOT_UNFOCUS_BY_REPLACE_ON_IMAGE = 1 shl 11
        const val FLAG_NOT_FOCUS_FROM_TOUCH = 1 shl 12
        const val FLAG_DISABLE_MAIN_LOOP = 1 shl 13
        const val FLAG_NOT_OPENING_ANIMATION = 1 shl 14

        const val MODE_RESTORE = 0
        const val MODE_MAXIMIZE = 1
        const val MODE_MINIMIZE = 2

        const val TARGET_AUTO = 0
        const val TARGET_ALL = 1
        const val TARGET_CONTENT = 2

        const val GRAVITY_LEFT = 0
        const val GRAVITY_TOP = 0
        const val GRAVITY_RIGHT = 1 shl 0
        const val GRAVITY_BOTTOM = 1 shl 1
        const val GRAVITY_CENTER_HORIZONTAL = 1 shl 2
        const val GRAVITY_CENTER_VERTICAL = 1 shl 3
        const val GRAVITY_CENTER = GRAVITY_CENTER_HORIZONTAL or GRAVITY_CENTER_VERTICAL

        const val ELEMENT_WINDOW = 0
        const val ELEMENT_FAKE_WINDOW = 1
        const val ELEMENT_MINI_WINDOW = 2
    }

    var eventListener: EventListener? = null

    protected val displayMetrics = context.applicationContext.resources.displayMetrics

    private val positiveScaleAnimation = ScaleAnimation(
        0.7f,
        1f,
        0.7f,
        1f,
        Animation.RELATIVE_TO_SELF,
        0.5f,
        Animation.RELATIVE_TO_SELF,
        0.5f
    ).apply {
        interpolator = LinearInterpolator()
        duration = 100
    }

    private var _isFocused = true
    private var _isExists = false

    private var minimizeTEMP = false
    private var valuesTEMP = intArrayOf(0, 0, 0, 0, 0)
    private var _mode = MODE_RESTORE

    private var _frameColor: Int = Color.WHITE
    private var _actionBarColor: Int? = null
    private var _borderColor: Int? = null
    private var _titleColor: Int? = null

    private val iconView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.FIT_XY
        layoutParams = TableLayout.LayoutParams(0, 0).apply {
            leftMargin = 5
        }
    }
    private val miniIconView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.FIT_XY
        layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT).apply {
            setMargins(5, 5, 5, 5)
        }
    }
    private val miniView = LinearLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT)
        orientation = LinearLayout.VERTICAL

        setOnClickListener {
            mode = valuesTEMP[4]
        }

        addView(miniIconView)
    }
    private val titleView = TextView(context).apply {
        layoutParams = TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            leftMargin = 5
            weight = 1f
        }

        text = title
        maxLines = 1
        setEms(3)
        ellipsize = TextUtils.TruncateAt.END
        setTypeface(typeface, Typeface.BOLD)
    }
    private val minimizeView = LinearLayout(context).apply {
        setOnClickListener {
            mode = MODE_MINIMIZE
        }
    }
    private val maximizeRestoreView = LinearLayout(context).apply {
        setOnClickListener {
            mode = if (mode == MODE_RESTORE)
                MODE_MAXIMIZE
            else
                MODE_RESTORE
        }
    }
    private val quitView = LinearLayout(context).apply {
        setOnClickListener {
            close()
        }
    }
    private val controlsView = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)

        gravity = Gravity.CENTER

        addView(minimizeView)
        addView(maximizeRestoreView)
        addView(quitView)
    }
    private val actionBarView = LinearLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT)
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER

        addView(iconView)
        addView(titleView)
        addView(controlsView)

        measure(layoutParams.width, layoutParams.height)
    }
    private val contentView = LinearLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT)
        orientation = LinearLayout.VERTICAL
    }
    private val containerView = LinearLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT).apply {
            setMargins(padding, padding, padding, padding)
        }
        orientation = LinearLayout.VERTICAL

        addView(actionBarView)
        addView(contentView)
    }
    private val resizingAnchorView = LinearLayout(context).apply {
        layoutParams = LinearLayout.LayoutParams(100, 100)
        setOnTouchListener(object: View.OnTouchListener {
            var DownPT: PointF? = PointF()
            var StartPT: PointF? = PointF()
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                mode = MODE_RESTORE
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        DownPT?.x = event.x
                        DownPT?.y = event.y
                        StartPT = PointF(this@apply.x, this@apply.x)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val mv: PointF = PointF(event.x - DownPT!!.x, event.y - DownPT!!.y)
                        this@Window.width = (StartPT!!.x + mv.x).toInt() + this@apply.layoutParams.width
                        this@Window.height = (StartPT!!.y + mv.y).toInt() + this@apply.layoutParams.height
                        this@apply.x = (StartPT!!.x + mv.x)
                        this@apply.y = (StartPT!!.y + mv.y)
                        StartPT = PointF(this@apply.x, this@apply.y)
                    }
                }

                this@Window.width = this@Window.width
                this@Window.height = this@Window.height

                return true
            }
        })
    }
    val view = CoordinatorLayout(context).apply {
        layoutParams = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.MATCH_PARENT)

//        setPadding(padding, padding, padding, padding)

        addView(containerView)
        addView(resizingAnchorView)
    }

    private val miniFloatingView = FloatingView(
        context,
        miniView,
        0,
        0,
        FloatingView.FLAG_DRAGGABLE
    ).apply {
        dragListener = object: FloatingView.DragListener {
            override fun onStartDragging(floatingView: FloatingView, touchX: Float, touchY: Float) {
                this@Window.onStartDragging(ELEMENT_MINI_WINDOW, touchX, touchY)
            }

            override fun onStopDragging(floatingView: FloatingView, touchX: Float, touchY: Float) {
                this@Window.onStopDragging(ELEMENT_MINI_WINDOW, touchX, touchY)
            }
        }
    }
    private val floatingView = FloatingView(
        context,
        view,
        _minWidth,
        _minHeight,
        FloatingView.FLAG_DRAGGABLE or FloatingView.FLAG_LAYOUT_NO_LIMITS
    ).apply {
        dragFilter = object: FloatingView.DragFilter {
            override fun applyChanges(
                floatingView: FloatingView,
                touchX: Float,
                touchY: Float,
                newX: Int,
                newY: Int
            ): Boolean {
                this@Window.x = newX
                this@Window.y = newY

                if (!this@Window.haveFlags(FLAG_NOT_RESIZABLE_BY_MOTION)) {
                    if (touchY > Utils.getStatusBarHeight(context) && mode == MODE_MAXIMIZE) {
                        valuesTEMP[1] = touchY.toInt()
                        mode = MODE_RESTORE
                    }

                    if (touchY <= Utils.getStatusBarHeight(context) && mode == MODE_RESTORE)
                        mode = MODE_MAXIMIZE
                }

                return this@Window.x == newX && this@Window.y == newY
            }
        }

        dragListener = object: FloatingView.DragListener {
            override fun onStartDragging(floatingView: FloatingView, touchX: Float, touchY: Float) {
                this@Window.onStartDragging(ELEMENT_WINDOW, touchX, touchY)
            }

            override fun onStopDragging(floatingView: FloatingView, touchX: Float, touchY: Float) {
                this@Window.onStopDragging(ELEMENT_WINDOW, touchX, touchY)
            }
        }
    }
    private val fakeView = ImageView(context).apply {
        scaleType = ImageView.ScaleType.FIT_XY
        layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT).apply {
            leftMargin = 5
        }
        setOnClickListener {
            if (!haveFlags(FLAG_NOT_FOCUS_FROM_TOUCH)) focus()
        }
    }
    private val fakeFloatingView = FloatingView(
        context,
        fakeView,
        _minWidth,
        _minHeight,
        FloatingView.FLAG_DRAGGABLE or FloatingView.FLAG_LAYOUT_NO_LIMITS
    ).apply {
        dragListener = object: FloatingView.DragListener {
            override fun onStartDragging(floatingView: FloatingView, touchX: Float, touchY: Float) {
                if (!this@Window.haveFlags(FLAG_NOT_FOCUS_FROM_TOUCH)) Manager._unfocusCurrent()
            }

            override fun onStopDragging(floatingView: FloatingView, touchX: Float, touchY: Float) {
                if (!this@Window.haveFlags(FLAG_NOT_FOCUS_FROM_TOUCH)) focus()
            }
        }

        dragFilter = object: FloatingView.DragFilter {
            override fun applyChanges(
                floatingView: FloatingView,
                touchX: Float,
                touchY: Float,
                newX: Int,
                newY: Int
            ): Boolean {
                this@Window.x = newX
                this@Window.y = newY

                return this@Window.x == newX && this@Window.y == newY
            }
        }
    }

    var title: String
        get() = titleView.text.toString()
        set(value) {
            titleView.text = value
        }

    var x: Int
        get() = floatingView.x + padding
        set(value) {
            floatingView.x = (if (mode != MODE_MAXIMIZE) Utils.by_interval(value, -(width - width / 3), displayMetrics.widthPixels - width / 3) else 0) - padding
            fakeFloatingView.x = floatingView.x
        }

    var y: Int
        get() = floatingView.y + padding
        set(value) {
            floatingView.y = (if (mode != MODE_MAXIMIZE) Utils.by_interval(value, 0, displayMetrics.heightPixels - height / 2) else 0) - padding
            fakeFloatingView.y = floatingView.y
        }

    var minWidth: Int
        get() = _minWidth
        set(value) {
            _minWidth = Utils.by_interval(value, getActionBarHeight() * 3 + padding * 2 + 50)
            width = width
        }

    var minHeight: Int
        get() = _minHeight
        set(value) {
            _minHeight = Utils.by_interval(value, getActionBarHeight() * 2 + padding * 2 + 5)
            height = height
        }

    var width: Int
        get() = floatingView.width - padding * 2
        set(value) {
            floatingView.width = Utils.by_interval(value, minWidth, displayMetrics.widthPixels + minWidth / 3 * 4) + padding * 2
            fakeFloatingView.width = floatingView.width

            resizingAnchorView.x = (floatingView.width - resizingAnchorView.layoutParams.width - 10 - padding).toFloat()
        }

    var height: Int
        get() = floatingView.height - getActionBarHeight(true) - padding * 2
        set(value) {
            floatingView.height = Utils.by_interval(value, minHeight, displayMetrics.heightPixels - Utils.getStatusBarHeight(context)) + getActionBarHeight(true) + padding * 2
            fakeFloatingView.height = floatingView.height

            resizingAnchorView.y = (floatingView.height - resizingAnchorView.layoutParams.height - 10 - padding).toFloat()
        }

    var frameColor: Int
        get() = _frameColor
        set(value) {
            _frameColor = value

            applyChanges()
        }

    var actionBarColor: Int?
        get() = if (_actionBarColor == null) frameColor else _actionBarColor
        set(value) {
            _actionBarColor = value

            applyChanges()
        }

    var borderColor: Int?
        get() = if (_borderColor == null) frameColor else _borderColor
        set(value) {
            _borderColor = value

            applyChanges()
        }

    var titleColor: Int?
        get() = if (_titleColor == null) ColorUtil.toTextColorByBackground(actionBarColor!!) else _titleColor
        set(value) {
            _titleColor = value
        }

    var mode: Int
        get() = _mode
        set(value) {
            _mode = value

            if (minimizeTEMP) {
                focus()

                containerView.startAnimation(positiveScaleAnimation)
                if (haveFlags(FLAG_RESIZABLE)) resizingAnchorView.startAnimation(positiveScaleAnimation)
            }

            applyChanges()
        }

    var flags: Int
        get() = _flags
        set(value) {
            _flags = value

            applyChanges()
        }

    init {
        minWidth += 0
        minHeight += 0

        x = 0
        y = 0

        applyChanges()
        setIconImage(R.mipmap.ic_launcher)
    }

    fun setPositionByGravity(gravity: Int) {
        x = when {
            gravity and GRAVITY_RIGHT >= 1 -> displayMetrics.widthPixels - width
            gravity and GRAVITY_CENTER_HORIZONTAL >= 1 -> (displayMetrics.widthPixels - width) / 2
            else -> 0
        }

        y = when {
            gravity and GRAVITY_BOTTOM >= 1 -> displayMetrics.heightPixels - Utils.getStatusBarHeight(context) - height - getActionBarHeight(true)
            gravity and GRAVITY_CENTER_VERTICAL >= 1 -> (displayMetrics.heightPixels - Utils.getStatusBarHeight(context) - height - getActionBarHeight(true)) / 2
            else -> 0
        }
    }

    fun setIconImage(resourceId: Int) {
        setIconImage(context.resources.getDrawable(resourceId))
    }

    fun setIconImage(drawable: Drawable) {
        setIconImage(drawable.toBitmap())
    }

    fun setIconImage(bitmap: Bitmap) {
        val bitmap = Utils.getRoundedCornerBitmap(bitmap, 9000000f)
        iconView.setImageBitmap(bitmap)
        miniIconView.setImageBitmap(bitmap)
    }

    fun setIconImage(icon: Icon) {
        setIconImage(icon.loadDrawable(context))
    }

    protected fun setContent(view: View?) {
        contentView.removeAllViews()

        if (view != null) contentView.addView(view)
    }

    protected fun removeContent() {
        setContent(null)
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

    fun isHavePermissions(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun getPermissions() {
        if (!isHavePermissions()) context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
    }

    fun isExists(): Boolean {
        return _isExists
    }

    fun getActionBarHeight(takeIntoAccountDisplay: Boolean = false): Int {
        return if (takeIntoAccountDisplay) (if (!haveFlags(FLAG_NO_ACTIONBAR)) titleView.measuredHeight else 0) else titleView.measuredHeight
    }

    fun applyChanges() {
        contentView.isClickable = !haveFlags(FLAG_DRAGGABLE_BY_CONTENT)

        if (haveFlags(FLAG_NOT_DRAGGABLE)) {
            floatingView.removeFlags(FloatingView.FLAG_DRAGGABLE)
            fakeFloatingView.removeFlags(FloatingView.FLAG_DRAGGABLE)
        }
        else {
            floatingView.addFlags(FloatingView.FLAG_DRAGGABLE)
            fakeFloatingView.addFlags(FloatingView.FLAG_DRAGGABLE)
        }

        if (mode == MODE_RESTORE && valuesTEMP[4] != MODE_RESTORE) {
            focus()

            width = valuesTEMP[2]
            height = valuesTEMP[3]

            x = valuesTEMP[0]
            y = valuesTEMP[1]

            valuesTEMP[4] = MODE_RESTORE
        }

        if (mode == MODE_MAXIMIZE) {
            if (haveFlags(FLAG_RESIZABLE)) {
                if (valuesTEMP[4] != MODE_MAXIMIZE) {
                    focus()
                    valuesTEMP = intArrayOf(x, y, width, height, MODE_MAXIMIZE)
                }

                x = 0
                y = 0

                width = displayMetrics.widthPixels
                height = displayMetrics.heightPixels - (if (!haveFlags(FLAG_NO_ACTIONBAR)) getActionBarHeight() * 2 else 0) - padding * 2
            } else _mode = valuesTEMP[4]
        }

        if (mode == MODE_MINIMIZE && valuesTEMP[4] != MODE_MINIMIZE) {
            minimizeTEMP = true

            _mode = MODE_RESTORE
            unfocus()
            _mode = MODE_MINIMIZE

            miniFloatingView.apply {
                this.x = if (this@Window.x > displayMetrics.widthPixels - (this@Window.x + this@Window.width)) displayMetrics.widthPixels else 0
                this.y = this@Window.y
            }

            if (isExists()) {
                miniFloatingView.create()
                floatingView.destroy()
                fakeFloatingView.destroy()

                miniIconView.startAnimation(positiveScaleAnimation)
            }
        }
        else if (isExists()) {
            minimizeTEMP = false
            miniFloatingView.destroy()

            if (isFocused()) {
                floatingView.create()
                fakeFloatingView.destroy()
            }
            else {
                fakeFloatingView.create()
                floatingView.destroy()
            }
        }

        maximizeRestoreView.isEnabled = haveFlags(FLAG_RESIZABLE)

        if (haveFlags(FLAG_NO_ACTIONBAR)) {
            if (actionBarView.visibility == View.VISIBLE) {
                _minHeight -= getActionBarHeight()
                floatingView.height -= getActionBarHeight()
            }
            actionBarView.visibility = View.GONE

            contentView.layoutParams = LinearLayout.LayoutParams(contentView.layoutParams).apply {
                setMargins(5, 5, 5, 5)
            }
        }
        else {
            if (actionBarView.visibility == View.GONE) {
                _minHeight += getActionBarHeight()
                floatingView.height += getActionBarHeight()
            }
            actionBarView.visibility = View.VISIBLE

            contentView.layoutParams = LinearLayout.LayoutParams(contentView.layoutParams).apply {
                setMargins(5, 0, 5, 5)
            }

            if (haveFlags(FLAG_NO_ICON))
                iconView.visibility = View.GONE
            else
                iconView.visibility = View.VISIBLE

            if (haveFlags(FLAG_NO_TITLE))
                titleView.visibility = View.INVISIBLE
            else
                titleView.visibility = View.VISIBLE

            if (haveFlags(FLAG_NO_QUIT_BUTTON))
                quitView.visibility = View.GONE
            else
                quitView.visibility = View.VISIBLE

            if (haveFlags(FLAG_NO_MAXIMIZE_RESTORE_BUTTON))
                maximizeRestoreView.visibility = View.GONE
            else
                maximizeRestoreView.visibility = View.VISIBLE

            if (haveFlags(FLAG_NO_MINIMIZE_BUTTON))
                minimizeView.visibility = View.GONE
            else
                minimizeView.visibility = View.VISIBLE
        }

        if (haveFlags(FLAG_NO_BORDER)) {
            contentView.layoutParams = LinearLayout.LayoutParams(contentView.layoutParams).apply {
                setMargins(0, 0, 0, 0)
            }
        }

        miniFloatingView.apply {
            width = getActionBarHeight() * 2 + 10
            height = getActionBarHeight() * 2 + 10
        }

        iconView.layoutParams.apply {
            width = getActionBarHeight() - 10
            height = getActionBarHeight() - 10
        }

        titleView.apply {
            setTextColor(titleColor!!)
        }

        minimizeView.apply {
            layoutParams = LinearLayout.LayoutParams(getActionBarHeight() - 10, getActionBarHeight() - 10).apply {
                rightMargin = 5
            }

            background = RippleDrawable(
                ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.argb(150, 255, 255, 255))),
                GradientDrawable().apply {
                    setColor(Color.parseColor("#4caf50"))
                    cornerRadius = 90f
                    setStroke(2, Color.WHITE)
                },
                null
            )
        }

        maximizeRestoreView.apply {
            layoutParams = LinearLayout.LayoutParams(getActionBarHeight() - 10, getActionBarHeight() - 10).apply {
                rightMargin = 5
            }

            background = RippleDrawable(
                ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.argb(150, 255, 255, 255))),
                GradientDrawable().apply {
                    setColor(if (isEnabled) Color.BLUE else Color.parseColor("#7986CB"))
                    cornerRadius = 90f
                    setStroke(2, Color.WHITE)
                },
                null
            )
        }

        quitView.apply {
            layoutParams = LinearLayout.LayoutParams(getActionBarHeight() - 10, getActionBarHeight() - 10).apply {
                rightMargin = 5
            }

            background = RippleDrawable(
                ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.argb(150, 255, 255, 255))),
                GradientDrawable().apply {
                    setColor(Color.RED)
                    cornerRadius = 90f
                    setStroke(2, Color.WHITE)
                },
                null
            )
        }

        actionBarView.apply {
            setBackgroundDrawable(GradientDrawable().apply {
                setColor(actionBarColor!!)
                cornerRadii = floatArrayOf(20f, 20f, 20f, 20f, 0f, 0f, 0f, 0f)
            })
        }

        contentView.apply {
            setBackgroundDrawable(GradientDrawable().apply {
                setColor(Color.BLACK)

                if (actionBarColor == borderColor || haveFlags(FLAG_NO_ACTIONBAR))
                    cornerRadius = 20f
                else
                    cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, 20f, 20f, 20f, 20f)
            })
        }

        resizingAnchorView.apply {
            visibility = if(haveFlags(FLAG_RESIZABLE)) View.VISIBLE else View.GONE
            layoutParams.apply {
                width = getActionBarHeight()
                height = getActionBarHeight()
            }
            setBackgroundDrawable(
                GradientDrawable(
                    GradientDrawable.Orientation.BR_TL,
                    intArrayOf(Color.WHITE, Color.BLACK)
                ).apply {
                    cornerRadii = floatArrayOf(90f, 90f, 20f, 20f, 20f, 20f, 20f, 20f)
                    setStroke(5, Color.WHITE, 8f, 10f)
                    elevation = 15f
                })
        }

        containerView.apply {
            setBackgroundDrawable(GradientDrawable().apply {
                setColor(borderColor!!)
                cornerRadius = 20f
            })

            elevation = 15f
        }

        miniView.setBackgroundDrawable(GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = 900f
        })

        width = width
        height = height
    }

    fun getBitmap(_target: Int = TARGET_AUTO): Bitmap {
        val target = if (_target == TARGET_AUTO) (if (haveFlags(FLAG_NO_ACTIONBAR) && haveFlags(FLAG_NO_BORDER)) TARGET_CONTENT else TARGET_ALL) else _target

        val returnedBitmap = Bitmap.createBitmap(floatingView.width, floatingView.height, Bitmap.Config.ARGB_8888)

        Canvas(returnedBitmap).apply {
            if (isExists() && mode != MODE_MINIMIZE) {
                if (target == TARGET_ALL) {
                    view.background?.draw(this)
                    view.draw(this)
                } else {
                    contentView.background?.draw(this)
                    contentView.draw(this)
                }
            }
            else drawColor(Color.WHITE)
        }

        return returnedBitmap
    }

    protected open fun _onStartDragging(element: Int, touchX: Float, touchY: Float) {
        if (element == ELEMENT_WINDOW && this.haveFlags(FLAG_NOT_UNFOCUS_BY_REPLACE_ON_IMAGE)) focus()
        else if (element == ELEMENT_MINI_WINDOW) {
            miniFloatingView.apply {
                width -= 10
                height -= 10
                x += 5
                y += 5
            }
        }
    }

    private fun onStartDragging(element: Int, touchX: Float, touchY: Float) {
        _onStartDragging(element, touchX, touchY)
    }

    protected open fun _onStopDragging(element: Int, touchX: Float, touchY: Float) {
        if (element == ELEMENT_MINI_WINDOW) {
            miniFloatingView.apply {
                width += 10
                height += 10
            }
        }
    }

    private fun onStopDragging(element: Int, touchX: Float, touchY: Float) {
        _onStopDragging(element, touchX, touchY)
    }

    fun isFocused(): Boolean {
        return _isFocused
    }

    protected open fun _focus() {
        Manager._isFocused(this)

        _isFocused = true

        floatingView.create()
        fakeFloatingView.destroy()
    }

    fun focus() {
        if (mode != MODE_MINIMIZE) {
            _focus()
            eventListener?.onFocus(this)
        }
    }

    protected open fun _unfocus() {
        if (!haveFlags(FLAG_NOT_UNFOCUS_BY_REPLACE_ON_IMAGE)) {
            fakeView.setImageBitmap(getBitmap(TARGET_ALL))

            fakeFloatingView.create()
            floatingView.destroy()
        }

        _isFocused = false
    }

    fun unfocus() {
        if (mode != MODE_MINIMIZE) {
            _unfocus()
            eventListener?.onUnfocus(this)
        }
    }

    protected open fun _open() {
        if (!isExists() && isHavePermissions()) {
            Manager._init(context)

            focus()

            applyChanges()
            floatingView.create()

            minimizeTEMP = false

            if (!haveFlags(FLAG_NOT_OPENING_ANIMATION)) {
                containerView.startAnimation(positiveScaleAnimation)

                if (haveFlags(FLAG_RESIZABLE))
                    resizingAnchorView.startAnimation(positiveScaleAnimation)
            }

            _isExists = true
        }
    }

    fun open() {
        _open()

        eventListener?.onOpen(this)
    }

    protected open fun _close() {
        if (isExists()) {
            _isExists = false
            _isFocused = false

            Manager._remove(this@Window)

            floatingView.destroy()
            miniFloatingView.destroy()
            fakeFloatingView.destroy()

            if (mode == MODE_MINIMIZE) mode = valuesTEMP[4]
        }
    }

    fun close() {
        _close()

        eventListener?.onClose(this)
    }

    protected open fun _mainLoop() {
        if (mode == MODE_MAXIMIZE) applyChanges()
        else {
            width = width
            height = height
            x = x
            y = y
        }
    }

    fun mainLoop() {
        _mainLoop()

        eventListener?.onMainLoop(this)
    }

    override fun toString(): String {
        return "${javaClass.name}{title=$title, isFocused=${isFocused()}}"
    }
}
