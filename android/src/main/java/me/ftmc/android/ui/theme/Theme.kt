package me.ftmc.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Colors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import me.ftmc.selfmusicplayer.ui.theme.Error10
import me.ftmc.selfmusicplayer.ui.theme.Error100
import me.ftmc.selfmusicplayer.ui.theme.Error20
import me.ftmc.selfmusicplayer.ui.theme.Error30
import me.ftmc.selfmusicplayer.ui.theme.Error40
import me.ftmc.selfmusicplayer.ui.theme.Error80
import me.ftmc.selfmusicplayer.ui.theme.Error90
import me.ftmc.selfmusicplayer.ui.theme.Neutral10
import me.ftmc.selfmusicplayer.ui.theme.Neutral20
import me.ftmc.selfmusicplayer.ui.theme.Neutral90
import me.ftmc.selfmusicplayer.ui.theme.Neutral95
import me.ftmc.selfmusicplayer.ui.theme.Neutral99
import me.ftmc.selfmusicplayer.ui.theme.NeutralVariant30
import me.ftmc.selfmusicplayer.ui.theme.NeutralVariant50
import me.ftmc.selfmusicplayer.ui.theme.NeutralVariant60
import me.ftmc.selfmusicplayer.ui.theme.NeutralVariant80
import me.ftmc.selfmusicplayer.ui.theme.NeutralVariant90
import me.ftmc.selfmusicplayer.ui.theme.Primary10
import me.ftmc.selfmusicplayer.ui.theme.Primary100
import me.ftmc.selfmusicplayer.ui.theme.Primary20
import me.ftmc.selfmusicplayer.ui.theme.Primary30
import me.ftmc.selfmusicplayer.ui.theme.Primary40
import me.ftmc.selfmusicplayer.ui.theme.Primary80
import me.ftmc.selfmusicplayer.ui.theme.Primary90
import me.ftmc.selfmusicplayer.ui.theme.Secondary10
import me.ftmc.selfmusicplayer.ui.theme.Secondary100
import me.ftmc.selfmusicplayer.ui.theme.Secondary20
import me.ftmc.selfmusicplayer.ui.theme.Secondary30
import me.ftmc.selfmusicplayer.ui.theme.Secondary40
import me.ftmc.selfmusicplayer.ui.theme.Secondary80
import me.ftmc.selfmusicplayer.ui.theme.Secondary90
import me.ftmc.selfmusicplayer.ui.theme.Tertiary10
import me.ftmc.selfmusicplayer.ui.theme.Tertiary100
import me.ftmc.selfmusicplayer.ui.theme.Tertiary20
import me.ftmc.selfmusicplayer.ui.theme.Tertiary30
import me.ftmc.selfmusicplayer.ui.theme.Tertiary40
import me.ftmc.selfmusicplayer.ui.theme.Tertiary80
import me.ftmc.selfmusicplayer.ui.theme.Tertiary90
import me.ftmc.selfmusicplayer.ui.theme.Typography2
import me.ftmc.selfmusicplayer.ui.theme.Typography3

private val DarkColorPalette3 = darkColorScheme(
  primary = Primary80,
  primaryContainer = Primary30,
  secondary = Secondary80,
  secondaryContainer = Secondary30,
  tertiary = Tertiary80,
  tertiaryContainer = Tertiary30,
  surface = Neutral10,
  surfaceVariant = NeutralVariant30,
  background = Neutral10,
  error = Error80,
  errorContainer = Error30,
  onPrimary = Primary20,
  onPrimaryContainer = Primary90,
  onSecondary = Secondary20,
  onSecondaryContainer = Secondary90,
  onTertiary = Tertiary20,
  onTertiaryContainer = Tertiary90,
  onSurface = Neutral90,
  onSurfaceVariant = NeutralVariant80,
  onError = Error20,
  onErrorContainer = Error80,
  onBackground = Neutral90,
  outline = NeutralVariant60,
  inverseSurface = Neutral90,
  inverseOnSurface = Neutral20,
  inversePrimary = Primary40
)

private val LightColorPalette3 = lightColorScheme(
  primary = Primary40,
  primaryContainer = Primary90,
  secondary = Secondary40,
  secondaryContainer = Secondary90,
  tertiary = Tertiary40,
  tertiaryContainer = Tertiary90,
  surface = Neutral99,
  surfaceVariant = NeutralVariant90,
  background = Neutral99,
  error = Error40,
  errorContainer = Error90,
  onPrimary = Primary100,
  onPrimaryContainer = Primary10,
  onSecondary = Secondary100,
  onSecondaryContainer = Secondary10,
  onTertiary = Tertiary100,
  onTertiaryContainer = Tertiary10,
  onSurface = Neutral10,
  onSurfaceVariant = NeutralVariant30,
  onError = Error100,
  onErrorContainer = Error10,
  onBackground = Neutral10,
  outline = NeutralVariant50,
  inverseSurface = Neutral20,
  inverseOnSurface = Neutral95,
  inversePrimary = Primary80
)

@Composable
fun Theme3(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  val colors = if (android.os.Build.VERSION.SDK_INT >= 31) {
    if (darkTheme) {
      dynamicDarkColorScheme(context)
    } else {
      dynamicLightColorScheme(context)
    }
  } else {
    if (darkTheme) {
      DarkColorPalette3
    } else {
      LightColorPalette3
    }
  }

  MaterialTheme(
    colorScheme = colors,
    content = content,
    typography = Typography3
  )
}

@Composable
fun Theme2(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = MaterialTheme.colorScheme
  androidx.compose.material.MaterialTheme(
    colors = Colors(
      primary = colorScheme.primary,
      primaryVariant = colorScheme.primaryContainer,
      onPrimary = colorScheme.onPrimary,
      secondary = colorScheme.secondary,
      secondaryVariant = colorScheme.secondaryContainer,
      onSecondary = colorScheme.onSecondary,
      surface = colorScheme.surface,
      onSurface = colorScheme.onSurface,
      background = colorScheme.background,
      onBackground = colorScheme.onBackground,
      error = colorScheme.error,
      onError = colorScheme.onError,
      isLight = !darkTheme
    ),
    content = content,
    typography = Typography2
  )
}


@Composable
fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  Theme3(darkTheme = darkTheme) {
    Theme2(darkTheme = darkTheme) {
      val surfaceColor = MaterialTheme.colorScheme.surface
      Surface(color = surfaceColor, modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        androidx.compose.material.Surface(color = surfaceColor, content = content)
      }
    }
  }
}
