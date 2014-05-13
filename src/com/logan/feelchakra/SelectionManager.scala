package com.logan.feelchakra

import android.util.Log

case class SelectionManager(list: List[Selection], current: Selection) {

  def this() = this(
    List(StationSelection, ArtistSelection, AlbumSelection, TrackSelection), 
    ArtistSelection
  )

  import MainActor._
  import UI._

  def setCurrent(current: Selection): SelectionManager = {
    mainActorRef ! NotifyHandlers(OnSelectionChanged(current))
    this.copy(current = current)
  }

}
