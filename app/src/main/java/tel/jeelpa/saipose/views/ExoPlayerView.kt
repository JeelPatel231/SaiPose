package tel.jeelpa.saipose.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.util.Rational
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.BaseMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import tel.jeelpa.saipose.data.viewmodels.AnimeDetailsViewModel
import tel.jeelpa.saipose.reference.Video
import javax.inject.Inject

/*
* DO NOT OVERCOMPLICATE THINGS
*
* this view model is only used to supply the exoplayer functions
* and the SINGLE EXOPLAYER INSTANCE INJECTED BY HILT
*
* only add/remove the UTILS used by exoplayer from the view,
* the data sharing is to be done by AnimeDetailsViewModel
*
* AnimeDetailsViewModel is the single source of truth for anime related details
* i.e list of episodes, current episode, scraped video links and mediaSource provided to exoplayer
*
* -----
* if there is any better way to pass the exoplayer instance directly to the view
* without using this intermediate viewmodel (hilt cannot inject dependencies in functions)
* please let me know.
* no i wont add the exoplayer instance to the anime viewmodel and pass that one viewmodel
* to both animeDetailsView and exoplayerView, i would like to keep things modular and different
* */

@HiltViewModel
class ExoPlayerViewModel @Inject constructor(
    val exoplayer: ExoPlayer,
) : ViewModel() {

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun playNewMediaSource(mediaSource: BaseMediaSource){
        exoplayer.stop()
        exoplayer.setMediaSource(mediaSource)
        exoplayer.prepare()
        exoplayer.play()
    }
}

@Composable
@AnimeNavGraph
@Destination
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun ExoPlayerViewContainer(
    // initial variables when moving from animeDetails to Exoplayer view
    currentEpisodeIndex: Int,
    // initial videoSource to directly start playing the video
    videoSource: Video,

    animeDetailsViewModel: AnimeDetailsViewModel,
    exoplayerViewModel: ExoPlayerViewModel = hiltViewModel(),
){
    // apply the source factory stuff for headers and info from FileURL here
    // get the start position from db if stored from previous play
    val startPosition = 0L
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var episodeTobeScraped : Int by rememberSaveable { mutableStateOf(currentEpisodeIndex) }

    if(showBottomSheet){
        AnimeSourceSelector(
            onDismissRequest = { showBottomSheet = false },
            onLinkClick = {
                exoplayerViewModel.playNewMediaSource(
                    animeDetailsViewModel.makeMediaSourceFromVideo(it)
                )
                showBottomSheet = false
            },
            listOfElements = animeDetailsViewModel.extractEpisode(episodeTobeScraped)
        )
    }

    val containerContext = LocalContext.current

    MyExoPlayerViewStateless(
        exoplayer = exoplayerViewModel.exoplayer,
        mediaSource = animeDetailsViewModel.makeMediaSourceFromVideo(videoSource),
        startPosition = startPosition,
        onNextClick = {
            val newEpisodeIndex = (episodeTobeScraped+1).coerceAtMost(animeDetailsViewModel.episodeLinks.data.size-1)
            if (newEpisodeIndex == episodeTobeScraped){
                val error = "No Previous Episode, this is the first one"
                Toast.makeText(containerContext, error,Toast.LENGTH_SHORT).show()
            } else {
                episodeTobeScraped = newEpisodeIndex
                showBottomSheet = true
            }
        },
        onPrevClick = {
            val newEpisodeIndex = (episodeTobeScraped-1).coerceAtLeast(0)
            if(newEpisodeIndex == episodeTobeScraped) {
                val error = "No Previous Episode, this is the first one"
                Toast.makeText(containerContext, error,Toast.LENGTH_SHORT).show()
            } else {
                episodeTobeScraped = newEpisodeIndex
                showBottomSheet = true
            }
        },
    )

}


// its not 100% stateless yet, TODO : try to make it stateless
@SuppressLint("NewApi")
@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun MyExoPlayerViewStateless (
    exoplayer: ExoPlayer,
    mediaSource: MediaSource,
    startPosition: Long, // get from db of how much the ep was completed,
    onNextClick: (View) -> Unit,
    onPrevClick: (View) -> Unit,
) {
    val activity = LocalContext.current as Activity

    var lifecycle by remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, evt ->
            lifecycle = evt
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // don't wait for android view to recompose on exiting, just exit
    if(lifecycle == Lifecycle.Event.ON_STOP){
        return
    }

    // lock screen orientation to landscape
    if(lifecycle != Lifecycle.Event.ON_STOP) {
        GoFullscreen()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PlayerView(context).apply {
                player = exoplayer
                // Previous Button
                findViewById<ImageButton>(androidx.media3.ui.R.id.exo_prev).apply {
                    setOnClickListener(onPrevClick)
                }
                // Next Button
                findViewById<ImageButton>(androidx.media3.ui.R.id.exo_next).apply {
                    setOnClickListener(onNextClick)
                }
            }
        },
        update = { view ->
//            Toast.makeText(activity, "CHANGED : $lifecycle", Toast.LENGTH_SHORT).show()
            when(lifecycle){
                // PiP window is closed or view is stopped
                Lifecycle.Event.ON_STOP -> exoplayer.pause()
                // from PiP or BG to android activity
                Lifecycle.Event.ON_RESUME -> view.useController = true
                // from android activity to BG or PiP
                Lifecycle.Event.ON_PAUSE -> view.useController = false

                // start
                Lifecycle.Event.ON_CREATE -> {
                    exoplayer.setMediaSource(mediaSource)
                    exoplayer.seekTo(startPosition)
                    exoplayer.prepare()
                    exoplayer.play()
                }

                else -> { /* DO NOTHING */ }
            }

            view.setFullscreenButtonClickListener {
                val pipParams = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16,9))
                    .build()
                activity.enterPictureInPictureMode(pipParams)
            }
        }
    )
}

@Composable
fun GoFullscreen() {
    val activity = LocalContext.current as Activity
    val orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    DisposableEffect(orientation) {
        val originalOrientation = activity.requestedOrientation
        val windowInsetController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)

        // orientation stuff
        activity.requestedOrientation = orientation

        // fullscreen stuff
        windowInsetController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation

            // restore fullscreen stuff
            windowInsetController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            windowInsetController.show(WindowInsetsCompat.Type.systemBars())

        }
    }
}
