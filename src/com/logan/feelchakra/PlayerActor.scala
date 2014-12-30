package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object PlayerActor {

  def props(): Props = Props[PlayerActor]

  case class Subscribe(ui: Handler)
  case class SetPlayerOpen(playerOpen: Boolean)
  case object FlipPlayerOpen
  case class SetPlaying(playing: Boolean)
  case object FlipPlaying
  case class SetStartPos(startPos: Int)
  case class SetPlayState(playState: PlayState)

}

class PlayerActor extends Actor {

  import PlayerActor._

  def update(
      playerOpen: Boolean,
      playing: Boolean, 
      startPos: Int,
      playState: PlayState
  ) = {
    context.become(mkReceive(playerOpen, playing, startPos, playState))
  }

  def mkReceive(
      playerOpen: Boolean,
      playing: Boolean, 
      startPos: Int,
      playState: PlayState
  ): Receive = {

    case Subscribe(ui) =>
      List(
        UI.OnPlayerOpenChanged(playerOpen),
        UI.OnLocalStartPosChanged(startPos),
        UI.OnLocalPlayingChanged(playing)
      ).foreach(m => notifyHandler(ui, m))

    case SetPlayerOpen(_playerOpen) =>
      mainActorRef ! MainActor.NotifyHandlers(UI.OnPlayerOpenChanged(_playerOpen))
      update(_playerOpen, playing, startPos, playState)

    case FlipPlayerOpen =>
      self ! SetPlayerOpen(!playerOpen)

    case SetPlaying(_playing) =>
      mainActorRef ! MainActor.NotifyHandlers(UI.OnLocalPlayingChanged(_playing))
      update(playerOpen, _playing, startPos, playState)

    case FlipPlaying =>
      self ! SetPlaying(!playing)

    case SetStartPos(_startPos) =>
      mainActorRef ! MainActor.NotifyHandlers(UI.OnLocalStartPosChanged(_startPos))
      update(playerOpen, playing, _startPos, playState)

    case SetPlayState(_playState) =>
      mainActorRef ! MainActor.NotifyHandlers(UI.OnStationPlayStateChanged(_playState))
      update(playerOpen, playing, startPos, _playState)

  }

  val receive = mkReceive(false, false, 0, NotPlaying)

}
