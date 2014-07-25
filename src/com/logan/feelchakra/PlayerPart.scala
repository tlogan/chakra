package com.logan.feelchakra

sealed trait PlayerPart {
  val label: String
}

case object PlayerQueue extends PlayerPart {
  val label: String =  "Queue"
}

case object PlayerChat extends PlayerPart {
  val label: String =  "Chat"
}
