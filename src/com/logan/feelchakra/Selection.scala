package com.logan.feelchakra

sealed trait Selection {
  val label: String
}
case object TrackSelection extends Selection {
  val label: String =  "Tracks"
}
case object StationSelection extends Selection {
  val label: String =  "Stations"
}
