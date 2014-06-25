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

  case object ChangeToPrevTrack
  case object ChangeToNextTrack
  case class ChangeTrackByIndex(trackIndex: Int)
  case class SetTrackDuration(trackDuration: Int)

  case class AddPlaylistTrack(track: Track) 
  case class AddAndPlayTrack(track: Track) 
  case object FlipPlayer
  case class SetPlayerOpen(playerOpen: Boolean) 
  case class AddStation(station: Station)
  case class CommitStation(device: WifiP2pDevice)
  case class RequestStation(station: Station)
  case object BecomeTheStation
  case object AcceptListeners 
  case class ConnectStation(remoteHost: String)
  case class ChangeStationMessenger(socket: Socket)

  case class WriteListenerPlayState(playState: PlayState) 

  case class SetLocalAddress(localAddress: InetSocketAddress)
  case class AddListenerWriter(remote: InetSocketAddress, socket: Socket)

  case object Advertise
  case object Discover 

  case class ChangeStationTrackByOriginPath(originPath: String)
  case class AddStationTrack(track: Track)

  case class AddStationAudioBuffer(path: String, audioBuffer: Array[Byte])
  case class EndStationAudioBuffer(path: String)
  case class SetStationPlayState(playState: PlayState)

  case class SelectArtistTuple(artistTuple: (String, AlbumMap)) 
  case class SelectAlbumTuple(albumTuple: (Album, List[Track])) 

}

class MainActor extends Actor {

  import MainActor._
  import UI._
  import scala.concurrent.ExecutionContext.Implicits.global

  private val serverRef: ActorRef = context.actorOf(Server.props(), "Server")
  private val clientRef: ActorRef = context.actorOf(Client.props(), "Client")

  //private val listenerNetworkRef: ActorRef = context.actorOf(ListenerNetwork.props(), "ListenerNetwork")

  private var database: Database = null
  private var cacheDir: File = null
  private var uis: Map[String, Handler] = new HashMap[String, Handler]()
  private var selectionManager: SelectionManager = new SelectionManager
  private var localManager: LocalManager = new LocalManager 
  private var stationManager: StationManager = new StationManager
  private var networkProfile: NetworkProfile = new NetworkProfile

  private var stationMessengerOp: Option[Messenger] = None
  private var messengers = HashMap[InetSocketAddress, Messenger]()
  private def notifyWriters(message: Object): Unit = {
    messengers.foreach(pair => { 
      val mes = pair._2
      mes.writerRef.!(message)
    })
  }

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
        OnSelectionChanged(selectionManager.current),
        OnPlayerOpenChanged(localManager.playerOpen),

        OnStationOptionChanged(stationManager.currentOp),
        OnDiscoveringChanged(stationManager.discovering),
        OnAdvertisingChanged(stationManager.advertising),
        OnStationListChanged(stationManager.map.values.toList),

        OnArtistMapChanged(localManager.artistMap),
        OnAlbumMapChanged(localManager.albumMap),
        OnTrackListChanged(localManager.trackList),

        OnTrackIndexChanged(localManager.currentIndex),
        OnPlaylistChanged(localManager.playlist),

        OnLocalTrackOptionChanged(localManager.currentOp),
        OnPrevTrackOptionChanged(localManager.prevOp),
        OnNextTrackOptionChanged(localManager.nextOp),

        OnLocalStartPosChanged(localManager.startPos),
        OnLocalPlayingChanged(localManager.playing)

      ).foreach(response => {
        ui.obtainMessage(0, response).sendToTarget()
      })

      uis = uis.+((key, ui))

    case Unsubscribe(key) =>
      uis = uis.-(key)

    case SetDatabase(database) =>
      this.database = database
      TrackListFuture(database) onComplete { 
        case Success(trackList) => 
          self ! SetTrackList(trackList)

        case Failure(t) => Log.d("chakra", "trakListFuture failed: " + t.getMessage)
      }

    case SetCacheDir(cacheDir) => 
      this.cacheDir = cacheDir

    case SetTrackList(trackList) =>
      localManager = localManager.setTrackList(trackList)

    case SetSelection(selection) => 
      selectionManager = selectionManager.setCurrent(selection)
      localManager = localManager.setPlayerOpen(false)

    case Discover => 
      stationManager = stationManager.setDiscovering(true)

    case FlipPlayer =>
      localManager = localManager.flipPlayer()

    case SetPlayerOpen(playerOpen) =>
      localManager = localManager.setPlayerOpen(playerOpen)

    case ChangeToPrevTrack =>
      changeTrackByIndex(localManager.currentIndex - 1)

    case ChangeToNextTrack =>
      changeTrackByIndex(localManager.currentIndex + 1)

    case ChangeTrackByIndex(trackIndex) => 
      changeTrackByIndex(trackIndex)

    case SetTrackDuration(trackDuration) =>
      localManager = localManager.setCurrentDuration(trackDuration)

    case WriteListenerPlayState(playState) => 
      notifyWriters(ListenerWriter.WritePlayState(playState))

    case AddPlaylistTrack(track) =>
      if (localManager.playlist.size == 0) {
        addPlaylistTrack(track)
        localManager = localManager.setCurrentIndex(0)
        playTrack(track)
      } else {
        addPlaylistTrack(track)
      }

    case AddAndPlayTrack(track) =>
      val newIndex = localManager.playlist.size
      addPlaylistTrack(track)
      localManager = localManager.setCurrentIndex(newIndex)
      playTrack(track)
 

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

    case AddListenerWriter(remote, socket) =>

      val writerRef = context.actorOf(ListenerWriter.props())
      writerRef ! ListenerWriter.SetSocket(socket)
      writerRef ! ListenerWriter.WriteTrackOp(localManager.currentOp)

      val reader = Runnable.createListenerReader(socket, writerRef)
      reader.run()

      val messenger = Messenger(writerRef, reader)
      messengers = messengers.+(remote -> messenger)

    case AcceptListeners =>
      serverRef.!(Server.Accept)

    case ConnectStation(remoteHost) =>
      localManager = localManager.setPlaying(false)
      stationManager.currentOp match {
        case Some(station) =>
          val remoteAddress = 
            new InetSocketAddress(remoteHost, station.record.get("port").toInt)
          clientRef.!(Client.Connect(remoteAddress))
        case None => Log.d("chakra", "Can't connect when station Op is NONE")
      }

    case ChangeStationMessenger(socket) =>
      val writerRef = context.actorOf(StationWriter.props(), "StationWriter")
      writerRef ! StationWriter.SetSocket(socket)
      val reader = Runnable.createStationReader(socket, writerRef)
      reader.run()
      stationMessengerOp = Some(Messenger(writerRef, reader))

    case ChangeStationTrackByOriginPath(originPath) =>
      stationManager = stationManager.setTrackOriginPathOp(Some(originPath))

    case AddStationTrack(track) =>
      Log.d("chakra", "AddStationTrack: " + track)
      val name = "chakra" + Platform.currentTime 
      val file = java.io.File.createTempFile(name, null, cacheDir)
      val remoteTrack = track.copy(path = file.getAbsolutePath())
      val fileOutput = new BufferedOutputStream(new FileOutputStream(file))
      stationManager = stationManager.addTrackAudio(track.path, remoteTrack, fileOutput)

    case AddStationAudioBuffer(path, audioBuffer) =>
      val trackAudio = stationManager.trackAudioMap(path)
      trackAudio._2.write(audioBuffer)

    case EndStationAudioBuffer(path) =>
      Log.d("chakra", "end station: " + path)
      val trackAudio = stationManager.trackAudioMap(path)
      trackAudio._2.close()
      stationManager = stationManager.commitTrack(path)

    case SetStationPlayState(playState) =>
      Log.d("chakra", "set station playstate: " + playState)
      stationManager = stationManager.setPlayState(playState)

    case SelectArtistTuple(artistTuple) =>
      localManager = localManager.artistTupleOp match {
        case Some(currentArtistTuple) if currentArtistTuple == artistTuple =>
          localManager.setArtistTupleOp(None)
        case _ =>
          localManager.setArtistTupleOp(Some(artistTuple))
      }

    case SelectAlbumTuple(albumTuple) =>
      localManager = localManager.albumTupleOp match {
        case Some(currentAlbumTuple) if currentAlbumTuple == albumTuple =>
          localManager.setAlbumTupleOp(None)
        case _ =>
          localManager.setAlbumTupleOp(Some(albumTuple))
      }

  }

  private def changeTrackByIndex(trackIndex: Int): Unit = {
    val current = localManager.optionByIndex(trackIndex)
    localManager = localManager.setCurrentIndex(trackIndex)
    current match {
      case Some(track) =>
        playTrack(track)
      case None =>
    }
  } 

  private def addPlaylistTrack(track: Track): Unit = {
    localManager = localManager.addPlaylistTrack(track)
    notifyWriters(ListenerWriter.WriteTrackOp(Some(track)))
    AudioReader(track.path).subscribe(
      audioBuffer => notifyWriters(ListenerWriter.WriteAudioBuffer(track.path, audioBuffer)),
      t => {},
      () => notifyWriters(ListenerWriter.WriteAudioDone(track.path))
    )
  }

  private def playTrack(track: Track): Unit = {
    localManager = localManager.setStartPos(0)
    if (stationManager.currentOp == None) {
      localManager = localManager.setPlaying(true)
      notifyWriters(ListenerWriter.WriteCurrentTrackPath(track.path))
    }
  }

  private def notifyHandlers(uis: Map[String, Handler], response: OnChange): Unit = {
    uis.foreach(pair => {
      val ui = pair._2
      ui.obtainMessage(0, response).sendToTarget()
    })
  }

}
