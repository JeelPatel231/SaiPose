package tel.jeelpa.saipose.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import tel.jeelpa.saipose.R
import tel.jeelpa.saipose.components.PreviewAnnotations
import tel.jeelpa.saipose.components.PreviewThemeProvider
import tel.jeelpa.saipose.reference.ExtractorMap
import tel.jeelpa.saipose.reference.ParserMap
import tel.jeelpa.saipose.utils.navigateTop
import tel.jeelpa.saipose.views.destinations.SettingsViewDestination


@Composable
fun HomeScreen(
    navigator: DestinationsNavigator,
){
    HomeScreenStateless(
        onSettingsClick = { navigator.navigateTop(SettingsViewDestination) }
    )
}

@Composable
fun HomeScreenStateless(
    onSettingsClick: () -> Unit
) {
    LazyColumn {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Home Screen",
                    modifier = Modifier
                        .weight(1F)
                        .align(Alignment.CenterVertically)
                )
                IconButton(
                    onClick = onSettingsClick,
                ) {
                    Icon(painterResource(R.drawable.round_settings_24), "Settings")
                }
            }
        }
        item {
            Text(
                text = "Loaded Parsers",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        items(ParserMap.toList()){
           Text(it.first)
        }
        item{
            Text(
                text = "Loaded Extractors",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        items(ExtractorMap.toList()){
            Text(it.first)
        }
    }
}


@PreviewAnnotations
@Composable
private fun _preview(){
    PreviewThemeProvider {
        HomeScreenStateless(
            onSettingsClick = {}
        )
    }
}