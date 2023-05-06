package tel.jeelpa.saipose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size

private const val WIDTH: Int = 118
private const val ASPECT_RATIO: Float = 2/3F

@Composable
fun MediaCardPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(WIDTH.dp)
            .padding(4.dp)
            .clip(MaterialTheme.shapes.medium)
            .padding(4.dp)
    ) {
        Box(
            modifier = modifier
                .aspectRatio(ASPECT_RATIO)
                .customPlaceholder(true)
                .clip(MaterialTheme.shapes.medium)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .height((MaterialTheme.typography.bodySmall.lineHeight.value * 2).dp)
                .fillMaxWidth()
                .customPlaceholder(true)
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun MediaCard(
    photoUrl: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(WIDTH.dp)
            .padding(4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = true, onClick = onClick)
            .padding(4.dp)
    ) {
        var visible by remember { mutableStateOf(true) }
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUrl)
                .crossfade(250)
                .size(Size.ORIGINAL)
                .build(),
            onSuccess = { visible = false }
        )
        Image(
            painter = painter,
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .aspectRatio(ASPECT_RATIO)
                .customPlaceholder(visible)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .height((MaterialTheme.typography.bodySmall.lineHeight.value * 2).dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@PreviewAnnotations
@Composable
private fun _preview(){
    PreviewThemeProvider {
        MediaCardPlaceholder()
    }
}

@PreviewAnnotations
@Composable
private fun _preview2(){
    PreviewThemeProvider {
        Row {
            MediaCard(
                photoUrl = "",
                label = "Kimetsu no Yaiba: Katanakaji no Sato-hen",
                onClick = {},
            )
        }
    }
}