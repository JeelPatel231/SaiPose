package tel.jeelpa.saipose.extractor

import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tel.jeelpa.saipose.reference.FileUrl
import tel.jeelpa.saipose.reference.Video
import tel.jeelpa.saipose.reference.VideoContainer
import tel.jeelpa.saipose.reference.VideoExtractor
import tel.jeelpa.saipose.reference.VideoServer
import tel.jeelpa.saipose.reference.VideoType
import tel.jeelpa.saipose.reference.client
import tel.jeelpa.saipose.reference.getSize
import java.math.BigInteger


class VideoVardDownload : BaseVideoVard() {
    override suspend fun extract(server: VideoServer): VideoContainer {
        val url = server.embed.url
        val id = url.substringAfter("/e/").substringBefore("/")

        val res = client.get("$mainUrl/api/make/download/$id").parsed<HashResponse>()
        res.hash ?: return VideoContainer(emptyList())

        delay(11_000)
        val setup = client.post(
            "$mainUrl/api/front/download",
            mapOf(
                "Accept" to "*/*",
                "Accept-Encoding" to "identity"
            ),
            data = mapOf(
                "cmd" to "download",
                "file_code" to id,
                "hash" to res.hash,
                "version" to res.version!!,
            )
        ).also { if (!it.text.startsWith("{")) throw Exception("Video Not Found") }
            .parsed<SetupResponse>()
        val mp4 = FileUrl(decode(setup.link!!, setup.seed), headers)
        return VideoContainer(
            listOf(
                Video(null, VideoType.CONTAINER, mp4, getSize(mp4))
            )
        )
    }
}

class VideoVardNoDownload : BaseVideoVard() {
    override suspend fun extract(server: VideoServer): VideoContainer {
        val url = server.embed.url
        val id = url.substringAfter("/e/").substringBefore("/")
        val hash = client.get("$mainUrl/api/make/hash/$id", headers).parsed<HashResponse>()
        hash.hash ?: return VideoContainer(emptyList())

        val res = client.post(
            "$mainUrl/api/player/setup",
            headers,
            data = mapOf(
                "cmd" to "get_stream",
                "file_code" to id,
                "hash" to hash.hash
            )
        ).also { if (!it.text.startsWith("{")) throw Exception("Video Not Found") }
            .parsed<SetupResponse>()
        val m3u8 = FileUrl(decode(res.src!!, res.seed), headers)

        return VideoContainer(listOf(
            Video(null, VideoType.M3U8, m3u8)
        ))
    }
}

abstract class BaseVideoVard : VideoExtractor() {

    protected val mainUrl = "https://videovard.sx"
    protected val headers = mapOf("Referer" to "$mainUrl/")

    companion object {
        protected val big0 = 0.toBigInteger()
        protected val big3 = 3.toBigInteger()
        protected val big4 = 4.toBigInteger()
        protected val big15 = 15.toBigInteger()
        protected val big16 = 16.toBigInteger()
        protected val big255 = 255.toBigInteger()

        @JvmStatic
        protected fun decode(dataFile: String, seed: String): String {
            val dataSeed = replace(seed)
            val newDataSeed = binaryDigest(dataSeed)
            val newDataFile = bytes2blocks(ascii2bytes(dataFile))
            var list = listOf(1633837924, 1650680933).map { it.toBigInteger() }
            val xorList = mutableListOf<BigInteger>()
            for (i in newDataFile.indices step 2) {
                val temp = newDataFile.slice(i..i + 1)
                xorList += xorBlocks(list, tearDecode(temp, newDataSeed))
                list = temp
            }

            val result = replace(unPad(blocks2bytes(xorList)).map { it.toInt().toChar() }.joinToString(""))
            return padLastChars(result)
        }

        protected fun binaryDigest(input: String): List<BigInteger> {
            val keys = listOf(1633837924, 1650680933, 1667523942, 1684366951).map { it.toBigInteger() }
            var list1 = keys.slice(0..1)
            var list2 = list1
            val blocks = bytes2blocks(digestPad(input))

            for (i in blocks.indices step 4) {
                list1 = tearCode(xorBlocks(blocks.slice(i..i + 1), list1), keys).toMutableList()
                list2 = tearCode(xorBlocks(blocks.slice(i + 2..i + 3), list2), keys).toMutableList()

                val temp = list1[0]
                list1[0] = list1[1]
                list1[1] = list2[0]
                list2[0] = list2[1]
                list2[1] = temp
            }

            return listOf(list1[0], list1[1], list2[0], list2[1])
        }

        protected fun tearDecode(a90: List<BigInteger>, a91: List<BigInteger>): MutableList<BigInteger> {
            var (a95, a96) = a90

            var a97 = (-957401312).toBigInteger()
            for (_i in 0 until 32) {
                a96 -= ((((a95 shl 4) xor rShift(a95, 5)) + a95) xor (a97 + a91[rShift(a97, 11).and(3.toBigInteger()).toInt()]))
                a97 += 1640531527.toBigInteger()
                a95 -= ((((a96 shl 4) xor rShift(a96, 5)) + a96) xor (a97 + a91[a97.and(3.toBigInteger()).toInt()]))

            }

            return mutableListOf(a95, a96)
        }

        protected fun digestPad(string: String): List<BigInteger> {
            val empList = mutableListOf<BigInteger>()
            val length = string.length
            val extra = big15 - (length.toBigInteger() % big16)
            empList.add(extra)
            for (i in 0 until length) {
                empList.add(string[i].code.toBigInteger())
            }
            for (i in 0 until extra.toInt()) {
                empList.add(big0)
            }

            return empList
        }

        protected fun bytes2blocks(a22: List<BigInteger>): List<BigInteger> {
            val empList = mutableListOf<BigInteger>()
            val length = a22.size
            var listIndex = 0

            for (i in 0 until length) {
                val subIndex = i % 4
                val shiftedByte = a22[i] shl (3 - subIndex) * 8

                if (subIndex == 0) {
                    empList.add(shiftedByte)
                } else {
                    empList[listIndex] = empList[listIndex] or shiftedByte
                }

                if (subIndex == 3) listIndex += 1
            }

            return empList
        }

        protected fun blocks2bytes(inp: List<BigInteger>): List<BigInteger> {
            val tempList = mutableListOf<BigInteger>()
            inp.indices.forEach { i ->
                tempList += (big255 and rShift(inp[i], 24))
                tempList += (big255 and rShift(inp[i], 16))
                tempList += (big255 and rShift(inp[i], 8))
                tempList += (big255 and inp[i])
            }
            return tempList
        }

        protected fun unPad(a46: List<BigInteger>): List<BigInteger> {
            val evenOdd = a46[0].toInt().mod(2)
            return (1 until (a46.size - evenOdd)).map {
                a46[it]
            }
        }

        protected fun xorBlocks(a76: List<BigInteger>, a77: List<BigInteger>): List<BigInteger> {
            return listOf(a76[0] xor a77[0], a76[1] xor a77[1])
        }

        protected fun rShift(input: BigInteger, by: Int): BigInteger {
            return (input.mod(4294967296.toBigInteger()) shr by)
        }

        protected fun tearCode(list1: List<BigInteger>, list2: List<BigInteger>): MutableList<BigInteger> {
            var a1 = list1[0]
            var a2 = list1[1]
            var temp = big0

            for (_i in 0 until 32) {
                a1 += (a2 shl 4 xor rShift(a2, 5)) + a2 xor temp + list2[(temp and big3).toInt()]
                temp -= 1640531527.toBigInteger()
                a2 += (a1 shl 4 xor rShift(a1, 5)) + a1 xor temp + list2[(rShift(temp, 11) and big3).toInt()]
            }
            return mutableListOf(a1, a2)
        }

        protected fun ascii2bytes(input: String): List<BigInteger> {
            val abc = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
            val abcMap = abc.mapIndexed { i, c -> c to i.toBigInteger() }.toMap()
            var index = -1
            val length = input.length
            var listIndex = 0
            val bytes = mutableListOf<BigInteger>()

            while (true) {
                for (i in input) {
                    if (abc.contains(i)) {
                        index++
                        break
                    }
                }

                bytes.add((abcMap[input.getOrNull(index)?:return bytes]!! * big4))

                while (true) {
                    index++
                    if (abc.contains(input[index])) {
                        break
                    }
                }

                var temp = abcMap[input[index]]!!

                bytes[listIndex] = bytes[listIndex] or rShift(temp, 4)
                listIndex++
                temp = (big15.and(temp))

                if ((temp == big0) && (index == (length - 1))) return bytes

                bytes.add((temp * big4 * big4))

                while (true) {
                    index++
                    if (index >= length) return bytes
                    if (abc.contains(input[index])) break
                }

                temp = abcMap[input[index]]!!
                bytes[listIndex] = bytes[listIndex] or rShift(temp, 2)
                listIndex++
                temp = (big3 and temp)
                if ((temp == big0) && (index == (length - 1))) {
                    return bytes
                }
                bytes.add((temp shl 6))
                for (i in input) {
                    index++
                    if (abc.contains(input[index])) {
                        break
                    }
                }
                bytes[listIndex] = bytes[listIndex] or abcMap[input[index]]!!
                listIndex++
            }
        }

        protected fun replace(a: String): String {
            val map = mapOf(
                '0' to '5',
                '1' to '6',
                '2' to '7',
                '5' to '0',
                '6' to '1',
                '7' to '2'
            )
            var b = ""
            a.forEach {
                b += if (map.containsKey(it)) map[it] else it
            }
            return b
        }

        protected fun padLastChars(input:String):String{
            return if(input.reversed()[3].isDigit()) input
            else input.dropLast(4)
        }
    }

    @Serializable
    protected data class HashResponse(
        @SerialName("hash") val hash: String? = null,
        @SerialName("version") val version:String? = null
    )

    @Serializable
    protected data class SetupResponse(
        @SerialName("seed") val seed: String,
        @SerialName("src") val src: String?=null,
        @SerialName("link") val link:String?=null
    )
}