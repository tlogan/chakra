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
  case class AddLocalAudioBuffer(audioBuffer: Array[Byte])
  case object EndLocalAudioBuffer

  case class AddTrackToPlaylist(track: Track) 
  case object FlipPlayer
  case class AddStation(station: Station)
  case class CommitStation(device: WifiP2pDevice)
  case class RequestStation(station: Station)
  case object BecomeTheStation
  case object AcceptListeners 
  case class ConnectStation(remoteHost: String)

  case class SetLocalAddress(localAddress: InetSocketAddress)

  case object Advertise
  case object Discover 

  case class SetStationTrack(track: Track)
  case class AddStationAudioBuffer(audioBuffer: Array[Byte])
  case object EndStationAudioBuffer
  case class SetStationPlayState(playState: PlayState)

}

class MainActor extends Actor {

  import MainActor._
  import UI._
  import scala.concurrent.ExecutionContext.Implicits.global

  private val serverRef: ActorRef = context.actorOf(Server.props(), "Server")
  private val clientRef: ActorRef = context.actorOf(Client.props(), "Client")

  private val listenerNetworkRef: ActorRef = context.actorOf(ListenerNetwork.props(), "ListenerNetwork")
  private val stationMessengerRef: ActorRef = context.actorOf(StationMessenger.props(), "StationMessenger")

  private var database: Database = null
  private var cacheDir: File = null
  private var uis: Map[String, Handler] = new HashMap[String, Handler]()
  private var selectionManager: SelectionManager = new SelectionManager
  private var localManager: LocalManager = new LocalManager 
  private var stationManager: StationManager = new StationManager
  private var networkProfile: NetworkProfile = new NetworkProfile

  private var fileOutputOp: Option[BufferedOutputStream] = None 

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

        OnLocalTrackOptionChanged(localManager.currentOp),
        OnLocalStartPosChanged(localManager.startPos),
        OnLocalPlayingChanged(localManager.playing)

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
      localManager = localManager.setCurrentIndex(trackIndex)
      localManager = localManager.setStartPos(0).setPlaying(true)
      if (stationManager.currentOp == None) {
        listenerNetworkRef ! ListenerNetwork.NotifyMessengers(Messenger.WriteTrackOp(current))
        listenerNetworkRef ! ListenerNetwork.NotifyMessengers(Messenger.WritePlayState(Playing(Platform.currentTime)))
      }

    case AddLocalAudioBuffer(audioBuffer) =>
      listenerNetworkRef ! ListenerNetwork.NotifyMessengers(Messenger.WriteAudioBuffer(audioBuffer))

    case EndLocalAudioBuffer =>
      listenerNetworkRef ! ListenerNetwork.NotifyMessengers(Messenger.WriteAudioDone)
      Log.d("chakra", "Audio Done")

    case AddTrackToPlaylist(track) =>
      if (localManager.playlist.size == 0) {
        if (stationManager.currentOp == None) {
          listenerNetworkRef ! ListenerNetwork.NotifyMessengers(Messenger.WriteTrackOp(Some(track)))
          listenerNetworkRef ! ListenerNetwork.NotifyMessengers(Messenger.WritePlayState(Playing(Platform.currentTime)))
        }
        localManager = localManager.addPlaylistTrack(track).setCurrentIndex(0)
        localManager = localManager.setStartPos(0).setPlaying(true)

      } else {
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

    case AcceptListeners =>
      serverRef.!(Server.Accept(listenerNetworkRef))

    case ConnectStation(remoteHost) =>
      localManager = localManager.setPlaying(false)
      stationManager.currentOp match {
        case Some(station) =>
          val remoteAddress = 
            new InetSocketAddress(remoteHost, station.record.get("port").toInt)
          clientRef.!(Client.Connect(remoteAddress, stationMessengerRef))
        case None => Log.d("chakra", "Can't connect when station Op is NONE")
      }

    case SetStationTrack(track) =>
      Log.d("chakra", "SetStationTrack: " + track)

      fileOutputOp match {
        case None =>
        case Some(fileOutput) => fileOutput.close()
      }

      val name = "chakra" + Platform.currentTime 
      val file = java.io.File.createTempFile(name, null, cacheDir)
      val remoteTrack = track.copy(path = file.getAbsolutePath())
      val fileOutput = new BufferedOutputStream(new FileOutputStream(file))
      fileOutputOp = Some(fileOutput)
      stationManager = stationManager.setTrackOp(Some(remoteTrack))

    case AddStationAudioBuffer(audioBuffer) =>
      fileOutputOp match {
        case None =>
        case Some(fileOutput) => fileOutput.write(audioBuffer)
      }

    case EndStationAudioBuffer =>
      Log.d("chakra", "end station")
      fileOutputOp match {
        case None =>
        case Some(fileOutput) => 
          fileOutput.close()
          mainActorRef ! NotifyHandlers(OnStationAudioBufferDone(stationManager.trackOp))
      }
      fileOutputOp = None

    case SetStationPlayState(playState) =>
      Log.d("chakra", "set station playstate: " + playState)
      stationManager = stationManager.setPlayState(playState)


  }

  private def notifyHandlers(uis: Map[String, Handler], response: OnChange): Unit = {
    uis.foreach(pair => {
      val ui = pair._2
      ui.obtainMessage(0, response).sendToTarget()
    })
  }

}
