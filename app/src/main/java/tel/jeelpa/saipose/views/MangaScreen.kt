package tel.jeelpa.saipose.views

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import tel.jeelpa.saipose.components.HorizontalCardView
import tel.jeelpa.saipose.components.PreviewAnnotations
import tel.jeelpa.saipose.components.PreviewThemeProvider
import tel.jeelpa.saipose.data.viewmodels.MangaScreenViewModel
import tel.jeelpa.saipose.domain.remote.BaseManga
import tel.jeelpa.saipose.utils.DataLoadState
import tel.jeelpa.saipose.utils.navigateTop
import tel.jeelpa.saipose.views.destinations.MangaDetailsDestination

@RootNavGraph
@NavGraph
annotation class MangaNavGraph(
    val start: Boolean = false
)

@MangaNavGraph(start = true)
@Composable
@Destination
fun MangaScreen(
    navigator: DestinationsNavigator,
    viewModel: MangaScreenViewModel = hiltViewModel()
){
    val goToAnimeDetails: (Int) -> Unit = remember {
        { navigator.navigateTop(MangaDetailsDestination(id = it))}
    }
    MangaScreenStateless(
        onAnimeClick = goToAnimeDetails,
        trendingList = viewModel.trendingManga,
        popularList = viewModel.popularManga,
        popularNovel = viewModel.popularNovel
    )
}

@Composable
@Destination
fun MangaScreenStateless(
    trendingList: DataLoadState<SnapshotStateList<BaseManga>>,
    popularList: DataLoadState<SnapshotStateList<BaseManga>>,
    popularNovel: DataLoadState<SnapshotStateList<BaseManga>>,
    onAnimeClick: (Int) -> Unit
) {
    LazyColumn {
        item {
            Text(text = "Trending")
            HorizontalCardView(
                listData = trendingList.data,
                loadState = trendingList.loadState,
                onMediaClick = onAnimeClick
            )
            Text(text = "Popular")
            HorizontalCardView(
                listData = popularList.data,
                loadState = popularList.loadState,
                onMediaClick = onAnimeClick
            )
            Text(text = "Popular Novel")
            HorizontalCardView(
                listData = popularNovel.data,
                loadState = popularNovel.loadState,
                onMediaClick = onAnimeClick
            )
        }
    }
}

@PreviewAnnotations
@Composable
private fun _preview(){
    val unloadedData = DataLoadState(emptyList<BaseManga>().toMutableStateList())
    PreviewThemeProvider {
        MangaScreenStateless(
            trendingList = unloadedData,
            popularList = unloadedData,
            popularNovel = unloadedData,
            onAnimeClick = {}
        )
    }
}