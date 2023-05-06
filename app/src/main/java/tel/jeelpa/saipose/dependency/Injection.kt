package tel.jeelpa.saipose.dependency



import android.app.Application
import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.room.Room
import com.apollographql.apollo3.ApolloClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.brahmkshatriya.nicehttp.ignoreAllSSLErrors
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import tel.jeelpa.saipose.data.AnilistAnimeClientImpl
import tel.jeelpa.saipose.data.AnilistMangaClientImpl
import tel.jeelpa.saipose.data.database.AppDatabase
import tel.jeelpa.saipose.data.database.Plugin
import tel.jeelpa.saipose.data.database.PluginType
import tel.jeelpa.saipose.domain.remote.AnimeClient
import tel.jeelpa.saipose.domain.remote.MangaClient
import tel.jeelpa.saipose.reference.AnimeParser
import tel.jeelpa.saipose.reference.VideoExtractor
import tel.jeelpa.saipose.utils.associateNotNull
import tel.jeelpa.saipose.utils.loadPluginSafe
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@UnstableApi
object VideoCache {
    private var simpleCache: SimpleCache? = null
    fun getInstance(context: Context): SimpleCache {
        val databaseProvider = StandaloneDatabaseProvider(context)
        if (simpleCache == null)
            simpleCache = SimpleCache(
                File(context.cacheDir, "exoplayer").also { it.deleteOnExit() }, // Ensures always fresh file
                LeastRecentlyUsedCacheEvictor(300L * 1024L * 1024L),
                databaseProvider
            )
        return simpleCache as SimpleCache
    }

    fun release() {
        simpleCache?.release()
        simpleCache = null
    }
}

@Module
@InstallIn(SingletonComponent::class)
object Injection {

    @Provides
    @Singleton
    fun provideExoPlayer(app: Application) : ExoPlayer {
        val exoplayer = ExoPlayer.Builder(app)
            .build().apply {
                playWhenReady = true
            }

        // TODO : study this and check how to use intended way
        val session = MediaSession.Builder(app.applicationContext, exoplayer).build()
        val controller = MediaController.Builder(app.applicationContext, session.token).buildAsync().get()

        return exoplayer
    }

    @Provides
    @Singleton
    fun providesExoplayerOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            ignoreAllSSLErrors()
            followRedirects(true)
            followSslRedirects(true)
        }.build()
    }

    @Provides
    @Singleton
    fun providesApolloClient(): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://graphql.anilist.co")
            .build()
    }

    @Provides
    @Singleton
    fun providesAnimeClient(apolloClient: ApolloClient) : AnimeClient {
        return AnilistAnimeClientImpl(apolloClient)
    }

    @Provides
    @Singleton
    fun providesMangaClient(apolloClient: ApolloClient) : MangaClient {
        return AnilistMangaClientImpl(apolloClient)
    }

    @Provides
    @Singleton
    fun provideDatabase(app:Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "saiposedb"
        ).build()
    }

    data class ParserProxy(val data: Map<String, AnimeParser>)
    data class ExtractorProxy(val data: Map<String, VideoExtractor>)

    @Provides
    @Singleton
    @Named("Extractors")
    fun providesExtractorsPlugin(app: Application, db:AppDatabase) : ExtractorProxy = runBlocking {
//        val listOfExtractors: List<Plugin> = db.pluginDao().getPluginByType(PluginType.EXTRACTOR)
        val listOfExtractors: List<Plugin> = listOf(
            Plugin("FPlayer","parserdex.jar","tel.jeelpa.saipose.extractor.FPlayer",PluginType.EXTRACTOR),
            Plugin("GogoCDN","parserdex.jar","tel.jeelpa.saipose.extractor.GogoCDN",PluginType.EXTRACTOR),
            Plugin("PStream","parserdex.jar","tel.jeelpa.saipose.extractor.PStream",PluginType.EXTRACTOR),
            Plugin("RapidCloud","parserdex.jar","tel.jeelpa.saipose.extractor.RapidCloud",PluginType.EXTRACTOR),
            Plugin("StreamSB","parserdex.jar","tel.jeelpa.saipose.extractor.StreamSB",PluginType.EXTRACTOR),
            Plugin("StreamTape","parserdex.jar","tel.jeelpa.saipose.extractor.StreamTape",PluginType.EXTRACTOR),
            Plugin("VidStreaming","parserdex.jar","tel.jeelpa.saipose.extractor.VidStreaming",PluginType.EXTRACTOR),
        )

        // Create instances and add them to the map
        return@runBlocking ExtractorProxy(listOfExtractors
            .associateNotNull { it.serviceName to loadPluginSafe<VideoExtractor>(app, it.jarName, it.className)}
        )
    }

    @Provides
    @Singleton
    @Named("Parsers")
    fun providesParsersPlugin(app: Application, db:AppDatabase): ParserProxy = runBlocking {
//        val listOfParsers: List<Plugin> = db.pluginDao().getPluginByType(PluginType.PARSER)
        val listOfParsers: List<Plugin> = listOf(
            Plugin("AnimePahe","parserdex.jar","tel.jeelpa.saipose.parser.AnimePahe",PluginType.PARSER),
            Plugin("Gogo","parserdex.jar","tel.jeelpa.saipose.parser.Gogo",PluginType.PARSER),
            Plugin("AllAnime","parserdex.jar","tel.jeelpa.saipose.parser.AllAnime",PluginType.PARSER),
            Plugin("ConsumeBili","parserdex.jar","tel.jeelpa.saipose.parser.ConsumeBili",PluginType.PARSER),
            Plugin("Consumet9Anime","parserdex.jar","tel.jeelpa.saipose.parser.Consumet9Anime",PluginType.PARSER),
            Plugin("Marin","parserdex.jar","tel.jeelpa.saipose.parser.Marin",PluginType.PARSER),
        )
//         Create instances and add them to the map
        // BAD, refactor
        return@runBlocking ParserProxy(listOfParsers
            .associateNotNull { it.serviceName to loadPluginSafe<AnimeParser>(app, it.jarName, it.className)}
        )
    }
}