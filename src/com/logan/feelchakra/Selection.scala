package com.logan.feelchakra

sealed trait Selection {
  val label: String
}

case object StationSelection extends Selection {
  val label: String =  "Station"
}
case object ArtistSelection extends Selection {
  val label: String =  "Artist"
}
case object AlbumSelection extends Selection {
  val label: String =  "Album"
}
case object TrackSelection extends Selection {
  val label: String =  "Track"
}
