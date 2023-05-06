package tel.jeelpa.saipose.data

import com.apollographql.apollo3.ApolloClient
import tel.jeelpa.saipose.domain.remote.AnimeClient
import tel.jeelpa.saipose.domain.remote.AnimeDetails
import tel.jeelpa.saipose.domain.remote.BaseAnime
import tel.jeelpa.saipose.domain.remote.BaseManga
import tel.jeelpa.saipose.domain.remote.MangaClient
import tel.jeelpa.saipose.domain.remote.MangaDetails
import tel.jeelpat.AnimeBaselineQuery
import tel.jeelpat.AnimeDetailsQuery
import tel.jeelpat.AnimeRecentlyUpdatedQuery
import tel.jeelpat.AnimeSearchQuery
import tel.jeelpat.MangaBaselineQuery
import tel.jeelpat.MangaDetailsQuery
import tel.jeelpat.MangaSearchQuery
import tel.jeelpat.type.MediaSort

class AnilistMangaClientImpl(
    private val apolloClient: ApolloClient
) : MangaClient {
    override suspend fun getTrending(): List<BaseManga> {
        return apolloClient.query(
            MangaBaselineQuery(
            page = 1,
            perPage = 50,
            sort = listOf(MediaSort.TRENDING_DESC)
        )
        ).execute()
            .data?.Page?.media?.mapNotNull {
                // return if media is null
                it ?: return@mapNotNull null

                val title = it.title

                BaseManga(
                    anilistId = it.id,
                    malId = it.idMal,
                    coverImage = it.coverImage?.large!!,
                    bannerImage = it.bannerImage,
                    title = title?.english ?: title?.romaji ?: title?.userPreferred!!,
                    chapterCount = it.chapters,
                    airingStatus = it.status
                )
            } ?: emptyList()
    }

    override suspend fun getPopular(): List<BaseManga> {
        return apolloClient.query(
            MangaBaselineQuery(
            page = 1,
            perPage = 50,
            sort = listOf(MediaSort.POPULARITY_DESC)
        )
        ).execute()
            .data?.Page?.media?.mapNotNull {
                // return if media is null
                it ?: return@mapNotNull null
                val title = it.title

                BaseManga(
                    anilistId = it.id,
                    malId = it.idMal,
                    coverImage = it.coverImage?.large!!,
                    bannerImage = it.bannerImage,
                    title = title?.english ?: title?.romaji ?: title?.userPreferred!!,
                    chapterCount = it.chapters,
                    airingStatus = it.status
                )
            } ?: emptyList()
    }

    override suspend fun getRecentlyUpdated(): List<BaseManga> {
        return apolloClient.query(AnimeRecentlyUpdatedQuery(lesser = (System.currentTimeMillis()/1000).toInt()  - 10000)).execute()
            .data?.Page?.airingSchedules?.mapNotNull {
                // return if media is null
                val media = it?.media ?: return@mapNotNull null
                val title = media.title

                BaseManga(
                    anilistId = media.id,
                    malId = media.idMal,
                    coverImage = media.coverImage?.large!!,
                    bannerImage = media.bannerImage,
                    title = title?.english ?: title?.romaji ?: title?.userPreferred!!,
                    chapterCount = media.chapters,
                    airingStatus = media.status
                )
            } ?: emptyList()
    }

    override suspend fun getMangaDetails(id: Int): MangaDetails {
        // not null asserted because we know the media exists in anilist DB
        val data = apolloClient.query(MangaDetailsQuery(id = id)).execute().data!!.Media!!
        val title = data.title
        return MangaDetails(
            anilistId = data.id,
            malId = data.idMal,
            coverImage = data.coverImage?.large!!,
            bannerImage = data.bannerImage,
            title = title?.english ?: title?.romaji ?: title?.userPreferred!!,
            chapterCount = data.chapters,
            description = data.description,
            genres = data.genres ?: emptyList(),
            airingStatus = data.status
        )
    }

    override suspend fun searchAnime(query: String): List<BaseManga> {
        return apolloClient.query(MangaSearchQuery(search = query)).execute()
            .data?.Page?.media?.mapNotNull {
                // return if media is null
                it ?: return@mapNotNull null
                val title = it.title

                BaseManga(
                    anilistId = it.id,
                    malId = it.idMal,
                    coverImage = it.coverImage?.large!!,
                    bannerImage = it.bannerImage,
                    title = title?.english ?: title?.romaji ?: title?.userPreferred!!,
                    chapterCount = it.chapters,
                    airingStatus = it.status
                )
            } ?: emptyList()
    }
}