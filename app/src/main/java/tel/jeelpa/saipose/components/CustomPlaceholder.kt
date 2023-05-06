package tel.jeelpa.saipose.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder

fun Modifier.customPlaceholder(
    visible: Boolean,
    shape: Shape? = null,
    color: Color? = null
) = composed {
    placeholder(
        color = color ?: MaterialTheme.colorScheme.secondaryContainer,
        visible = visible,
        highlight = PlaceholderHighlight.fade(),
        shape = shape ?: MaterialTheme.shapes.medium
    )
}