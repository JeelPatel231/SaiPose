package tel.jeelpa.saipose.domain.remote

import tel.jeelpat.type.MediaStatus

open class BaseAnime(
    override val anilistId: Int,
    override val malId: Int?,
    override val title: String,
    override val coverImage: String,
    open val bannerImage: String?,
    open val episodeCount: Int?,
    open val airingStatus: MediaStatus?,
) : BaseMedia

data class AnimeDetails(
    override val anilistId: Int,
    override val malId: Int?,
    override val title: String,
    override val coverImage: String,
    override val bannerImage: String?,
    override val episodeCount: Int?,
    override val airingStatus: MediaStatus?,
    val description: String?,
    val genres : List<String?>
): BaseAnime(anilistId, malId,title, coverImage, bannerImage, episodeCount, airingStatus)


interface AnimeClient {
    suspend fun getTrending() : List<BaseAnime>

    suspend fun getPopular() : List<BaseAnime>

    suspend fun getRecentlyUpdated() : List<BaseAnime>

    suspend fun getAnimeDetails(id: Int): AnimeDetails

    suspend fun searchAnime(query: String): List<BaseAnime>
}