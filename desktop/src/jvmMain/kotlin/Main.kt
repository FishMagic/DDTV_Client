import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import me.ftmc.android.ui.theme.AppTheme
import me.ftmc.common.App
import me.ftmc.common.backend.darkMode
import me.ftmc.common.screenTypeChangeWidth

fun main() = application {
  Window(
    onCloseRequest = ::exitApplication,
    state = WindowState(size = DpSize(screenTypeChangeWidth, 600.dp)),
    resizable = false
  ) {
    AppTheme(darkTheme = darkMode ?: isSystemInDarkTheme()) {
      App()
    }
  }
}