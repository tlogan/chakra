/*
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

  def setPlayState(playState: PlayState): StationManager = {
    mainActorRef ! NotifyHandlers(OnStationPlayStateChanged(playState))
    this.copy(playState = playState)
  }

}
*/
