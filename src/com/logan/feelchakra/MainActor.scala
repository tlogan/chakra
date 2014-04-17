package com.logan.feelchakra

import android.util.Log

object MainActor {

  def props(): Props = {
    Props[MainActor]
  }

  //Requests
  case class NotifyHandlers(onChange: UI.OnChange)
  case class Subscribe(key: String, ui: Handler) 
  case class Unsubscribe(key: String) 
  case class SetDatabase(database: Database) 
  case class SetTrackList(trackList: List[Track]) 
  case class SetSelection(selection: Selection) 

  case class ChangeTrackByIndex(trackIndex: Int)
  case class AddTrackToPlaylist(track: Track) 
  case object FlipPlayer
  case class AddStation(station: Station)
  case class CommitStation(device: WifiP2pDevice)
  case class RequestStation(station: Station)
  case object BecomeTheStation
  case object AcceptRemotes 
  case class ConnectRemote(remoteHost: String)

  case class SetCurrentRemoteTrack(track: Track)
  case class SetCurrentRemoteAudio(audioBuffer: Array[Byte])
  case class SetNextRemoteTrack(track: Track)
  case class SetNextRemoteAudio(audioBuffer: Array[Byte])

  case class SetLocalAddress(localAddress: InetSocketAddress)

  case object Advertise
  case object Discover 

}

class MainActor extends Actor {

  import MainActor._
  import UI._
  import scala.concurrent.ExecutionContext.Implicits.global

  private val serverRef: ActorRef = context.actorOf(Server.props(), "Server")
  private val clientRef: ActorRef = context.actorOf(Client.props(), "Client")
  private val networkRef: ActorRef = context.actorOf(Network.props(), "Network")

  private var database: Database = null
  private var uis: Map[String, Handler] = new HashMap[String, Handler]()
  private var selectionManager: SelectionManager = new SelectionManager
  private var trackManager: TrackManager = new TrackManager
  private var stationManager: StationManager = new StationManager
  private var networkProfile: NetworkProfile = new NetworkProfile



  def receive = {

    case NotifyHandlers(onChange) =>
      notifyHandlers(uis, onChange)

    case Subscribe(key, ui) =>
      networkProfile.localAddressOp match {
        case Some(localAddress) => 
          val response = OnProfileChanged(networkProfile)
          ui.obtainMessage(0, response).sendToTarget()
        case None => {}
      }

      List(
        OnSelectionListChanged(selectionManager.list),
        OnPlayerOpenChanged(trackManager.playerOpen),
        OnSelectionChanged(selectionManager.current),
        OnStationOptionChanged(stationManager.currentOp),
        OnDiscoveringChanged(stationManager.discovering),
        OnAdvertisingChanged(stationManager.advertising),
        OnStationListChanged(stationManager.map.values.toList),
        OnTrackIndexChanged(trackManager.currentIndex),
        OnPlaylistChanged(trackManager.playlist),
        OnTrackListChanged(trackManager.list),
        OnTrackOptionChanged(trackManager.currentOp),
        OnPlayStateChanged(true),
        OnPositionChanged(0)
      ).foreach(response => {
        ui.obtainMessage(0, response).sendToTarget()
      })

      uis = uis.+((key, ui))

    case Unsubscribe(key) =>
      uis = uis.-(key)

    case SetDatabase(database) =>
      val trackListFuture = TrackList(database)
      trackListFuture.onComplete({ 
        case Success(trackList) => self ! SetTrackList(trackList)
        case Failure(t) => Log.d("chakra", "trakListFuture failed: " + t.getMessage)
      })
      this.database = database

    case SetTrackList(trackList) =>
      trackManager = trackManager.setList(trackList)

    case SetSelection(selection) => 
      selectionManager = selectionManager.setCurrent(selection)

    case Discover => 
      stationManager = stationManager.setDiscovering(true)

    case FlipPlayer =>
      trackManager = trackManager.flipPlayer()

    case ChangeTrackByIndex(trackIndex) => 
      val current = trackManager.optionByIndex(trackIndex)
      val next = trackManager.optionByIndex(trackIndex + 1)
      if (stationManager.currentOp == None) {
        networkRef ! Network.WriteBothTracks(current, next)
      }
      trackManager = trackManager.setCurrentIndex(trackIndex)

    case AddTrackToPlaylist(track) =>
      trackManager = if (trackManager.playlist.size == 0) {
        if (stationManager.currentOp == None) {
          networkRef ! Network.WriteBothTracks(Some(track), None)
        }
        trackManager.addPlaylistTrack(track)
          .setCurrentIndex(0)
      } else {
        if (trackManager.currentIsLast && stationManager.currentOp == None) {
          networkRef ! Network.WriteNextTrackOp(Some(track))
        }
        trackManager.addPlaylistTrack(track)
      }

    case AddStation(station) =>
      stationManager = stationManager.stageStation(station)

    case CommitStation(device) =>
      stationManager = stationManager.commitStation(device)
     
    case RequestStation(station) =>
      stationManager = {
        stationManager
          .setDiscovering(false)
          .setCurrentOp(Some(station))
      }

    case BecomeTheStation =>
      stationManager = {
        stationManager
          .setCurrentOp(None)
          .setDiscovering(selectionManager.current == StationSelection)
      }

    case SetLocalAddress(localAddress) =>
      networkProfile = networkProfile.setLocalAddress(localAddress)

    case AcceptRemotes =>
      serverRef.!(Server.Accept(networkRef))
    case ConnectRemote(remoteHost) =>
      stationManager.currentOp match {
        case Some(station) =>
          val remoteAddress = 
            new InetSocketAddress(remoteHost, station.record.get("port").toInt)
          clientRef.!(Client.Connect(remoteAddress, networkRef))
        case None => Log.d("chakra", "Can't connect when station Op is NONE")
      }

    case SetCurrentRemoteTrack(track) =>
      stationManager = stationManager.setCurrentRemoteTrack(track)

    case SetCurrentRemoteAudio(audioBuffer) =>
      stationManager = stationManager.setCurrentRemoteAudio(audioBuffer)

    case SetNextRemoteTrack(track) =>
      stationManager = stationManager.setNextRemoteTrack(track)

    case SetNextRemoteAudio(audioBuffer) =>
      stationManager = stationManager.setNextRemoteAudio(audioBuffer)

  }

  private def notifyHandlers(uis: Map[String, Handler], response: OnChange): Unit = {
    uis.foreach(pair => {
      val ui = pair._2
      ui.obtainMessage(0, response).sendToTarget()
    })
  }

}
