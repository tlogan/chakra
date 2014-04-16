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


  def receive = receiveAll(
    null, new HashMap[String, Handler](),
    new SelectionManager, new TrackManager,
    new StationManager, new NetworkProfile
  )

  def receiveAll(
    database: Database,
    uis: Map[String, Handler],
    selectionManager: SelectionManager,
    trackManager: TrackManager,
    stationManager: StationManager,
    networkProfile: NetworkProfile
  ): Receive = {

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

      val u = uis.+((key, ui))
      context.become(receiveAll(
        database, u, selectionManager,
        trackManager, stationManager, networkProfile
      ))

    case Unsubscribe(key) =>
      context.become(receiveAll(
        database, uis.-(key), 
        selectionManager, trackManager,
        stationManager, networkProfile
      ))

    case SetDatabase(database) =>
      val trackListFuture = TrackList(database)
      trackListFuture.onComplete({ 
        case Success(trackList) => self ! SetTrackList(trackList)
        case Failure(t) => Log.d("chakra", "trakListFuture failed: " + t.getMessage)
      })
      context.become(receiveAll(
        database, uis, selectionManager,
        trackManager, stationManager, networkProfile 
      ))

    case SetTrackList(trackList) =>
      context.become(receiveAll(
        database, uis, selectionManager,
        trackManager.setList(trackList),
        stationManager, networkProfile 
      ))

    case SetSelection(selection) => 
      context.become(receiveAll(
        database, uis, selectionManager.setCurrent(selection),
        trackManager, stationManager, networkProfile
      ))

    case Discover => 
      context.become(receiveAll(
        database, uis, selectionManager, trackManager, 
        stationManager.setDiscovering(true), networkProfile
      ))

    case FlipPlayer =>
      context.become(receiveAll(
        database, uis, selectionManager,
        trackManager.flipPlayer(),
        stationManager, networkProfile
      ))

    case ChangeTrackByIndex(trackIndex) => 
      val current = trackManager.optionByIndex(trackIndex)
      val next = trackManager.optionByIndex(trackIndex + 1)
      if (stationManager.currentOp == None) {
        networkRef ! Network.WriteBothTracks(current, next)
      }
      context.become(receiveAll(
        database, uis, selectionManager,
        trackManager.setCurrentIndex(trackIndex),
        stationManager, networkProfile
      ))

    case AddTrackToPlaylist(track) =>
      val newTrackManager = if (trackManager.playlist.size == 0) {
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

      context.become(receiveAll(
        database, uis, selectionManager, newTrackManager,
        stationManager, networkProfile
      ))

    case AddStation(station) =>
      context.become(receiveAll(
        database, uis, selectionManager, trackManager,
        stationManager.stageStation(station), networkProfile
      ))

    case CommitStation(device) =>
      context.become(receiveAll(
        database, uis, selectionManager, trackManager,
        stationManager.commitStation(device), networkProfile
      ))
     
    case RequestStation(station) =>
      context.become(receiveAll(
        database, uis, selectionManager, trackManager,
        stationManager.setDiscovering(false)
          .setCurrentOp(Some(station)),
        networkProfile
      ))

    case BecomeTheStation =>
      context.become(receiveAll(
        database, uis, selectionManager, trackManager,
        stationManager.setCurrentOp(None)
          .setDiscovering(selectionManager.current == StationSelection),
        networkProfile
      ))

    case SetLocalAddress(localAddress) =>
      context.become(receiveAll(
        database, uis, selectionManager, 
        trackManager, stationManager,
        networkProfile.setLocalAddress(localAddress)
      ))

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
      context.become(receiveAll(
        database, uis, selectionManager, trackManager, 
        stationManager.setCurrentRemoteTrack(track), 
        networkProfile
      ))

    case SetCurrentRemoteAudio(audioBuffer) =>
      context.become(receiveAll(
        database, uis, selectionManager, trackManager, 
        stationManager.setCurrentRemoteAudio(audioBuffer), 
        networkProfile
      ))

    case SetNextRemoteTrack(track) =>
      context.become(receiveAll(
        database, uis, selectionManager, trackManager, 
        stationManager.setNextRemoteTrack(track), 
        networkProfile
      ))


    case SetNextRemoteAudio(audioBuffer) =>
      context.become(receiveAll(
        database, uis, selectionManager, trackManager, 
        stationManager.setNextRemoteAudio(audioBuffer), 
        networkProfile
      ))

  }

  private def notifyHandlers(uis: Map[String, Handler], response: OnChange): Unit = {
    uis.foreach(pair => {
      val ui = pair._2
      ui.obtainMessage(0, response).sendToTarget()
    })
  }

}
