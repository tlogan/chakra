package com.logan.feelchakra

import android.util.Log

object MainActor {

  //Requests
  case class Subscribe(key: String, handler: Handler) 
  case class Unsubscribe(key: String) 
  case class SetMainActivityDatabase(database: Database) 
  case class SetSelection(selection: Selection) 

  case class ChangeTrackByIndex(trackIndex: Int)
  case class AddTrackToPlaylist(track: Track) 
  case object FlipPlayer
  case class AddStation(station: Station)
  case class CommitStation(device: WifiP2pDevice)
  case class ConnectToStation(station: Station)
  case object BecomeTheStation
  case object StartServer
  case class StartClient(remoteHost: String)
  case class SetRemoteTrack(track: Track)

  case object Advertise

  val mainActorRef = ActorSystem("actorSystem").actorOf(Props[MainActor], "mainActor")

}

class MainActor extends Actor {

  import MainActor._
  import OutputHandler._
  import scala.concurrent.ExecutionContext.Implicits.global


  private var _handlers: HashMap[String, Handler] = HashMap()
  private def notifyHandlers(response: OnChange): Unit = {
    _handlers.foreach(pair => {
      val handler = pair._2
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
  private var _discovering: Boolean = false 
  private var _advertising: Boolean = false 
  private var _stationOption: Option[Station] = None 
  private val serviceName: String = "_chakra" 
  private val serviceType: String = "_syncstream._tcp" 
  private var _serverRefOp: Option[ActorRef] = None
  private var _clientRefOp: Option[ActorRef] = None 
  private val localAddress = new InetSocketAddress("localhost", 0)
  private def trackOption: Option[Track] = _playlist.lift(_trackIndex)

  def receive = {

    case Subscribe(key, handler) =>
      List(
        OnProfileChanged(localAddress, serviceName, serviceType),
        OnSelectionListChanged(selectionList),
        OnPlayerOpenChanged(_playerOpen),
        OnSelectionChanged(_selection),
        OnStationOptionChanged(_stationOption),
        OnDiscoveringChanged(_discovering),
        OnAdvertisingChanged(_advertising),
        OnStationListChanged(_stationMap.values.toList),
        OnTrackIndexChanged(_trackIndex),
        OnPlaylistChanged(_playlist),
        OnPlayerOpenChanged(_playerOpen),
        OnSelectionChanged(_selection),
        OnTrackListChanged(_trackList),
        OnTrackOptionChanged(trackOption),
        OnPlayStateChanged(true),
        OnPositionChanged(0)
      ).foreach(response => {
        handler.obtainMessage(0, response).sendToTarget()
      })
      Log.d("chakra", "subscribing " + key)
      _handlers = _handlers.+((key, handler))

    case Unsubscribe(key) =>
      Log.d("chakra", "ubsubscribing " + key)
      _handlers = _handlers.-(key)
      Log.d("chakra", "handlers size: " + _handlers.size)

    case SetMainActivityDatabase(database) =>
      setDatabase(database)

    case SetSelection(selection) => 
      setSelection(selection)
      selection match {
        case TrackSelection => setDiscovering(false)
        case StationSelection => setDiscovering(_stationOption == None)
      }
      

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
     
    case ConnectToStation(station) =>
      setDiscovering(false)
      setStationOption(Some(station))

    case BecomeTheStation =>
      setStationOption(None)
      setDiscovering(_selection == StationSelection)

    case StartServer =>
      _serverRefOp match {
        case Some(serverRef) => Log.d("chakra", "Some:" + serverRef.toString)
        case None => 
          Log.d("chakra", "None")
          _serverRefOp = Some(context.actorOf(ServerConnector.props(localAddress), "ServerConnector"))
          Log.d("chakra", "Post actor creation - None")
      }
    case StartClient(remoteHost) =>
      _stationOption match {
        case Some(station) =>
          val remoteAddress = new InetSocketAddress(remoteHost, station.record.get("port").toInt)
          _clientRefOp = Some(context.actorOf(ClientConnector.props(remoteAddress), "ClientConnector"))
          case None => {
            Log.d("chakra", " _stationOption None")
          }
      }

    case SetRemoteTrack(track) =>
      setRemoteTrack(track)

  }


  private def setDatabase(database: Database): Unit = {
    _mainActivityDatabase = database 
    val trackListFuture = TrackList(database)
    trackListFuture.onComplete({ 
      case Success(trackList) => setTrackList(trackList)
      case Failure(t) => Log.d("chakra", "trakListFuture failed: " + t.getMessage)
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

  private def setDiscovering(discovering: Boolean): Unit = {
    Log.d("chakra", "discovering " + discovering)
    _discovering = discovering
    notifyHandlers(OnDiscoveringChanged(_discovering))
  }

  private def setAdvertising(advertising: Boolean): Unit = {
    Log.d("chakra", "advertising " + advertising)
    _advertising = advertising
    notifyHandlers(OnAdvertisingChanged(_advertising))
  }





}
