package tel.jeelpa.saipose.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import tel.jeelpa.saipose.R
import tel.jeelpa.saipose.components.PreviewAnnotations
import tel.jeelpa.saipose.components.PreviewThemeProvider
import tel.jeelpa.saipose.data.database.Plugin
import tel.jeelpa.saipose.data.viewmodels.SettingsViewModel


@Composable
@Destination
fun SettingsView(
    settings: SettingsViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val localPluginList = settings.allPlugins.collectAsState(initial = emptyList())
    var error by remember { mutableStateOf("") }

    SettingsViewStateless(
        pluginList = localPluginList.value,
        onFABClick = { showDialog = true },
        showDialog = showDialog,
        onDismissRequest = { showDialog = false },
        onRegisterClick = {
            settings.registerPlugin(it)
            showDialog = false
        },
        onPluginItemClick = settings::deletePluginDirect,
        errorString = error,
        onErrorDismiss = { error = ""}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsViewStateless(
    pluginList: List<Plugin>,
    onFABClick: () -> Unit,
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onRegisterClick: (String) -> Unit,
    onPluginItemClick: (Plugin) -> Unit,
    errorString: String,
    onErrorDismiss: () -> Unit,
){
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onFABClick) {
                Icon(painter = painterResource(id = R.drawable.round_add_24), contentDescription = "Add Plugin")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ){ paddingValues ->
        if (showDialog) {
            AddPluginDialog(
                onDismissRequest = onDismissRequest,
                onRegisterClick = onRegisterClick
            )
        }
        if(errorString != ""){
            Dialog(onDismissRequest = onErrorDismiss) {
                Text(errorString)
            }
        }
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            item {
                Text("Plugins in DB")
            }
            items(pluginList) {plugin ->
                Text(
                    text = plugin.className,
                    modifier = Modifier.clickable(enabled = true, onClick = { onPluginItemClick(plugin) })
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPluginDialog(
    onDismissRequest : () -> Unit,
    onRegisterClick : (String) -> Unit,
){
    var inputText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { changedValue -> inputText = changedValue },
                    maxLines = 1,
                    placeholder = { Text(text = "Enter Class Name") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onRegisterClick(inputText) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "Register")
                }
            }
        }
    }
}

@PreviewAnnotations
@Composable
private fun _preview(){
    PreviewThemeProvider {
        SettingsViewStateless(
            emptyList(),
            {},
            false,
            {},
            {},
            {},
            "Unique Constraint Failed",
            {}
        )
    }
}

@PreviewAnnotations
@Composable
private fun _preview2(){
    PreviewThemeProvider {
        AddPluginDialog(onDismissRequest = { }, onRegisterClick = {})
    }
}