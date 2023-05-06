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
import tel.jeelpa.saipose.data.viewmodels.AnimeScreenViewModel
import tel.jeelpa.saipose.domain.remote.BaseAnime
import tel.jeelpa.saipose.utils.DataLoadState
import tel.jeelpa.saipose.utils.navigateTop
import tel.jeelpa.saipose.views.destinations.AnimeDetailsDestination

@RootNavGraph
@NavGraph
annotation class AnimeNavGraph(
    val start: Boolean = false
)

@AnimeNavGraph(start = true)
@Composable
@Destination
fun AnimeScreen(
    navigator: DestinationsNavigator,
    viewModel: AnimeScreenViewModel = hiltViewModel()
){
    val goToAnimeDetails: (Int) -> Unit = remember {
        { navigator.navigateTop(AnimeDetailsDestination(id = it)) }
    }
    AnimeScreenStateless(
        onAnimeClick = goToAnimeDetails,
        trendingList = viewModel.trendingAnime,
        popularList = viewModel.popularAnime,
        recentlyUpdatedList = viewModel.recentlyUpdateAnime
    )
}

@Composable
@Destination
fun AnimeScreenStateless(
    trendingList: DataLoadState<SnapshotStateList<BaseAnime>>,
    popularList: DataLoadState<SnapshotStateList<BaseAnime>>,
    recentlyUpdatedList: DataLoadState<SnapshotStateList<BaseAnime>>,
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
            Text(text = "Recently Updated")
            HorizontalCardView(
                listData = recentlyUpdatedList.data,
                loadState = recentlyUpdatedList.loadState,
                onMediaClick = onAnimeClick
            )
        }
    }
}

@PreviewAnnotations
@Composable
private fun _preview(){
    val unloadedData = DataLoadState(emptyList<BaseAnime>().toMutableStateList())
    PreviewThemeProvider {
        AnimeScreenStateless(
            trendingList = unloadedData,
            popularList = unloadedData,
            recentlyUpdatedList = unloadedData,
            onAnimeClick = {}
        )
    }
}