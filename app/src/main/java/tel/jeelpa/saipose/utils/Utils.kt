package tel.jeelpa.saipose.utils

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.Direction
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <A,B, T : Collection<A>> T.asyncMap( lambda: suspend (A) -> B ) = coroutineScope {
    this@asyncMap.map { async { lambda(it) } }.awaitAll()
}

suspend fun <A,B, T : Collection<A>> T.asyncMapNotNull( lambda: suspend (A) -> B ) = coroutineScope {
    this@asyncMapNotNull.map { async { lambda(it) } }.awaitAll().filterNotNull()
}

@Composable
fun debugPlaceholder(@DrawableRes debugPreview: Int) =
    if (LocalInspectionMode.current) {
        painterResource(id = debugPreview)
    } else {
        null
    }

fun <T> T.printAsObject(pre:String? = "") : T {
    println("$pre$this")
    return this
}

fun <T : Collection<Any?>> T.printAsCollection(pre:String? = "") : T {
    this.forEach{ println("$pre$it") }
    return this
}

fun <T : Collection<Any?>> T.printCollectionSize(pre:String? = "") : T {
    println("$pre${this.size}")
    return this
}

inline fun <A:Any, T, K, V: A?> Iterable<T>.associateNotNull(
    transform: (T) -> Pair<K, V>
): Map<K, A> = this.associate(transform).filterValuesNotNull()

fun <K, V:A? ,A:Any> Map<K,V>.filterValuesNotNull(): Map<K, A> {
    return this.filterValues { it != null } as Map<K, A>
}

fun DestinationsNavigator.navigateTop(
    direction: Direction,
    onlyIfResumed: Boolean = false,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) = navigate(direction, onlyIfResumed, navOptions {launchSingleTop = true}, navigatorExtras)