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

  //Requests
  case class Subscribe(handler: Handler) 
  case class SetMainActivityDatabase(database: Database) 
  case class SetSelection(selection: Selection) 

  case class ChangeTrackByIndex(trackIndex: Int)
  case class AddTrackToPlaylist(track: Track) 
  case object FlipPlayer
  case class AddStation(station: Station)
  case class CommitStation(device: WifiP2pDevice)
  case class SetStation(station: Station)
  case object StartServer
  case class StartClient(remoteHost: String)
  case class SetRemoteTrack(track: Track)

  val mainActorRef = ActorSystem("actorSystem").actorOf(Props[MainActor], "mainActor")

}

class MainActor extends Actor with RequiresMessageQueue[UnboundedMessageQueueSemantics] {

  import MainActor._
  import OutputHandler._

  private var _handlers: List[Handler] = List()
  private def notifyHandlers(response: OnChange): Unit = {
    _handlers.foreach(handler => {
      handler.obtainMessage(0, response).sendToTarget()
    })
  }

  private var _mainActivityDatabase: Database = _ 
  private var _selection: Selection = TrackSelection 
  private var _trackIndex: Int = -1 
  private var _playlist: List[Track] = List()
  private var _playerOpen: Boolean = false 
  private var _stationMap: Map[String, Station] = HashMap[String, Station]()
  private var _stagedStationMap: Map[String, Station] = HashMap[String, Station]()
  private var _trackSelectionFHO: Option[Handler] = None 
  private var _trackList: List[Track] = List()
  private val selectionList = List(TrackSelection, StationSelection)
  private var _stationOption: Option[Station] = None 
  private val serviceName: String = "_chakra" 
  private val serviceType: String = "_syncstream._tcp" 
  private var _serverRefOp: Option[ActorRef] = None
  private var _clientRefOp: Option[ActorRef] = None 
  private val localAddress = new InetSocketAddress("localhost", 0)
  private def trackOption: Option[Track] = _playlist.lift(_trackIndex)

  private def setDatabase(database: Database): Unit = {
    _mainActivityDatabase = database 
    val trackListFuture = TrackList(database)
    trackListFuture.onComplete({ 
      case Success(trackList) => setTrackList(trackList)
      case Failure(t) => Log.d("trackListFuture", "failed: " + t.getMessage)
    })
  }

  private def changeTrackByIndex(trackIndex: Int): Unit = {
    setTrackIndex(trackIndex)
    notifyHandlers(OnTrackOptionChanged(trackOption))
  }
  private def setTrackList(trackList: List[Track]): Unit = {
    _trackList = trackList
    notifyHandlers(OnTrackListChanged(_trackList))
  }


  private def setSelection(selection: Selection): Unit = {
    _selection = selection
    notifyHandlers(OnSelectionChanged(_selection))
  }

  private def setPlayerOpen(playerOpen: Boolean): Unit = {
    _playerOpen = playerOpen
    notifyHandlers(OnPlayerOpenChanged(_playerOpen))
  }

  private def setPlaylist(playlist: List[Track]): Unit = {
    _playlist = playlist 
    notifyHandlers(OnPlaylistChanged(_playlist))
  }

  private def setTrackIndex(trackIndex: Int): Unit = {
    _trackIndex = trackIndex 
    notifyHandlers(OnTrackIndexChanged(_trackIndex))
  }

  private def setStagedStationMap(map: Map[String, Station]): Unit = {
    _stagedStationMap = map 
  }

  private def setStationMap(map: Map[String, Station]): Unit = {
    _stationMap = map
    notifyHandlers(OnStationListChanged(_stationMap.values.toList))
  }

  private def setStationOption(stationOption: Option[Station]): Unit = {
    _stationOption = stationOption
    notifyHandlers(OnStationOptionChanged(_stationOption))
  }


  private def setRemoteTrack(track: Track): Unit = {
    notifyHandlers(OnRemoteTrackChanged(track))
  }

  private def forkTrack(track: Track): List[Track] = {
    _serverRefOp match {
      case Some(serverRef) => serverRef.!(ServerConnector.OnNextTrack(track))
      case None => {}
    }
    _playlist.:+(track)
  }

  def receive = {

    case Subscribe(handler) =>
      List(
        OnSelectionListChanged(selectionList),
        OnPlayerOpenChanged(_playerOpen),
        OnSelectionChanged(_selection),
        OnStationOptionChanged(_stationOption),
        OnStationListChanged(_stationMap.values.toList),
        OnTrackIndexChanged(_trackIndex),
        OnPlaylistChanged(_playlist),
        OnPlayerOpenChanged(_playerOpen),
        OnSelectionChanged(_selection),
        OnTrackListChanged(_trackList),
        OnTrackOptionChanged(trackOption),
        OnPlayStateChanged(true),
        OnPositionChanged(0),
        OnProfileChanged(localAddress, serviceName, serviceType)
      ).foreach(response => {
        handler.obtainMessage(0, response).sendToTarget()
      })
      _handlers = _handlers.:+(handler)

    case SetMainActivityDatabase(database) =>
      setDatabase(database)

    case SetSelection(selection) => 
      setSelection(selection)

    case FlipPlayer =>
      setPlayerOpen(!_playerOpen)

    case ChangeTrackByIndex(trackIndex) => 
      changeTrackByIndex(trackIndex)

    case AddTrackToPlaylist(track) =>
      setPlaylist(forkTrack(track))
      if (_trackIndex < 0) {
        changeTrackByIndex(0)
      }

    case AddStation(station) =>
      setStagedStationMap(_stagedStationMap.+(station.device.deviceAddress -> station))

    case CommitStation(device) =>
     if (_stagedStationMap.isDefinedAt(device.deviceAddress)) {
       val station = _stagedStationMap(device.deviceAddress)
       setStationMap(_stationMap.+((device.deviceAddress, station)))
     }

     
    case SetStation(station) =>
      _stationOption = Some(station)
      setStationOption(Some(station))

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
          _clientRefOp = Some(context.actorOf(ClientConnector.props(remoteAddress), "ClientConnector"))
          case None => {
            Log.d("StartClient: _stationOption", "None")
          }
      }

    case SetRemoteTrack(track) =>
      setRemoteTrack(track)

  }




}
