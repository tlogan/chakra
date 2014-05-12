package com.logan.feelchakra

sealed trait Selection {
  val label: String
}

case object ArtistSelection extends Selection {
  val label: String =  "Artist"
}
case object TrackSelection extends Selection {
  val label: String =  "Track"
}
case object StationSelection extends Selection {
  val label: String =  "Station"
}
