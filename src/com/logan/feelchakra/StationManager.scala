package com.logan.feelchakra

import android.util.Log

import scala.concurrent.ExecutionContext.Implicits.global

case class StationManager(
  playState: PlayState
) { 

  def this() = this(
    NotPlaying
  )

  import MainActor._
  import UI._



  /*
  def setCurrentConnection(stationCon: StationConnection): StationManager = {
    mainActorRef ! NotifyHandlers(OnStationConnectionChanged(stationCon))
    this.copy(currentConnection = stationCon)
  }

  def commitStationConnection(): StationManager = {
    currentConnection match {
      case StationRequested(station) =>
        val stationCon = StationConnected(station)
        mainActorRef ! NotifyHandlers(OnStationConnectionChanged(stationCon))
        this.copy(currentConnection = stationCon)
      case _ => 
        assert(false)
        this
    }
    
  }
  */

  def setPlayState(playState: PlayState): StationManager = {
    mainActorRef ! NotifyHandlers(OnStationPlayStateChanged(playState))
    this.copy(playState = playState)
  }

}
