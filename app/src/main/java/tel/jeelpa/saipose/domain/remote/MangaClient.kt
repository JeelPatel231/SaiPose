package tel.jeelpa.saipose.domain.remote

import tel.jeelpat.type.MediaStatus

open class BaseManga(
    override val anilistId: Int,
    override val malId: Int?,
    override val title: String,
    override val coverImage: String,
    open val bannerImage: String?,
    open val chapterCount: Int?,
    open val airingStatus: MediaStatus?,
): BaseMedia

data class MangaDetails(
    override val anilistId: Int,
    override val malId: Int?,
    override val title: String,
    override val coverImage: String,
    override val bannerImage: String?,
    override val chapterCount: Int?,
    override val airingStatus: MediaStatus?,
    val description: String?,
    val genres : List<String?>
): BaseManga(anilistId, malId,title,coverImage, bannerImage, chapterCount, airingStatus)

interface MangaClient {
    suspend fun getTrending() : List<BaseManga>

    suspend fun getPopular() : List<BaseManga>

    suspend fun getRecentlyUpdated() : List<BaseManga>

    suspend fun getMangaDetails(id: Int): MangaDetails

    suspend fun searchAnime(query: String): List<BaseManga>
}