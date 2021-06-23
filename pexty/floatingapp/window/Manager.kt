package pexty.floatingapp.window

import android.content.Context
import android.os.Handler

object Manager {
    private var _isInited = false
    private var windows = ArrayList<Window>()

    private val handler = Handler()
    private lateinit var runnable: Runnable

    fun _init(_context: Context) {
        if (!_isInited) {
            _isInited = true

            runnable = object: Runnable {
                private var context = _context

                override fun run() {
                    windows.forEach {if (!it.haveFlags(Window.FLAG_DISABLE_MAIN_LOOP)) it.mainLoop()}

                    handler.postDelayed(this, 500)
                }
            }

            handler.post(runnable)
        }
    }

    fun _isFocused(window: Window) {
        if (windows.indexOf(window) != -1) windows.remove(window)
        windows.forEach {it.unfocus()}
        windows.add(window)
    }

    fun _unfocusCurrent() {
        if (windows.size > 0) windows[windows.size - 1].unfocus()
    }

    fun _remove(window: Window) {
        if (windows.indexOf(window) != -1) windows.remove(window)
    }

    fun destroy() {
        quitAll()

        if (_isInited) {
            _isInited = false
            handler.removeCallbacks(runnable)
        }
    }

    fun getWindowsArrayList(): ArrayList<Window> {
        return windows
    }

    fun quitAll() {
        for (i in 0 until windows.size) windows[0].close()
    }
}