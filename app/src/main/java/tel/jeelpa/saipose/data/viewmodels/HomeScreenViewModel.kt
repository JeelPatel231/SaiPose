package tel.jeelpa.saipose.data.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import tel.jeelpa.saipose.R
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(): ViewModel() {
    var currentDestination: NavItems by mutableStateOf(NavItems.UserHome)
        private set
    fun changeCurrentDestination(new: NavItems){
        this.currentDestination = new
    }
}

enum class NavItems(val label: String, val icon: Int) {
    Anime("Anime", R.drawable.round_movie_filter_24),
    UserHome("Home", R.drawable.round_home_24),
    Manga("Manga", R.drawable.round_menu_book_24),
}