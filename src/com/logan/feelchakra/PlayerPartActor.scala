package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object PlayerPartActor {

  def props(): Props = Props[PlayerPartActor]

  case class Subscribe(ui: Handler)
  case class SetPlayerPart(playerPart: PlayerPart)

}

class PlayerPartActor extends Actor {

  import PlayerPartActor._

  def update(playerPart: PlayerPart, playerPartList: List[PlayerPart]) = {
    context.become(mkReceive(playerPart, playerPartList))
  }

  def mkReceive(playerPart: PlayerPart, playerPartList: List[PlayerPart]): Receive = {

    case Subscribe(ui) =>
      List(
        UI.OnPlayerPartListChanged(playerPartList),
        UI.OnPlayerPartChanged(playerPart)
      ).foreach(m => notifyHandler(ui, m))

    case SetPlayerPart(_playerPart) =>
      mainActorRef ! MainActor.NotifyHandlers(UI.OnPlayerPartChanged(_playerPart))
      update(_playerPart, playerPartList)

  }

  val receive = mkReceive(
      PlayerQueue,
      List(PlayerQueue, PlayerChat)
  )

}
