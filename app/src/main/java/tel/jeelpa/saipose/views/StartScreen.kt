package tel.jeelpa.saipose.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import tel.jeelpa.saipose.BottomNavigationBar
import tel.jeelpa.saipose.components.PreviewAnnotations
import tel.jeelpa.saipose.components.PreviewThemeProvider
import tel.jeelpa.saipose.data.viewmodels.HomeScreenViewModel
import tel.jeelpa.saipose.data.viewmodels.NavItems


@Composable
@RootNavGraph(start = true)
@Destination
fun StartScreen(
    navigator: DestinationsNavigator,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    StartScreenStateless(
        currentDestination = viewModel.currentDestination,
        navItems = NavItems.values(),
        onNavButtonCLick = viewModel::changeCurrentDestination
    ) {
        ScreenContainer(
            navigator = navigator,
            currentDestination = viewModel.currentDestination
        )
    }
}

@Composable
fun ScreenContainer(
    navigator: DestinationsNavigator,
    currentDestination: NavItems
){
    when (currentDestination) {
        NavItems.UserHome -> HomeScreen(navigator)
        NavItems.Anime -> AnimeScreen(navigator)
        NavItems.Manga -> MangaScreen(navigator)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreenStateless(
    currentDestination: NavItems,
    navItems: Array<NavItems>,
    onNavButtonCLick: (NavItems) -> Unit,
    content: @Composable () -> Unit,
){
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentDestination = currentDestination,
                items = navItems,
                onItemClick = onNavButtonCLick
            )
        }
    ) {
        Box(
            Modifier
                .padding(bottom = it.calculateBottomPadding())
                .fillMaxWidth()) {
            content()
        }
    }
}

@PreviewAnnotations
@Composable
private fun _preview(){
    PreviewThemeProvider {
        StartScreenStateless(
            currentDestination = NavItems.UserHome,
            navItems = NavItems.values(),
            onNavButtonCLick = {}
        ) {
            Text(text = "Preview")
        }
    }
}