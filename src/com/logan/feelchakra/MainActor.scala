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
  case class SetCacheDir(cacheDir: File) 

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
  case class SetNextRemoteTrack(track: Track)
  case class SetRemoteAudio(audioBuffer: Array[Byte])
  case object SetRemoteAudioDone

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
  private var cacheDir: File = null
  private var uis: Map[String, Handler] = new HashMap[String, Handler]()
  private var selectionManager: SelectionManager = new SelectionManager
  private var localManager: LocalManager = new LocalManager 
  private var stationManager: StationManager = new StationManager
  private var networkProfile: NetworkProfile = new NetworkProfile

  private var remoteManager: RemoteManager = _ 

  private var playState: PlayState = new PlayState 

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
        OnPlayerOpenChanged(localManager.playerOpen),
        OnSelectionChanged(selectionManager.current),
        OnStationOptionChanged(stationManager.currentOp),
        OnDiscoveringChanged(stationManager.discovering),
        OnAdvertisingChanged(stationManager.advertising),
        OnStationListChanged(stationManager.map.values.toList),
        OnTrackIndexChanged(localManager.currentIndex),
        OnPlaylistChanged(localManager.playlist),
        OnTrackListChanged(localManager.list),
        OnTrackOptionChanged(localManager.currentOp),
        OnPlayingChanged(playState.playing),
        OnStartPosChanged(playState.startPos)
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

    case SetCacheDir(cacheDir) => 
      this.cacheDir = cacheDir
      remoteManager = new RemoteManager(cacheDir)

    case SetTrackList(trackList) =>
      localManager = localManager.setList(trackList)

    case SetSelection(selection) => 
      selectionManager = selectionManager.setCurrent(selection)

    case Discover => 
      stationManager = stationManager.setDiscovering(true)

    case FlipPlayer =>
      localManager = localManager.flipPlayer()

    case ChangeTrackByIndex(trackIndex) => 
      val current = localManager.optionByIndex(trackIndex)
      val next = localManager.optionByIndex(trackIndex + 1)
      if (stationManager.currentOp == None) {
        networkRef ! Network.WriteBothTracks(current, next)
      }
      localManager = localManager.setCurrentIndex(trackIndex)
      playState = playState.setStartPos(0).setPlaying(true)
      networkRef ! Network.WritePlayState(playState)

    case AddTrackToPlaylist(track) =>
      if (localManager.playlist.size == 0) {
        if (stationManager.currentOp == None) {
          networkRef ! Network.WriteBothTracks(Some(track), None)
        }
        localManager = localManager.addPlaylistTrack(track)
          .setCurrentIndex(0)

        playState = playState.setStartPos(0)
        playState = playState.setPlaying(true)
        networkRef ! Network.WritePlayState(playState)

      } else {
        if (localManager.currentIsLast && stationManager.currentOp == None) {
          networkRef ! Network.WriteNextTrackOp(Some(track))
        }
        localManager = localManager.addPlaylistTrack(track)
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
      playState = playState.setPlaying(false)
      stationManager.currentOp match {
        case Some(station) =>
          val remoteAddress = 
            new InetSocketAddress(remoteHost, station.record.get("port").toInt)
          clientRef.!(Client.Connect(remoteAddress, networkRef))
        case None => Log.d("chakra", "Can't connect when station Op is NONE")
      }

    case SetCurrentRemoteTrack(track) =>
      remoteManager = remoteManager.setCurrentTrackOp(track)

    case SetNextRemoteTrack(track) =>

    case SetRemoteAudio(audioBuffer) =>
      remoteManager.addAudio(audioBuffer)

    case SetRemoteAudioDone =>
      remoteManager.close()

  }

  private def notifyHandlers(uis: Map[String, Handler], response: OnChange): Unit = {
    uis.foreach(pair => {
      val ui = pair._2
      ui.obtainMessage(0, response).sendToTarget()
    })
  }


}
