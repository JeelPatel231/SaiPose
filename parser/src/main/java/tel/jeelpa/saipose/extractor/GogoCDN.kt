package tel.jeelpa.saipose.extractor

import android.util.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tel.jeelpa.saipose.reference.FileUrl
import tel.jeelpa.saipose.reference.Mapper
import tel.jeelpa.saipose.reference.Uri
import tel.jeelpa.saipose.reference.Video
import tel.jeelpa.saipose.reference.VideoContainer
import tel.jeelpa.saipose.reference.VideoExtractor
import tel.jeelpa.saipose.reference.VideoServer
import tel.jeelpa.saipose.reference.VideoType
import tel.jeelpa.saipose.reference.asyncMap
import tel.jeelpa.saipose.reference.client
import tel.jeelpa.saipose.reference.findBetween
import tel.jeelpa.saipose.reference.getSize
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class GogoCDN : VideoExtractor() {

    override suspend fun extract(server : VideoServer): VideoContainer {

        val list = mutableListOf<Video>()

        val url = server.embed.url
        val host = server.embed.headers["referer"]

        val response = client.get(url).document

        if (url.contains("streaming.php")) {

            val keys = keysAndIv

            val it = response.select("script[data-name=\"episode\"]").attr("data-value")

            val decrypted = cryptoHandler(it, keys.key, keys.iv, false)!!.replace("\t", "")
            val id = decrypted.findBetween("", "&")!!
            val end = decrypted.substringAfter(id)

            val encryptedId = cryptoHandler(id, keys.key, keys.iv, true)
            val encryptedUrl = "https://${Uri(url).host}/encrypt-ajax.php?id=$encryptedId$end&alias=$id"

            val encrypted = client.get(encryptedUrl, mapOf("X-Requested-With" to "XMLHttpRequest"), host)
                .text.findBetween("""{"data":"""", "\"}")!!

            val jumbledJson = cryptoHandler(encrypted, keys.secondKey, keys.iv, false)!!
                .replace("""o"<P{#meme":""", """e":[{"file":""")

            val json =
                Mapper.parse<SourceResponse>(jumbledJson.dropLast(jumbledJson.length - jumbledJson.lastIndexOf('}') - 1))

            suspend fun add(i: SourceResponse.Source, backup: Boolean) {
                val label = i.label?.lowercase() ?: return
                val fileURL = FileUrl(i.file ?: return, mapOf("referer" to url))

                if (label != "auto p" && label != "hls p") {
                    list.add(
                        Video(
                            label.replace(" ", "").replace("p", "").toIntOrNull(),
                            VideoType.CONTAINER,
                            fileURL,
                            if (!backup) getSize(fileURL) else null,
                            if (backup) "Backup" else null
                        )
                    )
                } else list.add(
                    Video(null, VideoType.M3U8, fileURL, null, if (backup) "Backup" else null)
                )
            }

            json.source?.asyncMap { i ->
                add(i, false)
            }
            json.sourceBk?.asyncMap { i ->
                add(i, true)
            }
        } else if (url.contains("embedplus")) {
            val file = (response.toString().findBetween("sources:[{file: '", "',"))
            if (file != null) {
                if (
                    try {
                        client.head(file);true
                    } catch (e: Exception) {
                        false
                    }
                ) {
                    list.add(
                        Video(null, VideoType.M3U8, file)
                    )
                }
            }
        }
        return VideoContainer(list)
    }

    companion object {

        private val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        private var keysAndIv: Keys =
            Keys("37911490979715163134003223491201", "54674138327930866480207815084989", "3134003223491201")

        private fun cryptoHandler(string: String, key: String, iv: String, encrypt: Boolean = true): String? {
            return if (!encrypt) {
                cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), IvParameterSpec(iv.toByteArray()))
                String(cipher.doFinal(Base64.decode(string, Base64.NO_WRAP)))
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), IvParameterSpec(iv.toByteArray()))
                Base64.encodeToString(cipher.doFinal(string.toByteArray()), Base64.NO_WRAP)
            }
        }

        private data class Keys(
            val key: String,
            val secondKey: String,
            val iv: String
        )
    }

    @Serializable
    private data class SourceResponse(
        @SerialName("source") val source: List<Source>? = null,
        @SerialName("source_bk") val sourceBk: List<Source>? = null
    ) {
        @Serializable
        data class Source(
            @SerialName("file") val file: String? = null,
            @SerialName("label") val label: String? = null,
            @SerialName("type") val type: String? = null
        )
    }
}