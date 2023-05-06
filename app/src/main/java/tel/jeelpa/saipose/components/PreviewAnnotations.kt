package tel.jeelpa.saipose.components

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import tel.jeelpa.saipose.ui.theme.SaiposeTheme

@Preview(name="Light Mode",showSystemUi = false)
@Preview(name="Dark Mode",showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name="Light Mode SystemUI",showSystemUi = true)
@Preview(name="Dark Mode SystemUI",showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
//@Preview(name="Landscape",uiMode = Configuration.ORIENTATION_LANDSCAPE, showSystemUi = true)
@Preview(device = Devices.PIXEL_C)
annotation class PreviewAnnotations


@Composable
fun PreviewThemeProvider(content: @Composable () -> Unit) {
    SaiposeTheme {
        Surface {
            content()
        }
    }
}