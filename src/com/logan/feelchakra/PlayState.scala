package com.logan.feelchakra

import android.util.Log

import MainActor._
import UI._

class PlayState(
  val playing: Boolean,
  val startPos: Int
) { 

  def this() = this(false, 0)

  val startTimeOp: Option[Long] = {
    if (playing) {
      Some(Platform.currentTime)
    } else None
  }

  def setPlaying(playing: Boolean): PlayState = {
    mainActorRef ! NotifyHandlers(OnPlayingChanged(playing))
    new PlayState(playing, startPos)
  }

  def setStartPos(startPos: Int): PlayState = {
    mainActorRef ! NotifyHandlers(OnStartPosChanged(startPos))
    new PlayState(playing, startPos)
  }

}
