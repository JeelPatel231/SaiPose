package tel.jeelpa.saipose.data.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import tel.jeelpa.saipose.domain.remote.AnimeClient
import tel.jeelpa.saipose.domain.remote.BaseAnime
import tel.jeelpa.saipose.utils.fill
import tel.jeelpa.saipose.utils.withState
import javax.inject.Inject

@HiltViewModel
class AnimeScreenViewModel @Inject constructor(
    private val animeClient: AnimeClient
): ViewModel() {
    val trendingAnime = mutableStateListOf<BaseAnime>().withState()
    val popularAnime = mutableStateListOf<BaseAnime>().withState()
    val recentlyUpdateAnime = mutableStateListOf<BaseAnime>().withState()

    init {
        with(viewModelScope) {
            launch { trendingAnime.fill(animeClient::getTrending) }
            launch { popularAnime.fill(animeClient::getPopular) }
            launch { recentlyUpdateAnime.fill(animeClient::getRecentlyUpdated) }
        }
    }
}