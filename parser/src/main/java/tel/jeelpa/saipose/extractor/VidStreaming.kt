package tel.jeelpa.saipose.extractor

import tel.jeelpa.saipose.reference.ExtractorMap
import tel.jeelpa.saipose.reference.VideoContainer
import tel.jeelpa.saipose.reference.VideoExtractor
import tel.jeelpa.saipose.reference.VideoServer
import tel.jeelpa.saipose.reference.asyncMapNotNull
import tel.jeelpa.saipose.reference.client


class VidStreaming : VideoExtractor() {
    override suspend fun extract(server: VideoServer): VideoContainer {
        // `streaming.php` is the most likely one to be replaced
        // however sometimes the source will be using `embed` or `load`
        val url =
            server.embed.url
                .replace("streaming.php?", "loadserver.php?")
                .replace("embed.php", "loadserver.php")
                .replace("load.php", "loadserver.php")

        val res = client
            .get(url, mapOf("Referer" to "https://goload.one"))
            .document.select("ul.list-server-items > li.linkserver")

        return VideoContainer(
            res.asyncMapNotNull { server ->
                val src = server.attr("data-video") // link to the source
                val type = server.text().lowercase() // e.g streamsb, vidstreaming, multi quality

                // Using a when makes it easier to expand upon later
                return@asyncMapNotNull when (type) {
                    // TODO, make it not nullable
                    "streamsb" -> ExtractorMap["StreamSB"]?.extract(VideoServer(name="StreamSB", embedUrl = src))?.videos
                    "xstreamcdn" -> ExtractorMap["FPlayer"]?.extract(VideoServer(name = "XStreamCDN", embedUrl = src))?.videos
                    else -> null
                }
            }.flatten()
        )
    }
}