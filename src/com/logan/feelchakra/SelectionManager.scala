package com.logan.feelchakra

import android.util.Log

class SelectionManager(val list: List[Selection], val current: Selection) {

  def this() = this(
    List(TrackSelection, StationSelection), 
    TrackSelection
  )

  import MainActor._
  import UI._

  def setCurrent(current: Selection): SelectionManager = {
    mainActorRef ! NotifyHandlers(OnSelectionChanged(current))
    new SelectionManager(list, current)
  }

}
