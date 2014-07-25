package com.logan.feelchakra

import android.util.Log

case class PlayerPartManager(list: List[PlayerPart], current: PlayerPart)

object PlayerPartManager {
  def create() = PlayerPartManager(List(PlayerQueue, PlayerChat), PlayerQueue)
}
