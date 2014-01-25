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

  case class SetSelectionFragmentHandler(selectionFragmentHandler: Handler)

  val mainActorRef = ActorSystem("actorSystem").actorOf(Props[MainActor], "mainActor")

}

class MainActor extends Actor with RequiresMessageQueue[UnboundedMessageQueueSemantics] {

  var _mainActivityHandler: Handler = _ 
  var _selectionFragmentHandler: Handler = _ 
  var _selection: Selection = _ 

  val selectionList = List(Tracks, Stations)
  val trackList = List(
    Track("path//1//", "oops i did it again", "oopsy", "brit"), 
    Track("path//2//", "walking in circles", "oopsy", "brit"), 
    Track("path//3//", "frogs are fun", "frogs", "frogger") 
  )
 
  def receive = {

    case MainActor.SetMainActivityHandler(handler: Handler) =>
      _mainActivityHandler = handler
      _mainActivityHandler.obtainMessage(MainActivity.mainActorConnected, selectionList).sendToTarget()

    case MainActor.SetSelectionFragmentHandler(handler: Handler) =>
      _selectionFragmentHandler = handler 
      _selectionFragmentHandler
        .obtainMessage(SelectionFragment.selectionFragmentConnected, trackList)
        .sendToTarget()

    case MainActor.SetSelection(selection: Selection) => 
      _selection = selection
      _mainActivityHandler.obtainMessage(MainActivity.selectionChanged, selection).sendToTarget()

  }

}
