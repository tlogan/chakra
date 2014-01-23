package com.feelchakra

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import akka.dispatch.RequiresMessageQueue
import akka.dispatch.UnboundedMessageQueueSemantics

import android.os.Handler
 
object MainActor {
  case class SetMainActivityHandler(mainActivityHandler: Handler) 
  case class SetSelection(selection: Selection) 

  val mainActorRef = ActorSystem("actorSystem").actorOf(Props[MainActor], "mainActor")

}

class MainActor extends Actor with RequiresMessageQueue[UnboundedMessageQueueSemantics] {

  var _mainActivityHandler: Handler = _ 
  var _selection: Selection = _ 
  val selectionList = List(Tracks, Stations)
 
  def receive = {
    case MainActor.SetSelection(selection: Selection) => 
      _selection = selection
      _mainActivityHandler.obtainMessage(MainActivity.SelectionChanged, selection).sendToTarget()
    case MainActor.SetMainActivityHandler(handler: Handler) =>
      _mainActivityHandler = handler
      _mainActivityHandler.obtainMessage(MainActivity.MainActorConnected, selectionList).sendToTarget()
  }
}
