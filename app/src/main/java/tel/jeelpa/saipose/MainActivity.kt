package tel.jeelpa.saipose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint
import tel.jeelpa.saipose.components.PreviewAnnotations
import tel.jeelpa.saipose.components.PreviewThemeProvider
import tel.jeelpa.saipose.data.viewmodels.AnimeDetailsViewModel
import tel.jeelpa.saipose.data.viewmodels.MangaDetailsViewModel
import tel.jeelpa.saipose.data.viewmodels.NavItems
import tel.jeelpa.saipose.dependency.Injection
import tel.jeelpa.saipose.reference.ExtractorMap
import tel.jeelpa.saipose.reference.ParserMap
import tel.jeelpa.saipose.reference.initializeNetwork
import tel.jeelpa.saipose.ui.theme.SaiposeTheme
import tel.jeelpa.saipose.views.NavGraphs
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
class MainActivity: ComponentActivity() {


    @Inject @Named("Parsers") lateinit var _parsers: Injection.ParserProxy
    @Inject @Named("Extractors") lateinit var _extractors: Injection.ExtractorProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        initializeNetwork()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        // Register all parsers and extractors
        ParserMap = _parsers.data
        ExtractorMap = _extractors.data

        setContent {
            SaiposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()
                    DestinationsNavHost(
                        navController = navController,
                        navGraph = NavGraphs.root,
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding(),

                        // share the anime details viewmodels in the AnimeRoute Destinations
                        // i.e AnimeDetails and ExoplayerView
                        dependenciesContainerBuilder = { //this: DependenciesContainerBuilder<*>
                            dependency(NavGraphs.anime) {
                                val parentAnimeEntry = remember(navBackStackEntry) {
                                    navController.getBackStackEntry(NavGraphs.anime.route)
                                }
                                hiltViewModel<AnimeDetailsViewModel>(parentAnimeEntry)

                            }
                            dependency(NavGraphs.manga) {
                                val parentMangaEntry = remember(navBackStackEntry) {
                                    navController.getBackStackEntry(NavGraphs.manga.route)
                                }
                                hiltViewModel<MangaDetailsViewModel>(parentMangaEntry)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentDestination: NavItems,
    items: Array<NavItems>,
    onItemClick: (NavItems) -> Unit,
    modifier:Modifier = Modifier
){
    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = currentDestination == item,
                onClick = { onItemClick( item ) }
            )
        }
    }
}

