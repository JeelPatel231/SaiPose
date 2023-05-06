package tel.jeelpa.saipose.parser

import tel.jeelpa.saipose.reference.AnimeParser
import tel.jeelpa.saipose.reference.Episode
import tel.jeelpa.saipose.reference.ExtractorMap
import tel.jeelpa.saipose.reference.FileUrl
import tel.jeelpa.saipose.reference.ShowResponse
import tel.jeelpa.saipose.reference.Uri
import tel.jeelpa.saipose.reference.VideoContainer
import tel.jeelpa.saipose.reference.VideoExtractor
import tel.jeelpa.saipose.reference.VideoServer
import tel.jeelpa.saipose.reference.client

class Gogo : AnimeParser() {
    override val name = "Gogo"
    override val saveName = "gogo_anime_gr"
    override val hostUrl = "https://gogoanime.gr"
    override val malSyncBackupName = "Gogoanime"
    override val isDubAvailableSeparately = true

    override suspend fun loadEpisodes(
        animeLink: String,
        extra: Map<String, String>?
    ): List<Episode> {
        val list = mutableListOf<Episode>()

        val pageBody = client.get("$hostUrl/category/$animeLink").document
        val lastEpisode =
            pageBody.select("ul#episode_page > li:last-child > a").attr("ep_end").toString()
        val animeId = pageBody.select("input#movie_id").attr("value").toString()

        val epList = client
            .get("https://ajax.gogo-load.com/ajax/load-list-episode?ep_start=0&ep_end=$lastEpisode&id=$animeId").document
            .select("ul > li > a").reversed()
        epList.forEach {
            val num = it.select(".name").text().replace("EP", "").trim()
            list.add(Episode(num, hostUrl + it.attr("href").trim()))
        }

        return list
    }

    private fun httpsIfy(text: String): String {
        return if (text.take(2) == "//") "https:$text"
        else text
    }

    override suspend fun loadVideoServers(
        episodeLink: String,
        extra: Map<String, String>?
    ): List<VideoServer> {
        val list = mutableListOf<VideoServer>()
        client.get(episodeLink).document.select("div.anime_muti_link > ul > li").forEach {
            val name = it.select("a").text().replace("Choose this server", "")
            val url = httpsIfy(it.select("a").attr("data-video"))
            val embed = FileUrl(url, mapOf("referer" to hostUrl))

            list.add(VideoServer(name, embed))
        }
        return list
    }

    override suspend fun extractVideo(server: VideoServer): VideoContainer? {
        val domain = Uri(server.embed.url).host ?: return null
        val extractor: VideoExtractor? = when {
            "gogo" in domain -> ExtractorMap["GogoCDN"]
            "goload" in domain -> ExtractorMap["GogoCDN"]
            "playgo" in domain -> ExtractorMap["GogoCDN"]
            "anihdplay" in domain -> ExtractorMap["GogoCDN"]
            "playtaku" in domain -> ExtractorMap["GogoCDN"]
            "sb" in domain -> ExtractorMap["StreamSB"]
            "sss" in domain -> ExtractorMap["StreamSB"]
            "fplayer" in domain -> ExtractorMap["FPlayer"]
            "fembed" in domain -> ExtractorMap["FPlayer"]
            else -> null
        }
        return extractor?.extract(server)
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val encoded = encode(query + if (selectDub) " (Dub)" else "")
        val list = mutableListOf<ShowResponse>()
        client.get("$hostUrl/search.html?keyword=$encoded").document
            .select(".last_episodes > ul > li div.img > a").forEach {
                val link = it.attr("href").toString().replace("/category/", "")
                val title = it.attr("title")
                val cover = it.select("img").attr("src")
                list.add(ShowResponse(title, link, cover))
            }
        return list
    }
}