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
import scala.collection.immutable.HashMap

import android.os.Handler

import guava.scala.android.Database
import guava.scala.android.Table
import android.util.Log 
import scala.util.{Success,Failure}

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager._

import java.net.InetSocketAddress 

import akka.actor.{Actor, ActorRef, Props}


object MainActor {

  case class SetMainActivityHandler(handler: Handler) 
  case class SetMainActivityDatabase(database: Database) 
  case class SetSelection(selection: Selection) 
  case class SetTrack(track: Track) 
  case class AddTrackToPlaylist(track: Track) 

  case class SetTrackSelectionFragmentHandler(handler: Handler)
  case class SetPlayerFragmentHandler(handler: Handler)
  case object FlipPlayer

  case class SetPlayerServiceHandler(handler: Handler)

  case class SetStationSelectionFragmentHandler(handler: Handler)
  case class AddStation(station: Station)
  case class CommitStation(device: WifiP2pDevice)

  case class SetStation(station: Station)
  case object StartServer
  case class StartClient(remoteHost: String)

  case class SetRemoteTrack(track: String)

  val mainActorRef = ActorSystem("actorSystem").actorOf(Props[MainActor], "mainActor")

}

class MainActor extends Actor with RequiresMessageQueue[UnboundedMessageQueueSemantics] {

  var _mainActivityHandler: Handler = _ 
  var _playerFragmentHandler: Handler = _ 
  var _playerServiceHandler: Handler = _ 
  var _mainActivityDatabase: Database = _ 
  var _selection: Selection = _ 
  var _trackIndex: Int = -1 
  var _playlist: List[Track] = List()

  var _playerOpen: Boolean = false 

  //station selection fragment handler (FH)
  var _stationSelectionFH: Handler = _ 
  var _stationMap: Map[String, Station] = HashMap[String, Station]()
  var _stagedStationMap: Map[String, Station] = HashMap[String, Station]()

  //track selection fragment handler option (FHO)
  var _trackSelectionFHO: Option[Handler] = None 
  var _trackList: List[Track] = List()

  val selectionList = List(TrackSelection, StationSelection)

  var _stationOption: Option[Station] = None 
  val serviceName: String = "_chakra" 
  val serviceType: String = "_syncstream._tcp" 

  var _serverRefOp: Option[ActorRef] = None
  var _clientRef: ActorRef = _

  val localAddress = new InetSocketAddress("localhost", 0)

  import MainActor._
  def receive = {

    case SetMainActivityHandler(handler) =>
      _mainActivityHandler = handler
      _mainActivityHandler.obtainMessage(0, MainActivity.OnMainActorConnected(selectionList, _playerOpen)).sendToTarget()


    case SetMainActivityDatabase(database) =>
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

    case SetTrackSelectionFragmentHandler(handler) =>
      _trackSelectionFHO = Some(handler) 
      handler.obtainMessage(0, TrackSelectionFragment.OnMainActorConnected(_trackList))
        .sendToTarget()

    case SetSelection(selection) => 
      _selection = selection
      _mainActivityHandler.obtainMessage(0, MainActivity.OnSelectionChanged(selection)).sendToTarget()


    case SetPlayerFragmentHandler(handler) =>
      _playerFragmentHandler = handler
      handler.obtainMessage(0, PlayerFragment.OnMainActorConnected(_trackIndex, _playlist))
        .sendToTarget()

    case SetPlayerServiceHandler(handler) => {
      _playerServiceHandler = handler
      handler.obtainMessage(0, PlayerService.OnMainActorConnected(_playlist.lift(_trackIndex), true, 0)).sendToTarget()

      handler.obtainMessage(0, 
        PlayerService.OnProfileChanged(localAddress, serviceName, serviceType)
      ).sendToTarget()

      handler.obtainMessage(0, 
        PlayerService.OnStationOptionChanged(_stationOption)
      ).sendToTarget()

    }

    case SetTrack(track) => {
      if (!_playlist.contains(track)) {
        _playlist = forkTrack(track)
      } 

      _trackIndex = _playlist.indexOf(track) 

      _playerFragmentHandler
        .obtainMessage(0, PlayerFragment.OnPlayListChanged(_trackIndex, _playlist))
        .sendToTarget()

      _playerServiceHandler.obtainMessage(0, PlayerService.OnTrackOptionChanged( _playlist.lift(_trackIndex) )).sendToTarget()
    }

    case AddTrackToPlaylist(track) =>

      _playlist = forkTrack(track)
      if (_trackIndex < 0) {
        _trackIndex = 0
        _playerServiceHandler.obtainMessage(0, PlayerService.OnTrackOptionChanged(
          _playlist.lift(_trackIndex) 
        )).sendToTarget()
      }
      _playerFragmentHandler
        .obtainMessage(0, PlayerFragment.OnPlayListChanged(_trackIndex, _playlist))
        .sendToTarget()

    case FlipPlayer =>
      _playerOpen = !_playerOpen
      _mainActivityHandler.obtainMessage(0, MainActivity.OnPlayerFlipped(_playerOpen)).sendToTarget()
      _playerFragmentHandler.obtainMessage(0, PlayerFragment.OnPlayerFlipped(_playerOpen))
        .sendToTarget()

    case SetStationSelectionFragmentHandler(handler) =>
      _stationSelectionFH = handler
      handler.obtainMessage(0, StationSelectionFragment.OnMainActorConnected(_stationMap.values.toList))
        .sendToTarget()

    case AddStation(station) =>
      _stagedStationMap = _stagedStationMap.+(station.device.deviceAddress -> station)

    case CommitStation(device) =>
     if (_stagedStationMap.isDefinedAt(device.deviceAddress)) {
       _stationMap = _stationMap.+((device.deviceAddress, _stagedStationMap(device.deviceAddress)))
       _stationSelectionFH
         .obtainMessage(0, StationSelectionFragment.OnStationListChanged(_stationMap.values.toList))
         .sendToTarget()
     }

     
    case SetStation(station) =>
      _stationOption = Some(station)

      _stationSelectionFH.obtainMessage(0, 
        StationSelectionFragment.OnStationOptionChanged(_stationOption)
      ).sendToTarget()

      _playerServiceHandler.obtainMessage(0, 
        PlayerService.OnStationOptionChanged(_stationOption)
      ).sendToTarget()

    case StartServer =>
      _serverRefOp match {
        case Some(serverRef) => Log.d("StartServer", "Some:" + serverRef.toString)
        case None => 
          Log.d("StartServer", "None")
          _serverRefOp = Some(context.actorOf(ServerConnector.props(localAddress), "ServerConnector"))
          Log.d("StartServer", "Post actor creation - None")
      }
    case StartClient(remoteHost) =>
      _stationOption match {
        case Some(station) =>
          val remoteAddress = new InetSocketAddress(remoteHost, station.record.get("port").toInt)
          _clientRef = context.actorOf(ClientConnector.props(remoteAddress), "ClientConnector")
          case None => {
            Log.d("StartClient: _stationOption", "None")
          }
      }

    case SetRemoteTrack(track) =>
      _playerServiceHandler.obtainMessage(
        0, 
        PlayerService.OnRemoteTrackChanged(track)
      ).sendToTarget()

  }

  private def forkTrack(track: Track): List[Track] = {
    _serverRefOp match {
      case Some(serverRef) => serverRef.!(ServerConnector.OnNextTrack(track))
      case None => {}
    }
    _playlist.:+(track)
  }

}
