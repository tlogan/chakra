package com.logan.feelchakra


object Selection {

  def label(selection: Selection): String = {
    selection match {
      case StationSelection =>  "Station"
      case ArtistSelection =>  "Artist"
      case AlbumSelection =>  "Album"
      case TrackSelection =>  "Track"
    }
  }

}

sealed trait Selection

case object StationSelection extends Selection
case object ArtistSelection extends Selection
case object AlbumSelection extends Selection
case object TrackSelection extends Selection
