package tel.jeelpa.saipose.data.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.BaseMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import okhttp3.OkHttpClient
import tel.jeelpa.saipose.dependency.VideoCache
import tel.jeelpa.saipose.domain.remote.AnimeClient
import tel.jeelpa.saipose.domain.remote.AnimeDetails
import tel.jeelpa.saipose.reference.Episode
import tel.jeelpa.saipose.reference.ParserMap
import tel.jeelpa.saipose.reference.Video
import tel.jeelpa.saipose.reference.VideoContainer
import tel.jeelpa.saipose.reference.VideoType
import tel.jeelpa.saipose.reference.asyncMap
import tel.jeelpa.saipose.reference.defaultHeaders
import tel.jeelpa.saipose.utils.DataLoadState
import tel.jeelpa.saipose.utils.LoadState
import tel.jeelpa.saipose.utils.fill
import tel.jeelpa.saipose.utils.printAsCollection
import tel.jeelpa.saipose.utils.withState
import javax.inject.Inject


@HiltViewModel
class AnimeDetailsViewModel @Inject constructor(
    private val animeClient: AnimeClient,
    private val applicationContext: Application,
    private val okHttpSourceFactory: OkHttpClient,
): ViewModel() {

    // intended to not start scraping when composable is remade when coming back from exoplayer
    private var alreadyLoaded = false

    // the video to be played in exoplayer, only used in ExoplayerView
//    var currentMediaSource by mutableStateOf<BaseMediaSource?>(null)

    // current parser key, to be used in anime details episode selection
    // and exoplayer bottom sheet to scrape next/prev episodes
    private var selectedParser = ParserMap.keys.first()
        set(newValue) {
            alreadyLoaded = false
            field = newValue
        }

    private val selectedParserInstance
        get() = ParserMap[selectedParser]!!

    // current anime details from anilist API, only used in animeDetailsView
    val animeDetails: DataLoadState<AnimeDetails?> = DataLoadState(null)

    // all episode links from current service, used in both views
    val episodeLinks = mutableStateListOf<Episode>().withState()


    // extracts episode's raw links, the function to be used in BottomSheet in both views.
    // output goes to exoplayer to play
    fun extractEpisode(episodeIndex: Int): Flow<Pair<String,VideoContainer>?> = channelFlow {
        val episode = episodeLinks.data[episodeIndex]
        val videoServer = selectedParserInstance.loadVideoServers(episode.link, null)
        if (videoServer.isEmpty()) {
            send(null)
            close()
            return@channelFlow
        }

        // parallely scrape all the videoServers and send raw links to the channel asynchronously
        videoServer.asyncMap { vidServer ->
            val extractedVideo = selectedParserInstance.extractVideo(vidServer)
            // send the video if its not null
            extractedVideo?.let{ send(Pair(vidServer.name,it)) }
        }

        send(null)
        close()
    }

    // loading the details about the anime from anilist
    // the episode one is paired in this for prototype, extract it in a new functoin
    suspend fun loadData(id: Int) = coroutineScope {
        if (alreadyLoaded) return@coroutineScope
        alreadyLoaded = true

        animeDetails.fillData(animeClient.getAnimeDetails(id))

        // this will be entirely decoupled from anilist because the count depends constructor the service we are scraping
        // the service might be up to date or couple episodes behind or simply doesnt have the anime hosted
        if(animeDetails.loadState == LoadState.Success) {
            val searchResults = selectedParserInstance.search(animeDetails.data!!.title).printAsCollection("Search Results : ")
            if (searchResults.isEmpty()) return@coroutineScope

            episodeLinks.fill { selectedParserInstance.loadEpisodes(searchResults.first().link, null).printAsCollection("EPISODES IN ANIME : ") }
        } else {
            episodeLinks.loadState = animeDetails.loadState
        }
    }


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun makeMediaSourceFromVideo(video: Video): BaseMediaSource {
        val simpleCache = VideoCache.getInstance(applicationContext)

        val dataSourceFactory = DataSource.Factory {
            val dataSource: HttpDataSource = OkHttpDataSource.Factory(okHttpSourceFactory).createDataSource()
            defaultHeaders.forEach {
                dataSource.setRequestProperty(it.key, it.value)
            }
            video.url.headers.forEach {
                dataSource.setRequestProperty(it.key, it.value)
            }
            dataSource
        }

        val cacheFactory = CacheDataSource.Factory().apply {
            setCache(simpleCache)
            setUpstreamDataSourceFactory(dataSourceFactory)
        }

        val mediaItemBuilder = MediaItem.Builder().setUri(video.url.url)

        val mediaSource = when(video.format){
            VideoType.M3U8 -> {
                val mediaItem = mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8).build()
                HlsMediaSource.Factory(cacheFactory).createMediaSource(mediaItem)
            }
            VideoType.DASH -> {
                val mediaItem = mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_MPD).build()
                DashMediaSource.Factory(cacheFactory).createMediaSource(mediaItem)
            }
            // mp4 just works with progressive containers
            else -> {
                val mediaItem = mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_MP4).build()
                ProgressiveMediaSource.Factory(cacheFactory).createMediaSource(mediaItem)
            }
        }

        return mediaSource
    }

}
