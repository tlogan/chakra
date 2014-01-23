package com.feelchakra

import android.app.Fragment

sealed trait Selection {
  val label: String
}
case object Tracks extends Selection {
  val label: String =  "Akka Tracks"
}
case object Stations extends Selection {
  val label: String =  "Akka Stations"
}
