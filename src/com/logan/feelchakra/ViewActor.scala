package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object ViewActor {

  def props(): Props = Props[ViewActor]

  case class Subscribe(ui: Handler)
  case class SetModHeight(modHeight: Int)

}

class ViewActor extends Actor {

  import ViewActor._

  def update(modHeight: Int) = {
    context.become(mkReceive(modHeight))
  }

  def mkReceive(modHeight: Int): Receive = {

    case Subscribe(ui) =>
      List(
        UI.OnModHeightChanged(modHeight)
      ).foreach(m => notifyHandler(ui, m))

    case SetModHeight(_modHeight) =>
      mainActorRef ! MainActor.NotifyHandlers(UI.OnModHeightChanged(_modHeight))
      update(_modHeight)

  }

  val receive = mkReceive(0)

}
