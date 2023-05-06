package tel.jeelpa.saipose.extractor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tel.jeelpa.saipose.reference.FileUrl
import tel.jeelpa.saipose.reference.Video
import tel.jeelpa.saipose.reference.VideoContainer
import tel.jeelpa.saipose.reference.VideoExtractor
import tel.jeelpa.saipose.reference.VideoServer
import tel.jeelpa.saipose.reference.VideoType
import tel.jeelpa.saipose.reference.client
import tel.jeelpa.saipose.reference.defaultHeaders
import tel.jeelpa.saipose.reference.findBetween

class StreamSB : VideoExtractor() {
    override suspend fun extract(server: VideoServer): VideoContainer {
        val videos = mutableListOf<Video>()
        val id = server.embed.url.let { it.findBetween("/e/", ".html") ?: it.split("/e/")[1] }
        val source = client.get("https://raw.githubusercontent.com/saikou-app/mal-id-filler-list/main/sb.txt").text
        val jsonLink =
            "$source/${bytesToHex("||$id||||streamsb".toByteArray())}/"
        val json = client.get(jsonLink, mapOf("watchsb" to "sbstream")).parsed<Response>()
        if (json.statusCode == 200) {
            videos.add(Video(null, VideoType.M3U8, FileUrl(json.streamData!!.file, defaultHeaders)))
        }
        return VideoContainer(videos)
    }

    private val hexArray = "0123456789ABCDEF".toCharArray()

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF

            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    @Serializable
    private data class Response(
        @SerialName("stream_data")
        val streamData: StreamData? = null,
        @SerialName("status_code")
        val statusCode: Int? = null
    )

    @Serializable
    private data class StreamData(
        @SerialName("file") val file: String
    )
}