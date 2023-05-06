package tel.jeelpa.saipose.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.coroutineScope

sealed class LoadState {
    object Uninitialized : LoadState()
    object Loading : LoadState()
    object Success : LoadState()
    class Error(val details: String) : LoadState()
}

data class DataLoadState<T>(
    private var _data: T,
) {
    val data : T
        get() = _data
    var loadState: LoadState by mutableStateOf(LoadState.Uninitialized)


    fun fillData(newData: T){
        this.loadState = LoadState.Loading
        this._data = newData
        this.loadState = LoadState.Success
    }

    suspend fun fillData(newDataSource: suspend () -> T) = coroutineScope {
        try {
            loadState = LoadState.Loading
            _data = newDataSource()
            loadState = LoadState.Success
        } catch (e : Throwable) {
            loadState = LoadState.Error(e.message ?: "Empty Message on Error")
        }
    }
}

// TODO: MAKE LOAD STATE PRIVATE AND MAKE THESE METHODS IN CLASS METHODS
suspend fun <U,T : MutableCollection<U>> DataLoadState<T>.fill(newDataSource: suspend () -> Collection<U>){
    try {
        this.loadState = LoadState.Loading
        this.data.clear()
        this.data.addAll(newDataSource())
        this.loadState = LoadState.Success
    } catch (e: Throwable){
        this.loadState = LoadState.Error(e.message ?: "Empty Error Message")
    }
}

fun <U,T : MutableState<U>> DataLoadState<T>.fill(newData: U){
    try {
        this.loadState = LoadState.Loading
        this.data.value = newData
        this.loadState = LoadState.Success
    } catch (e: Throwable){
        this.loadState = LoadState.Error(e.message ?: "Empty Error Message")
    }
}

fun<T> T.withState() : DataLoadState<T> {
    return DataLoadState(this)
}

//suspend fun <U,T : MutableCollection<U>>DataLoadState<T>.safeFill(
//    newDataSource : suspend () -> Collection<U>
//){
//    this.data.addAll(newDataSource())
//}

//fun <U,T : MutableState<U>>DataLoadState<T>.safeFill(
//    newData : U
//){
//    this.data.value = newData
//}
