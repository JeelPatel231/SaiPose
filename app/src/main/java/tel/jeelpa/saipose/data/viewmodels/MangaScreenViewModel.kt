package tel.jeelpa.saipose.data.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import tel.jeelpa.saipose.domain.remote.BaseManga
import tel.jeelpa.saipose.domain.remote.MangaClient
import tel.jeelpa.saipose.utils.fill
import tel.jeelpa.saipose.utils.withState
import javax.inject.Inject

@HiltViewModel
class MangaScreenViewModel @Inject constructor(
    private val mangaClient: MangaClient
): ViewModel() {
    val trendingManga = mutableStateListOf<BaseManga>().withState()
    val popularManga = mutableStateListOf<BaseManga>().withState()
    val popularNovel = mutableStateListOf<BaseManga>().withState()

    init {
        with(viewModelScope) {
            launch { trendingManga.fill(mangaClient::getTrending) }
            launch { popularManga.fill(mangaClient::getPopular) }
            launch { popularNovel.fill(mangaClient::getRecentlyUpdated) }
        }
    }
}
