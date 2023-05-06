package tel.jeelpa.saipose.data.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import tel.jeelpa.saipose.domain.remote.MangaClient
import tel.jeelpa.saipose.domain.remote.MangaDetails
import tel.jeelpa.saipose.utils.DataLoadState
import tel.jeelpa.saipose.utils.LoadState
import tel.jeelpa.saipose.utils.fill
import tel.jeelpa.saipose.utils.withState
import javax.inject.Inject

@HiltViewModel
class MangaDetailsViewModel @Inject constructor(
    private val mangaClient: MangaClient,
): ViewModel() {

    val mangaDetails: DataLoadState<MangaDetails?> = DataLoadState(null)
    val chapterLinks = mutableStateListOf<String>().withState()

    suspend fun loadData(id: Int) = coroutineScope {
        mangaDetails.fillData(mangaClient.getMangaDetails(id))

        // this will be entirely decouple from anilist because the count depends constructor the service we are scraping
        // the service might be up to date or couple episodes behind or simpy doesnt have the anime hosted
        if(mangaDetails.loadState == LoadState.Success) {
            chapterLinks.fill { (1..(mangaDetails.data!!.chapterCount?:0)).map{ "$it" } }
        } else {
            chapterLinks.loadState = mangaDetails.loadState
        }
    }
}