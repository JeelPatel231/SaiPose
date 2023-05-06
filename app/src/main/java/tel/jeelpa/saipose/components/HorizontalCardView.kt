package tel.jeelpa.saipose.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import tel.jeelpa.saipose.utils.LoadState
import tel.jeelpa.saipose.domain.remote.AnimeDetails
import tel.jeelpa.saipose.domain.remote.BaseMedia
import tel.jeelpat.type.MediaStatus


@Composable
fun HorizontalCardView(
    listData: List<BaseMedia>,
    loadState: LoadState,
    onMediaClick: (Int) -> Unit,
){
    val screenSize = LocalConfiguration.current.screenWidthDp
    val maxItems = (screenSize/130) + 1

    when (loadState) {

        is LoadState.Loading,
        is LoadState.Uninitialized -> {
            LazyRow {
                items(maxItems) {
                    MediaCardPlaceholder()
                }
            }
        }

        is LoadState.Success -> LazyRow {
            items(listData) {
                MediaCard(
                    photoUrl = it.coverImage,
                    label = it.title,
                    onClick = { onMediaClick(it.anilistId) }
                )
            }
        }
        is LoadState.Error -> Text("Error Occurred : ${loadState.details}")
    }
}

@PreviewAnnotations
@Composable
private fun _preview(){
    PreviewThemeProvider {
        HorizontalCardView(
            listData = emptyList(),
            loadState = LoadState.Uninitialized,
            onMediaClick = {}
        )
    }
}

@PreviewAnnotations
@Composable
private fun _preview2(){
    val baseMediaList = (0..10).map {
        AnimeDetails(
            anilistId = it,
            title = "Anime Title # $it",
            description = null,
            airingStatus = MediaStatus.RELEASING,
            bannerImage = null,
            coverImage = "Invalid Cover Image URL",
            genres = listOf(),
            malId = 12,
            episodeCount = 12
        )
    }.toList()
    PreviewThemeProvider {
        HorizontalCardView(
            listData = baseMediaList,
            loadState = LoadState.Success,
            onMediaClick = {}
        )
    }
}