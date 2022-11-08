package net.clara.it.picture_in_picture

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import net.clara.it.picture_in_picture.ui.theme.PicturepiTheme

class MainActivity : ComponentActivity() {

    val videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

    class MyReciever: BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            Log.i("BroadcastReceiver", "test")
        }
    }


    private val isPipSupported by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        } else false
    }

    private var videoViewBounds = Rect()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PicturepiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidView(
                        factory =  {
                               VideoView(it, null).apply {
                                   setMediaController(MediaController(it))
                                   setVideoPath(videoUrl)
                                   start()
                               }
                        },
                        modifier = Modifier.fillMaxSize()
                            .onGloballyPositioned {
                                videoViewBounds = it.boundsInWindow().toAndroidRect()
                            }
                    )
                }
            }
        }
    }

    private fun updatePipParams(): PictureInPictureParams? {
        return if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            PictureInPictureParams.Builder()
                .setSourceRectHint(videoViewBounds)
                .setAspectRatio(Rational(16,9))
                .setActions(listOf(
                    RemoteAction(
                        Icon.createWithResource(applicationContext, R.drawable.ic_pause),
                        "pause",
                        "pause playback",
                        PendingIntent.getBroadcast(
                            applicationContext,
                            0,
                            Intent(applicationContext,MyReciever::class.java),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                ))
                .build()
        } else {
            return null
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isPipSupported) return

        updatePipParams().let { params ->
            if ( params != null )
                enterPictureInPictureMode(params)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PicturepiTheme {

    }
}