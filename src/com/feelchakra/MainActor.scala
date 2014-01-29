package com.feelchakra

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import akka.dispatch.RequiresMessageQueue
import akka.dispatch.UnboundedMessageQueueSemantics

import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.Observer
import scala.concurrent.Future
import android.provider.MediaStore 

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ListBuffer

import android.os.Handler

import guava.android.Database
import guava.android.Table
import android.util.Log 
import scala.util.{Success,Failure}
object MainActor {


  case class SetMainActivityHandler(mainActivityHandler: Handler) 
  case class SetMainActivityDatabase(database: Database) 
  case class SetSelection(selection: Selection) 

  case class SetTrackSelectionFragmentHandler(selectionFragmentHandler: Handler)

  val mainActorRef = ActorSystem("actorSystem").actorOf(Props[MainActor], "mainActor")

}

class MainActor extends Actor with RequiresMessageQueue[UnboundedMessageQueueSemantics] {

  var _mainActivityHandler: Handler = _ 
  var _mainActivityDatabase: Database = _ 
  var _selection: Selection = _ 

  //track selection fragment handler option (FHO)
  var _trackSelectionFHO: Option[Handler] = None 
  var _trackList: List[Track] = List()

  val selectionList = List(TrackSelection, StationSelection)
 
  def receive = {

    case MainActor.SetMainActivityHandler(handler: Handler) =>
      _mainActivityHandler = handler
      _mainActivityHandler.obtainMessage(MainActivity.mainActorConnected, selectionList).sendToTarget()

    case MainActor.SetMainActivityDatabase(database: Database) =>
      _mainActivityDatabase = database 
      val trackListFuture = TrackList(database)
      trackListFuture onComplete { 
        case Success(trackList: List[Track]) => {
          _trackList = trackList
          _trackSelectionFHO match {
            case Some(handler) =>
              handler.obtainMessage(TrackSelectionFragment.trackListChanged, _trackList)
              .sendToTarget()
            case None =>
              Log.d("trackSelectionFHO", "None")
          }
          Log.d("trackListFuture", "Success")
        }
        case Failure(t) => Log.d("trackListFuture", "failed: " + t.getMessage)
      }

    case MainActor.SetTrackSelectionFragmentHandler(handler: Handler) =>
      _trackSelectionFHO = Some(handler) 
      handler.obtainMessage(TrackSelectionFragment.mainActorConnected, _trackList)
        .sendToTarget()

    case MainActor.SetSelection(selection: Selection) => 
      _selection = selection
      _mainActivityHandler.obtainMessage(MainActivity.selectionChanged, selection).sendToTarget()

  }

}
