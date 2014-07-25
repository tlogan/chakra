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

  case class SetModHeight(height: Int)

  case class SetTrackList(trackList: List[Track]) 
  case class SetSelection(selection: Selection) 

  case class AppendFutureTrack(track: Track) 
  case class AppendOrRemoveFutureTrack(track: Track) 
  case class SetPresentTrack(track: Track) 
  case class SetPresentTrackToPrev
  case class SetPresentTrackToNext

  case class SetPresentTrackFromPastIndex(index: Int) 
  case class SetPresentTrackFromFutureIndex(index: Int) 

  case class SetStartPos(startPos: Int)
  case object FlipPlaying
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

  private var database: Database = null
  private var cacheDir: File = null
  private var uis: Map[String, Handler] = new HashMap[String, Handler]()

  private var _modHeight: Int = 0
  private var selectionManager: SelectionManager = new SelectionManager
  private var playerPartManager: PlayerPartManager = PlayerPartManager.create()
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
      notifyHandlers(onChange)

    case Subscribe(key, ui) =>
      networkProfile.localAddressOp match {
        case Some(localAddress) => 
          val response = OnProfileChanged(networkProfile)
          ui.obtainMessage(0, response).sendToTarget()
        case None => {}
      }

      List(

        OnModHeightChanged(_modHeight),

        OnPlayerPartListChanged(playerPartManager.list),
        OnPlayerPartChanged(playerPartManager.current),

        OnSelectionListChanged(selectionManager.list),
        OnSelectionChanged(selectionManager.current),
        OnPlayerOpenChanged(localManager.playerOpen),

        OnStationConnectionChanged(stationManager.currentConnection),
        OnDiscoveringChanged(stationManager.discovering),
        OnAdvertisingChanged(stationManager.advertising),
        OnStationListChanged(stationManager.fullyDiscoveredStationMap.values.toList),

        OnArtistMapChanged(localManager.artistMap),
        OnAlbumMapChanged(localManager.albumMap),
        OnTrackListChanged(localManager.trackList),

        OnPastTrackListChanged(localManager.pastTrackList),
        OnPresentTrackOptionChanged(localManager.presentTrackOp),
        OnFutureTrackListChanged(localManager.futureTrackList),

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

    case SetModHeight(height) =>
      _modHeight = height
      notifyHandlers(UI.OnModHeightChanged(_modHeight))

    case SetTrackList(trackList) =>
      localManager = localManager.setTrackList(trackList)

    case SetSelection(selection) => 
      selectionManager = selectionManager.setCurrent(selection)

    case Discover => 
      stationManager = stationManager.setDiscovering(true)

    case SetStartPos(startPos) =>
      localManager = localManager.setStartPos(startPos)

    case FlipPlaying => 
      val playing = !localManager.playing
      localManager = localManager.setPlaying(playing)

    case FlipPlayer =>
      localManager = localManager.flipPlayer()

    case SetPlayerOpen(playerOpen) =>
      localManager = localManager.setPlayerOpen(playerOpen)

    case WriteListenerPlayState(playState) => 
      notifyWriters(ListenerWriter.WritePlayState(playState))

    case AppendFutureTrack(track) =>
      appendFutureTrack(track)

    case AppendOrRemoveFutureTrack(track) =>
      if (localManager.futureTrackList.contains(track)) {
        localManager = localManager.removeFutureTrack(track)
      } else {
        appendFutureTrack(track)
      }

    case SetPresentTrack(track) =>
      setPresentTrack(track)

    case SetPresentTrackToPrev =>
      localManager.pastTrackList.lastOption match {
        case Some(track) =>
          localManager = localManager
            .setPresentTrackFromPastIndex(localManager.pastTrackList.size - 1)
          if (stationManager.currentConnection == StationDisconnected) {
            playTrack(track)
          } 
        case None =>
      }

    case SetPresentTrackToNext =>
      localManager.futureTrackList.headOption match {
        case Some(track) => 
          localManager = localManager.setPresentTrackFromFutureIndex(0)
          if (stationManager.currentConnection == StationDisconnected) {
            playTrack(track)
          } 
        case None =>
      }

    case SetPresentTrackFromPastIndex(index) =>
      localManager = localManager.setPresentTrackFromPastIndex(index)
      localManager.presentTrackOp match {
        case Some(track) => 
          if (stationManager.currentConnection == StationDisconnected) {
            playTrack(track)
          } 
        case None =>
      }
        
    case SetPresentTrackFromFutureIndex(index) => 
      localManager = localManager.setPresentTrackFromFutureIndex(index)
      localManager.presentTrackOp match {
        case Some(track) => 
          if (stationManager.currentConnection == StationDisconnected) {
            playTrack(track)
          } 
        case None =>
      }

    case AddStation(station) =>
      stationManager = stationManager.stageStationDiscovery(station)

    case CommitStation(device) =>
      stationManager = stationManager.commitStationDiscovery(device)
     
    case RequestStation(station) =>
      stationManager = {
        stationManager
          .setDiscovering(false)
          .setCurrentConnection(StationRequested(station))
      }

    case BecomeTheStation =>
      stationManager = {
        stationManager
          .setCurrentConnection(StationDisconnected)
          .setDiscovering(selectionManager.current == StationSelection)
      }

    case SetLocalAddress(localAddress) =>
      networkProfile = networkProfile.setLocalAddress(localAddress)

    case AddListenerWriter(remote, socket) =>

      val writerRef = context.actorOf(ListenerWriter.props())
      writerRef ! ListenerWriter.SetSocket(socket)
      localManager.presentTrackOp match {
        case Some(track) =>
          AudioReader(track.path).subscribe(
            audioBuffer => writerRef ! ListenerWriter.WriteAudioBuffer(track.path, audioBuffer),
            t => {},
            () => writerRef ! ListenerWriter.WriteAudioDone(track.path)
          )
        case None =>
      }

      val reader = Runnable.createListenerReader(socket, writerRef)
      reader.run()

      val messenger = Messenger(writerRef, reader)
      messengers = messengers.+(remote -> messenger)

    case AcceptListeners =>
      serverRef.!(Server.Accept)

    case ConnectStation(remoteHost) =>
      localManager = localManager.setPlaying(false)
      stationManager.currentConnection match {
        case StationRequested(station) =>
          val remoteAddress = 
            new InetSocketAddress(remoteHost, station.record.get("port").toInt)
          clientRef.!(Client.Connect(remoteAddress))
          Log.d("chakra", "staiton requested: " + remoteAddress)
        case StationConnected(station) =>
          Log.d("chakra", "Can't connect when station is already connected")
        case StationDisconnected => 
          Log.d("chakra", "Can't connect when station is disconnected")
      }

    case ChangeStationMessenger(socket) =>
      val writerRef = context.actorOf(StationWriter.props(), "StationWriter")
      writerRef ! StationWriter.SetSocket(socket)
      val reader = Runnable.createStationReader(socket, writerRef)
      reader.run()
      stationManager = stationManager.commitStationConnection()
      stationMessengerOp = Some(Messenger(writerRef, reader))

    case ChangeStationTrackByOriginPath(originPath) =>
      stationManager = stationManager.setTrackOriginPathOp(Some(originPath))

    case AddStationAudioBuffer(stationPath, audioBuffer) =>
      stationManager.transferringAudioMap.get(stationPath) match {
        case None =>
          val name = "chakra" + Platform.currentTime 
          val file = java.io.File.createTempFile(name, null, cacheDir)
          val fileOutput = new BufferedOutputStream(new FileOutputStream(file))
          stationManager = stationManager.addTrackAudio(stationPath, file.getAbsolutePath(), fileOutput)
          fileOutput.write(audioBuffer)
        case Some(transferringAudio) =>
          transferringAudio._2.write(audioBuffer)
      }



    case EndStationAudioBuffer(stationPath) =>
      Log.d("chakra", "end station: " + stationPath)
      val transferringAudio = stationManager.transferringAudioMap(stationPath)
      transferringAudio._2.close()
      val path = transferringAudio._1

      TrackFuture(path) onComplete {
        case Success(track) => 
          Log.d("chakra", "end station audio buffer track: " + track)
          stationManager = stationManager.commitTrackTransfer(stationPath, track)
        case Failure(t) => 
          assert(false) 
      }

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

  private def setPresentTrack(track: Track): Unit = {
    localManager = localManager
      .setPresentTrack(track)
      .removeFutureTrack(track)
    if (stationManager.currentConnection == StationDisconnected) {
      writeTrackToListeners(track)
      playTrack(track)
    } 
  }

  private def playTrack(track: Track): Unit = {
    notifyWriters(ListenerWriter.WriteCurrentTrackPath(track.path))
    localManager = localManager.setStartPos(0)
    localManager = localManager.setPlaying(true)
  }

  private def appendFutureTrack(track: Track): Unit = {
    localManager = localManager.appendFutureTrack(track)
    if (stationManager.currentConnection == StationDisconnected) {
      writeTrackToListeners(track)
    } 
  }
  private def writeTrackToListeners(track: Track): Unit = {
    AudioReader(track.path).subscribe(
      audioBuffer => notifyWriters(ListenerWriter.WriteAudioBuffer(track.path, audioBuffer)),
      t => {},
      () => notifyWriters(ListenerWriter.WriteAudioDone(track.path))
    )
  }


  private def notifyHandlers(response: OnChange): Unit = {
    uis.foreach(pair => {
      val ui = pair._2
      ui.obtainMessage(0, response).sendToTarget()
    })
  }

  def setCurrentPlayerPart(current: PlayerPart): PlayerPartManager = {
    notifyHandlers(UI.OnPlayerPartChanged(current))
    playerPartManager.copy(current = current)
  }

}
