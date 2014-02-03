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

import guava.scala.android.Database
import guava.scala.android.Table
import android.util.Log 
import scala.util.{Success,Failure}
object MainActor {

  case class SetMainActivityHandler(handler: Handler) 
  case class SetMainActivityDatabase(database: Database) 
  case class SetSelection(selection: Selection) 
  case class SetTrack(track: Track) 
  case class AddTrackToPlaylist(track: Track) 

  case class SetTrackSelectionFragmentHandler(handler: Handler)
  case class SetPlayerFragmentHandler(handler: Handler)
  case object FlipPlayer

  val mainActorRef = ActorSystem("actorSystem").actorOf(Props[MainActor], "mainActor")

}

class MainActor extends Actor with RequiresMessageQueue[UnboundedMessageQueueSemantics] {

  var _mainActivityHandler: Handler = _ 
  var _playerFragmentHandler: Handler = _ 
  var _mainActivityDatabase: Database = _ 
  var _selection: Selection = _ 
  var _trackOption: Option[Track] = None
  var _playlist: List[Track] = List()

  var _playerOpen: Boolean = false 

  //track selection fragment handler option (FHO)
  var _trackSelectionFHO: Option[Handler] = None 
  var _trackList: List[Track] = List()

  val selectionList = List(TrackSelection, StationSelection)
 
  def receive = {

    case MainActor.SetMainActivityHandler(handler: Handler) =>
      _mainActivityHandler = handler
      _mainActivityHandler.obtainMessage(0, MainActivity.OnMainActorConnected(selectionList, _playerOpen)).sendToTarget()


    case MainActor.SetMainActivityDatabase(database: Database) =>
      _mainActivityDatabase = database 
      val trackListFuture = TrackList(database)
      trackListFuture onComplete { 
        case Success(trackList: List[Track]) => {
          _trackList = trackList
          _trackSelectionFHO match {
            case Some(handler) =>
              handler.obtainMessage(0, TrackSelectionFragment.OnTrackListChanged(_trackList))
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
      handler.obtainMessage(0, TrackSelectionFragment.OnMainActorConnected(_trackList))
        .sendToTarget()

    case MainActor.SetSelection(selection: Selection) => 
      _selection = selection
      _mainActivityHandler.obtainMessage(0, MainActivity.OnSelectionChanged(selection)).sendToTarget()


    case MainActor.SetPlayerFragmentHandler(handler: Handler) =>
      _playerFragmentHandler = handler
      handler.obtainMessage(0, PlayerFragment.OnMainActorConnected(_trackOption, _playlist))
        .sendToTarget()

    case MainActor.SetTrack(track: Track) => 
      
      if (!_playlist.contains(track)) {
        _playlist = _playlist.:+(track)
      }

      _trackOption = Some(track) 
      _playerFragmentHandler
        .obtainMessage(0, PlayerFragment.OnPlayListChanged(_trackOption, _playlist))
        .sendToTarget()
      _playerFragmentHandler.obtainMessage(0, PlayerFragment.OnTrackOptionChanged(_trackOption))
        .sendToTarget()

    case MainActor.AddTrackToPlaylist(track) =>

      if (_trackOption == None) {
        _trackOption = Some(track)
        _playerFragmentHandler
          .obtainMessage(0, PlayerFragment.OnTrackOptionChanged(_trackOption))
          .sendToTarget()
      }
      _playlist = _playlist.:+(track)
      _playerFragmentHandler
        .obtainMessage(0, PlayerFragment.OnPlayListChanged(_trackOption, _playlist))
        .sendToTarget()

    case MainActor.FlipPlayer =>
      _playerOpen = !_playerOpen
      _mainActivityHandler.obtainMessage(0, MainActivity.OnPlayerFlipped(_playerOpen)).sendToTarget()
      _playerFragmentHandler.obtainMessage(0, PlayerFragment.OnPlayerFlipped(_playerOpen))
        .sendToTarget()

      /*
      _trackSelectionFHO match {
        case Some(handler) =>
          handler.obtainMessage(TrackSelectionFragment.trackChanged, _trackList)
            .sendToTarget()
        case None =>
          Log.d("trackSelectionFHO", "None when setting track")
      }
      */

  }

}
