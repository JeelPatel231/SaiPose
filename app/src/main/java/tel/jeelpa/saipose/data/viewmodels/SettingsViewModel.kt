package tel.jeelpa.saipose.data.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import tel.jeelpa.saipose.data.database.Plugin
import tel.jeelpa.saipose.data.database.PluginType
import tel.jeelpa.saipose.utils.PluginManager
import javax.inject.Inject

// TODO : feels bad, data is directly available, why lateinit and var
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val pluginManager : PluginManager
) : ViewModel() {

    val allPlugins : Flow<List<Plugin>>
        get() = pluginManager.listAllPlugins().distinctUntilChanged()

    private fun getPluginType(type: String) : PluginType? {
        return when(type){
            "parser" -> PluginType.PARSER
            "extractor" -> PluginType.EXTRACTOR
            else -> null
        }
    }

    // TODO : nuke all this, move to plugin manager
    fun registerPlugin(className: String) = viewModelScope.launch {
        val jarName = "parserdex.jar"
        val (type, serviceName) = className.split('.').takeLast(2)
        println("$type, $serviceName, $className")
        pluginManager.registerPlugin(
            Plugin(
                serviceName = serviceName,
                jarName = jarName,
                className = className,
                type = getPluginType(type)!!
            )
        )
    }

    fun deletePluginDirect(plugin: Plugin) = viewModelScope.launch {
        pluginManager.deletePlugin(plugin)
    }

    // TODO : nuke all this, move to plugin manager
    fun deletePlugin(className: String) = viewModelScope.launch {
        val jarName = "parserdex.jar"
        val (type, serviceName) = className.split('.').takeLast(2)
        println("$type, $serviceName, $className")

        pluginManager.deletePlugin(
            Plugin(
                serviceName = serviceName,
                jarName = jarName,
                className = className,
                type = getPluginType(type)!!
            )
        )
    }
}