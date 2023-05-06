package tel.jeelpa.saipose.extractor


import android.util.Base64
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import tel.jeelpa.saipose.reference.FileUrl
import tel.jeelpa.saipose.reference.Mapper
import tel.jeelpa.saipose.reference.Video
import tel.jeelpa.saipose.reference.VideoContainer
import tel.jeelpa.saipose.reference.VideoExtractor
import tel.jeelpa.saipose.reference.VideoServer
import tel.jeelpa.saipose.reference.VideoType
import tel.jeelpa.saipose.reference.client
import tel.jeelpa.saipose.reference.findBetween

class PStream : VideoExtractor() {

    val link = "https://www.pstream.net/u/player-script"
    val regex = Regex("""\)\)\}\("(.+)"\),n=.*file:.\.(.+),tracks:""")

    val headers = mapOf(
        "accept" to "*/*",
        "content-type" to "application/json",
        "accept-language" to "*/*"
    )

    override suspend fun extract(server : VideoServer): VideoContainer {
        val res = client.get(server.embed.url, headers).text
        val jslink = res.findBetween("<script src=\"$link", "\" type=") ?: return VideoContainer(listOf())
        val (base64, key) = regex.find(client.get(link + jslink, headers).text)?.destructured!!
        val jsonText = Base64.decode(base64, Base64.NO_WRAP).decodeToString().substring(2)
        val json = Mapper.parse<JsonObject>(jsonText)
        val link = json[key]?.jsonPrimitive?.content ?: return VideoContainer(listOf())
        return VideoContainer(listOf(Video(null, VideoType.M3U8, FileUrl(link, headers))))
    }
}