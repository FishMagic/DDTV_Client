package me.ftmc.common.components

import androidx.compose.runtime.Composable
import me.ftmc.common.pages.RealRoom
import me.ftmc.common.pages.Room


@Composable
fun RoomCard(
  room: Room, expandedUpdater: (Boolean) -> Unit, cancelUpdater: () -> Unit, addSuccessUpdater: (Boolean) -> Unit
) {
  if (room is RealRoom) {
    RoomStatusCard(room, expandedUpdater)
  } else {
    RoomAddCard(cancelUpdater, addSuccessUpdater)
  }
}