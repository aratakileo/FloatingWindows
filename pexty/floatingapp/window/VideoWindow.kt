package pexty.floatingapp.window

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.VideoView
import pexty.utils.Utils


open class VideoWindow : DynamicWindow {
    protected constructor(
        context: Context,
        original: VideoWindow
    ) : super (context, original) {
        setContent(videoView)
    }

    constructor(context: Context, __flags: Int = 0) : super(
        context,
        0,
        0,
        "Video player window",
        FLAG_ONLY_CONTENT or FLAG_RESIZABLE or FLAG_DRAGGABLE_BY_CONTENT or FLAG_NOT_RESIZABLE_BY_MOTION or __flags
    ) {
        setContent(videoView)
    }

    companion object {
        const val FLAG_INFINITE_VIDEO_LOOP = 1 shl 15
    }

    private var mediaMetadataRetriever: MediaMetadataRetriever = MediaMetadataRetriever()
    private var mediaPlayer: MediaPlayer? = null
    private var _videoCurrentPoint = 0
    private var videoStartPoint = 0
    private var _isVideoDidNotPlay = true
    private var _isHaveVideo = false

    private val videoView = VideoView(this.context).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ).apply {
            gravity = Gravity.CENTER
        }

        setOnPreparedListener {
            mediaPlayer = it
        }

        setOnCompletionListener {
            if (haveFlags(FLAG_INFINITE_VIDEO_LOOP)) {
                start()
                videoCurrentPoint = 0
            }
        }
    }

    val videoWidth: Int
        get() = if (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) != null) mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt() else 0

    val videoHeight: Int
        get() = if (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) != null) mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt() else 0

    val videoDuration: Int
        get() = videoView.duration

    var videoCurrentPoint: Int
        get() = _videoCurrentPoint
        set(value) {
            if (isClone) println("HaveVideo: ${isHaveVideo()}")

            if (mediaPlayer != null && !isVideoDidNotPlay() && isHaveVideo()) {
                _videoCurrentPoint = Utils.by_interval(value, 0, videoDuration)
                mediaPlayer?.seekTo(_videoCurrentPoint.toLong(), MediaPlayer.SEEK_CLOSEST)
            } else videoStartPoint = value
        }

    fun isVideoDidNotPlay(): Boolean {
        return _isVideoDidNotPlay
    }

    fun isHaveVideo(): Boolean {
        return _isHaveVideo
    }

    fun isPlaying(): Boolean {
        return videoView.isPlaying
    }

    fun start() {
        if (isHaveVideo()) {
            videoView.start()

            if (isVideoDidNotPlay()) {
                videoCurrentPoint = videoStartPoint
                videoStartPoint = 0
            }

            _isVideoDidNotPlay = false
        }
    }

    fun pause() {
        if (isHaveVideo()) videoView.pause()
    }

    fun stop() {
        if (isHaveVideo()) {
            _videoCurrentPoint = 0
            videoView.stopPlayback()
        }
    }

    fun setVideo(uri: Uri) {
        _isHaveVideo = true
        videoView.setVideoURI(uri)
        mediaMetadataRetriever.setDataSource(context, uri)

        dynamicWindow as VideoWindow
        if (!isClone) dynamicWindow.setVideo(uri)

        if (isExists()) start()
    }

    fun setVideo(resourceId: Int) {
        setVideo(Uri.parse("android.resource://${context.packageName}/$resourceId"))
    }

    fun setSizeByVideoRatio(widthHalf: Int = 2, heightHalf: Int = 4) {
        if (videoWidth > videoHeight) {
            val percent: Float = videoHeight.toFloat() / videoWidth.toFloat()
            val div = if (Utils.getScreenOrientation(context) == Utils.ORIENTATION_LANDSCAPE) heightHalf else widthHalf

            minWidth = displayMetrics.widthPixels / div
            minHeight = (minWidth.toFloat() * percent).toInt()
        } else {
            val percent: Float = videoWidth.toFloat() / videoHeight.toFloat()
            val div = if (Utils.getScreenOrientation(context) == Utils.ORIENTATION_PORTRAIT) heightHalf else widthHalf

            minHeight = displayMetrics.heightPixels / div
            minWidth = (minHeight.toFloat() * percent).toInt()
        }

        width = 0
        height = 0
    }

    override fun createDynamicWindow(): VideoWindow {
        return VideoWindow(context, this)
    }

    override fun _open() {
        super._open()

        if (isExists() && !isPlaying()) start()
    }

    override fun _close() {
        stop()

        super._close()
    }

    override fun _mainLoop() {
        _videoCurrentPoint = videoView.currentPosition

        super._mainLoop()
    }
}