package com.logan.feelchakra

object PlayerPart {
  def label(playerPart: PlayerPart): String = {
    playerPart match {
      case PlayerQueue =>  "Queue"
      case PlayerChat =>  "Chat"
    }
  }
}

sealed trait PlayerPart

case object PlayerQueue extends PlayerPart
case object PlayerChat extends PlayerPart
