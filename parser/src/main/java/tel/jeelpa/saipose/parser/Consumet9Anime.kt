package tel.jeelpa.saipose.parser


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tel.jeelpa.saipose.reference.AnimeParser
import tel.jeelpa.saipose.reference.Episode
import tel.jeelpa.saipose.reference.FileUrl
import tel.jeelpa.saipose.reference.ShowResponse
import tel.jeelpa.saipose.reference.Video
import tel.jeelpa.saipose.reference.VideoContainer
import tel.jeelpa.saipose.reference.VideoExtractor
import tel.jeelpa.saipose.reference.VideoServer
import tel.jeelpa.saipose.reference.VideoType
import tel.jeelpa.saipose.reference.client
import tel.jeelpa.saipose.reference.getSize

class Consumet9Anime : AnimeParser() {

    override val name = "Consumet 9Anime"
    override val saveName = "consumet_9anime"
    override val hostUrl = "https://api.consumet.org/anime/9anime"
    override val isDubAvailableSeparately = true

    override suspend fun search(query: String): List<ShowResponse> {
        return client.get("$hostUrl/$query").parsed<SearchResponse>().results.map {
            ShowResponse(it.title, it.id, it.image)
        }
    }

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?): List<Episode> {
        return client.get("$hostUrl/info/$animeLink").parsed<InfoResponse>().episodes.mapNotNull {
            Episode(
                it.number.toString(),
                "$hostUrl/watch/${if (selectDub) it.dubID ?: return@mapNotNull null else it.id}",
                it.title,
                isFiller = it.isFiller
            )
        }
    }

    override suspend fun loadVideoServers(episodeLink: String, extra: Map<String, String>?): List<VideoServer> {
        return listOf(
            VideoServer("vizcloud", episodeLink),
            VideoServer("filemoon", episodeLink),
            VideoServer("streamtape", episodeLink),
        )
    }

    override suspend fun extractVideo(server: VideoServer): VideoContainer = extractorInstance.extract(server)

    private val extractorInstance = Consumet9AnimeExtractor()

    class Consumet9AnimeExtractor : VideoExtractor() {
        override suspend fun extract(server: VideoServer): VideoContainer {
            val res = client.get("${server.embed.url}?server=${server.name}").parsed<EpisodeResponse>()
            res.sources ?: throw Exception(res.message!!)

            return VideoContainer(res.sources.map {
                //Change streamtape url, cuz its blocked on my ip :verycool:
                val url = it.url.takeIf { server.name!="streamtape" } ?: it.url.replace("tape.com","adblocker.xyz")

                val link = FileUrl(url, res.headers ?: mapOf())
                Video(
                    null,
                    if (it.isM3U8) VideoType.M3U8 else VideoType.CONTAINER,
                    link,
                    if (!it.isM3U8) getSize(link) else null,
                    it.quality
                )
            })
        }
    }

    @Serializable
    data class SearchResponse(
        val results: List<Result>
    ) {
        @Serializable
        data class Result(
            val id: String,
            val title: String,
            val image: String
        )
    }

    @Serializable
    data class InfoResponse(
        val episodes: List<Episode>
    ) {
        @Serializable
        data class Episode(
            val id: String,
            @SerialName("dubId")
            val dubID: String? = null,
            val title: String? = null,
            val number: Long,
            val isFiller: Boolean
        )
    }

    @Serializable
    data class EpisodeResponse(
        val message: String? = null,
        val headers: Map<String, String>? = null,
        val sources: List<Source>? = null,
    ) {
        @Serializable
        data class Source(
            val url: String,
            val quality: String? = null,
            val isM3U8: Boolean
        )
    }
}