package com.logan.feelchakra

import android.app.Fragment

sealed trait Selection {
  val label: String
}
case object TrackSelection extends Selection {
  val label: String =  "Tracks"
}
case object StationSelection extends Selection {
  val label: String =  "Stations"
}
