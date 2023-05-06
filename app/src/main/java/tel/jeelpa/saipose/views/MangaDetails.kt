package tel.jeelpa.saipose.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import tel.jeelpa.saipose.components.PreviewAnnotations
import tel.jeelpa.saipose.components.PreviewThemeProvider
import tel.jeelpa.saipose.data.viewmodels.MangaDetailsViewModel
import tel.jeelpa.saipose.utils.LoadState


@Composable
@MangaNavGraph
@Destination
fun MangaDetails(
    navigator: DestinationsNavigator,
    viewModel: MangaDetailsViewModel,
    id:Int,
){
    // split up variables for local state & readability
    val mangaDetails = viewModel.mangaDetails.data
    val mangaDetailsLoadState = viewModel.mangaDetails.loadState


    // load the anime details in the viewmodel
    LaunchedEffect(key1 = Unit){
        viewModel.loadData(id)
    }

    // do something about this down here
    val navigateToMangaReader : (Int) -> Unit = remember {
        // implement manga reader and navigate to it
        {}
    }

    // handle all loading states
    when(mangaDetailsLoadState) {

        // TODO : show placeholder/skeleton UI when Loading data
        is LoadState.Uninitialized,
        is LoadState.Loading -> Text("Loading...")

        // TODO : make a generic fullscreen ERROR page for failures
        is LoadState.Error -> Text("Error Occured : ${mangaDetailsLoadState.details}")

        is LoadState.Success ->
            MangaDetailsStateless(
                coverImageUrl = mangaDetails!!.coverImage,
                bannerImageUrl = mangaDetails.bannerImage ?: "Banner Image URl was NULL",
                title = (mangaDetails.title),
                anilistId = id,
                chapters = viewModel.chapterLinks.data,
                description = mangaDetails.description,
                onChapterClick = navigateToMangaReader
            )
    }
}

@Composable
fun MangaDetailsStateless(
    anilistId: Int,
    coverImageUrl: String,
    bannerImageUrl: String,
    title: String,
    chapters : List<String>,
    description: String?,
    onChapterClick: (Int) -> Unit
) {

    LazyColumn {
        item{
            Column {
                Text(anilistId.toString())
                Text(coverImageUrl)
                Text(bannerImageUrl)
                Text(title)
                Text(description ?: "Description text was null")
            }
        }
        itemsIndexed(chapters) { idx, item ->
            Button(onClick = { onChapterClick(idx) }, modifier = Modifier.fillMaxWidth()) {
                Text(item)
            }
        }
    }
}

@PreviewAnnotations
@Composable
private fun _preview(){
    PreviewThemeProvider {
        MangaDetailsStateless(
            anilistId = 12,
            coverImageUrl = "Cover Image URL",
            bannerImageUrl = "Banner Image URL",
            title = "A Manga Title",
            chapters = listOf("1","2","3"),
            description = "A Manga Description Paragraph",
            onChapterClick = {}
        )
    }
}