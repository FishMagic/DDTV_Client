package me.ftmc.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

expect fun getPlatformName(): String

expect fun saveConfig()

expect fun loadConfig()

expect fun byteArrayToImageBitmap(byteArray: ByteArray): ImageBitmap

expect fun Modifier.topBarModifier(): Modifier

expect fun Modifier.bottomBarModifier(): Modifier

expect fun Modifier.navigationBarsHeightModifier(): Modifier