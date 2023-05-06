package tel.jeelpa.saipose.extractor

import tel.jeelpa.saipose.reference.FileUrl
import tel.jeelpa.saipose.reference.Video
import tel.jeelpa.saipose.reference.VideoContainer
import tel.jeelpa.saipose.reference.VideoExtractor
import tel.jeelpa.saipose.reference.VideoServer
import tel.jeelpa.saipose.reference.VideoType
import tel.jeelpa.saipose.reference.client
import tel.jeelpa.saipose.reference.getSize

class StreamTape : VideoExtractor() {
    private val linkRegex = Regex("""'robotlink'\)\.innerHTML = '(.+?)'\+ \('(.+?)'\)""")

    override suspend fun extract(server: VideoServer): VideoContainer {
        val reg = linkRegex.find(client.get(server.embed.url.replace("tape.com","adblocker.xyz")).text)?:return VideoContainer(listOf())
        val extractedUrl = FileUrl("https:${reg.groups[1]!!.value + reg.groups[2]!!.value.substring(3)}")
        return VideoContainer(listOf(Video(null, VideoType.CONTAINER, extractedUrl, getSize(extractedUrl))))
    }
}