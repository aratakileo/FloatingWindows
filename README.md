# FloatingWindows
Floating windows for Android

### New API Version
<a href="https://github.com/teacondemns/AndroidFloatingWindowsEngine">
  <img align="" src="https://github-readme-stats.vercel.app/api/pin/?username=teacondemns&repo=AndroidFloatingWindowsEngine&theme=github_dark&ver=2" />
</a>

### Navigation
- [Download](#download)
- [When were last source code changes](#when-were-last-source-code-changes)

## How to use
Windows:
- [Window](#window)
- [DynamicWindow](#dynamicwindow)
- [VideoWindow](#videowindow)

Other:
- [FloatingView](#floatingview)

### Window
Initialize in `MainActivity.kt`
```kotlin
package com.pexty.studios.floating.windows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pexty.floatingapp.window.Window
import pexty.floatingapp.window.Manager

class MainActivity : AppCompatActivity() {
    lateinit var window: Window
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        window = Window(this, 500, 700, "Window title") //500, 700 - minimum window size
    }
    
    override fun onDestroy() {
        Manager.destroy()
    
        super.onDestroy()
    }
}
```
Window existence
```kotlin
window.isExists() //Get window existence
window.open() //Open window
window.close() //Close window
```
Window focus
```kotlin
window.isFocused() //Get window focusing
window.focus() //Focus window
window.unfocus() //Unfocus window
```
Flags
```kotlin
window.flags //Flags parameter (default value 0)
window.haveFlags(Window.FLAG_NO_ACTIONBAR)
window.addFlags(Window.FLAG_NO_ACTIONBAR)
window.removeFlags(Window.FLAG_NO_ACTIONBAR)
window.clearFlags()

/*
*
* You can set flags in initialization
*
Window(this, 500, 700, "Window title", Window.FLAG_NO_ACTIONBAR)
*
*/
```
Flags values
```kotlin
FLAG_NO_ICON = 1
FLAG_NO_TITLE = 2
FLAG_NO_MINIMIZE_BUTTON = 4
FLAG_NO_QUIT_BUTTON = 8
FLAG_NO_MAXIMIZE_RESTORE_BUTTON = 16
FLAG_NO_CONTROLS = FLAG_NO_MINIMIZE_BUTTON or FLAG_NO_QUIT_BUTTON or FLAG_NO_MAXIMIZE_RESTORE_BUTTON
FLAG_NO_ACTIONBAR = 32
FLAG_NO_BORDER = 64
FLAG_ONLY_CONTENT = FLAG_NO_ACTIONBAR or FLAG_NO_BORDER
FLAG_RESIZABLE = 128
FLAG_NOT_RESIZABLE_BY_MOTION = 256
FLAG_NOT_DRAGGABLE = 512
FLAG_DRAGGABLE_BY_CONTENT = 1024
FLAG_NOT_UNFOCUS_BY_REPLACE_ON_IMAGE = 2048
FLAG_NOT_FOCUS_FROM_TOUCH = 4096
FLAG_DISABLE_MAIN_LOOP = 8192
FLAG_NOT_OPENING_ANIMATION = 16384
```
### DynamicWindow
Initialize in `MainActivity.kt`
```kotlin
package com.pexty.studios.floating.windows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pexty.floatingapp.window.DynamicWindow
import pexty.floatingapp.window.Manager

class MainActivity : AppCompatActivity() {
    lateinit var window: DynamicWindow
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        window = DynamicWindow(this, 500, 700, "Window title") //500, 700 - minimum window size
    }
    
    override fun onDestroy() {
        Manager.destroy()
    
        super.onDestroy()
    }
}
```
Flags
```kotlin
window.flags //default value FLAG_NOT_UNFOCUS_BY_REPLACE_ON_IMAGE
```
### VideoWindow
Initialize in `MainActivity.kt`
```kotlin
package com.pexty.studios.floating.windows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pexty.floatingapp.window.VideoWindow
import pexty.floatingapp.window.Manager

class MainActivity : AppCompatActivity() {
    lateinit var window: VideoWindow
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        window = VideoWindow(this)
    }
    
    override fun onDestroy() {
        Manager.destroy()
    
        super.onDestroy()
    }
}
```
Flags values
```kotlin
FLAG_INFINITE_VIDEO_LOOP = 32768
```
### FloatingView
Initialize in `MainActivity.kt`
```kotlin
package com.pexty.studios.floating.windows

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import pexty.floatingapp.FloatingView

class MainActivity : AppCompatActivity() {
    lateinit var floatingView: FloatingView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        floatingView = FloatingView(this, findViewById<LinearLayout>(R.id.myLinear), 500, 700) //500, 700 - view size
    }
    
    override fun onDestroy() {
        floatingView.destroy()
    
        super.onDestroy()
    }
}
```
FloatingView existence
```kotlin
floatingView.isExists() //Get floatingView existence
floatingView.create() //Create floatingView
floatingView.destroy() //Destroy floatingView
```
Flags
```kotlin
floatingView.flags //Flags parameter (default value 0)
floatingView.haveFlags(FloatingView.FLAG_DRAGGABLE)
floatingView.addFlags(FloatingView.FLAG_DRAGGABLE)
floatingView.removeFlags(FloatingView.FLAG_DRAGGABLE)
floatingView.clearFlags()

/*
*
* You can set flags in initialization
*
FloatingView(this, findViewById<LinearLayout>(R.id.myLinear), 500, 700, FloatingView.FLAG_DRAGGABLE)
*
*/
```
Flags values
```kotlin
FLAG_DRAGGABLE = 1
FLAG_LAYOUT_NO_LIMITS = 2
FLAG_LAYOUT_IN_SCREEN = 4
```

## Download
[Download test-application](https://cloud.mail.ru/public/PCzA/tFEqetY1e)

## When were last source code changes
`30 June 2021`
