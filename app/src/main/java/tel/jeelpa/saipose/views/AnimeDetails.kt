package tel.jeelpa.saipose.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.takeWhile
import tel.jeelpa.saipose.components.PreviewAnnotations
import tel.jeelpa.saipose.components.PreviewThemeProvider
import tel.jeelpa.saipose.data.viewmodels.AnimeDetailsViewModel
import tel.jeelpa.saipose.reference.Episode
import tel.jeelpa.saipose.reference.Video
import tel.jeelpa.saipose.reference.VideoContainer
import tel.jeelpa.saipose.utils.LoadState
import tel.jeelpa.saipose.utils.navigateTop
import tel.jeelpa.saipose.views.destinations.ExoPlayerViewContainerDestination


@Composable
@AnimeNavGraph
@Destination
fun AnimeDetails(
    navigator: DestinationsNavigator,
    viewModel: AnimeDetailsViewModel,
    id:Int,
){
    // split up local references from viewmodel
    val animeDetailsLoadState = viewModel.animeDetails.loadState
    val animeDetails = viewModel.animeDetails.data

    // load the anime details in the viewmodel
    LaunchedEffect(key1 = Unit){
        viewModel.loadData(id)
    }

    // handle all loading states
    when(animeDetailsLoadState) {

        // TODO : show placeholder/skeleton UI when Loading data
        is LoadState.Uninitialized,
        is LoadState.Loading -> Text("Loading...")

        // TODO : make a generic fullscreen ERROR page for failures
        is LoadState.Error -> Text("Error Occurred : ${animeDetailsLoadState.details}")

        is LoadState.Success ->
            AnimeDetailsStateless(
                coverImageUrl = animeDetails!!.coverImage,
                bannerImageUrl = animeDetails.bannerImage ?: "Banner Image URl was NULL",
                title = (animeDetails.title),
                anilistId = id,
                episodes = viewModel.episodeLinks.data,
                description = animeDetails.description,
                extractEpisode = viewModel::extractEpisode,
                onBottomSheetItemClick = { vidSource, episodeIndex ->
                    navigator.navigateTop(
                        ExoPlayerViewContainerDestination(
                            videoSource = vidSource,
                            currentEpisodeIndex = episodeIndex
                        )
                    )
                }
            )
    }
}

@Composable
fun AnimeDetailsStateless(
    anilistId: Int,
    coverImageUrl: String,
    bannerImageUrl: String,
    title: String,
    episodes : List<Episode>,
    description: String?,
    // bottom sheet variables
    extractEpisode: (Int) -> Flow<Pair<String, VideoContainer>?>,
    onBottomSheetItemClick: (Video, Int) -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentSelectedEpisode : Int by remember { mutableStateOf(0) }

    if(showBottomSheet) {
        AnimeSourceSelector(
            onDismissRequest = { showBottomSheet = false },
            listOfElements = extractEpisode(currentSelectedEpisode),
            onLinkClick = { item ->
                onBottomSheetItemClick(item,currentSelectedEpisode)
                showBottomSheet = false
            },
        )
    }


    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Column {
                Text(anilistId.toString())
                Text(coverImageUrl)
                Text(bannerImageUrl)
                Text(title)
                Text(description ?: "Description text was null")
            }
        }
        itemsIndexed(episodes) {idx,item ->
            Button(onClick = {
                currentSelectedEpisode = idx
                showBottomSheet = true
             }, modifier = Modifier.fillMaxWidth()) {
                Text(item.number)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeSourceSelector(
    onDismissRequest: () -> Unit,
    onLinkClick: (Video) -> Unit,
    listOfElements: Flow<Pair<String, VideoContainer>?>,
){

    // local list that holds values collected from channelFlow
    val localListHolder = remember { mutableStateListOf<Pair<String,VideoContainer>>() }
    var finishedLoading by remember { mutableStateOf(false) }

    // start scraping data as soon as the composable starts
    LaunchedEffect(key1 = Unit){
        listOfElements.takeWhile { it != null }.collect {
            localListHolder.add(it!!)
        }
        finishedLoading = true
    }


    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ){
            items(localListHolder){ (vidServerName,vidCont) ->
                Text(text = vidServerName)
                vidCont.videos.forEach {
                    Text(
                        text = it.url.url,
                        modifier = Modifier.clickable(
                            enabled = true,
                            onClick = { onLinkClick(it) }
                        )
                    )
                    Divider()
                }
            }
            item {
                if (!finishedLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@PreviewAnnotations
@Composable
private fun _preview(){
    PreviewThemeProvider {
        AnimeDetailsStateless(
            anilistId = 12,
            coverImageUrl = "Cover Image URL",
            bannerImageUrl = "Banner Image URL",
            title = "A good Anime",
            episodes = listOf(),
            description = "A good Description",
            extractEpisode = { emptyList<Pair<String, VideoContainer>>().asFlow() },
            onBottomSheetItemClick = { _,_ -> }
        )
    }
}