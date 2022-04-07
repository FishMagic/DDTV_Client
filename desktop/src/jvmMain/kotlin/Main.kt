import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import me.ftmc.android.ui.theme.AppTheme
import me.ftmc.common.App
import me.ftmc.common.darkMode
import me.ftmc.common.screenTypeChangeWidth

fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    state = WindowState(size = DpSize(screenTypeChangeWidth, 650.dp)),
    resizable = false,
    title = "DDTV 客户端",
    icon = painterResource("app_icon.png")
  ) {
    AppTheme(darkTheme = darkMode ?: isSystemInDarkTheme()) {
      App()
    }
  }
}