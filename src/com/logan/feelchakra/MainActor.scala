package com.logan.feelchakra

import android.util.Log

object MainActor {

  def props(): Props = {
    Props[MainActor]
  }

  //Requests
  case class NotifyHandler(ui: Handler, onChange: UI.OnChange)
  case class NotifyHandlers(onChange: UI.OnChange)
  case class Subscribe(key: String, ui: Handler) 
  case class Unsubscribe(key: String) 
  case class SetDatabase(database: Database) 
  case class SetCacheDir(cacheDir: File) 

  case class SetModHeight(height: Int)

  case class SetTrackList(trackList: List[Track]) 
  case class SetSelection(selection: Selection) 

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
  case class CancelOrRequestStation(station: Station)
  case object BecomeTheStation
  case object AcceptListeners 
  case class ConnectStation(remoteHost: String)
  case class ChangeStationMessenger(socket: Socket)

  case class ConnectAsClient(remoteAddress: InetSocketAddress)

  case class WriteListenerPlayState(playState: PlayState) 

  case class SetLocalAddress(localAddress: InetSocketAddress)
  case class AddListenerWriter(remote: InetSocketAddress, socket: Socket)

  case class ChangeStationTrackByOriginPath(originPath: String)

  case class AddStationAudioBuffer(path: String, audioBuffer: Array[Byte])
  case class EndStationAudioBuffer(path: String)
  case class SetStationPlayState(playState: PlayState)

  case class SelectArtistTuple(artistTuple: (String, AlbumMap)) 
  case class SelectAlbumTuple(albumTuple: (Album, List[Track])) 

  case class WriteTrackToListeners(track: Track)
  case class WriteTrackToListenersIfStationDisconnected(track: Track)

  case class PlayTrack(track: Track)
  case class PlayTrackIfLocal(track: Track)


}

class MainActor extends Actor {

  import MainActor._
  import UI._
  import scala.concurrent.ExecutionContext.Implicits.global

  private val serverRef: ActorRef = context.actorOf(Server.props(), "Server")
  private val clientRef: ActorRef = context.actorOf(Client.props(), "Client")
  private val trackDeckRef: ActorRef = context.actorOf(TrackDeck.props(), "TrackDeck")
  private val trackLibraryRef: ActorRef = context.actorOf(TrackLibrary.props(), "TrackLibrary")
  private val stationTrackDeckRef: ActorRef = context.actorOf(StationTrackDeck.props(), "StationTrackDeck")
  private val stationDeckRef: ActorRef = context.actorOf(StationDeck.props(), "StationDeck")
  private val stationConnectionActorRef: ActorRef = context.actorOf(StationConnectionActor.props(), "StationConnectionActor")

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

        OnLocalStartPosChanged(localManager.startPos),
        OnLocalPlayingChanged(localManager.playing)

      ).foreach(response => {
        ui.obtainMessage(0, response).sendToTarget()
      })

      trackDeckRef ! TrackDeck.Subscribe(ui)
      trackLibraryRef ! TrackLibrary.Subscribe(ui)
      stationTrackDeckRef ! StationTrackDeck.Subscribe(ui)
      stationDeckRef ! StationDeck.Subscribe(ui)
      stationConnectionActorRef ! StationConnectionActor.Subscribe(ui)

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
      trackLibraryRef ! TrackLibrary.SetTrackList(trackList)

    case SetSelection(selection) => 
      selectionManager = selectionManager.setCurrent(selection)

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

    case WriteTrackToListenersIfStationDisconnected(track) =>
      stationConnectionActorRef ! StationConnectionActor.WriteTrackToListenersIfStationDisconnected(track, false)

    case AppendOrRemoveFutureTrack(track) =>
      trackDeckRef ! TrackDeck.AppendOrRemoveFutureTrack(track)

    case SetPresentTrack(track) =>
      trackDeckRef ! TrackDeck.SetPresentTrack(track)
      stationConnectionActorRef ! StationConnectionActor.WriteTrackToListenersIfStationDisconnected(track, true)

    case SetPresentTrackToPrev =>
      trackDeckRef ! TrackDeck.SetPresentTrackToPrev

    case SetPresentTrackToNext =>
      trackDeckRef ! TrackDeck.SetPresentTrackToNext

    case SetPresentTrackFromPastIndex(index) =>
      trackDeckRef ! TrackDeck.SetPresentTrackToPastIndex(index)
        
    case SetPresentTrackFromFutureIndex(index) => 
      trackDeckRef ! TrackDeck.SetPresentTrackToFutureIndex(index)

    case AddStation(station) =>
      val chakraDomain = List(networkProfile.serviceName, networkProfile.serviceType, "local").mkString(".") + "."
      Log.d("chakra", "chakraDomain: " + chakraDomain)
      if (station.domain == chakraDomain) {
        stationDeckRef ! StationDeck.StageStationDiscovery(station)
      }

    case CommitStation(device) =>
      stationDeckRef ! StationDeck.CommitStationDiscovery(device)
     
    case CancelOrRequestStation(station) =>
      stationConnectionActorRef ! StationConnectionActor.CancelOrRequest(station)

    case BecomeTheStation =>
      stationConnectionActorRef ! StationConnectionActor.Disconnect

    case SetLocalAddress(localAddress) =>
      networkProfile = networkProfile.setLocalAddress(localAddress)

    case AddListenerWriter(remote, socket) =>

      val writerRef = context.actorOf(ListenerWriter.props())
      writerRef ! ListenerWriter.SetSocket(socket)

      trackDeckRef ! TrackDeck.WritePresentTrackToListeners


      val reader = ListenerReader.create(socket, writerRef)
      reader.run()

      val messenger = Messenger(writerRef, reader)
      messengers = messengers.+(remote -> messenger)

    case AcceptListeners =>
      serverRef.!(Server.Accept)

    case ConnectStation(remoteHost) =>
      localManager = localManager.setPlaying(false)
      stationConnectionActorRef ! StationConnectionActor.Connect(remoteHost)

    case ConnectAsClient(remoteAddress) =>
      clientRef ! Client.Connect(remoteAddress)

    case ChangeStationMessenger(socket) =>
      val writerRef = context.actorOf(StationWriter.props(), "StationWriter")
      writerRef ! StationWriter.SetSocket(socket)
      val reader = StationReader.create(socket, writerRef)
      reader.run()
      stationConnectionActorRef ! StationConnectionActor.CommitStationConnection
      stationMessengerOp = Some(Messenger(writerRef, reader))

    case ChangeStationTrackByOriginPath(originPath) =>
      Log.d("chakra", "ChangeStationTrackByOriginPath: " + originPath)
      stationTrackDeckRef ! StationTrackDeck.SetTrackOriginPathOp(Some(originPath))

    case AddStationAudioBuffer(stationPath, audioBuffer) =>
      stationTrackDeckRef ! StationTrackDeck.AddAudioBuffer(stationPath, audioBuffer, cacheDir)



    case EndStationAudioBuffer(stationPath) =>
      Log.d("chakra", "end station: " + stationPath)
      stationTrackDeckRef ! StationTrackDeck.CommitTrackTransfer(stationPath)

    case SetStationPlayState(playState) =>
      Log.d("chakra", "set station playstate: " + playState)
      stationManager = stationManager.setPlayState(playState)

    case SelectArtistTuple(artistTuple) =>
      trackLibraryRef ! TrackLibrary.SelectArtistTuple(artistTuple)

    case SelectAlbumTuple(albumTuple) =>
      trackLibraryRef ! TrackLibrary.SelectAlbumTuple(albumTuple)

    case PlayTrack(track) =>
      notifyWriters(ListenerWriter.WriteCurrentTrackPath(track.path))
      localManager = localManager.setStartPos(0)
      localManager = localManager.setPlaying(true)

    case PlayTrackIfLocal(track) =>
      stationConnectionActorRef ! StationConnectionActor.PlayIfDisconnected

    case WriteTrackToListeners(track) =>
      AudioReader(track.path).subscribe(
        audioBuffer => notifyWriters(ListenerWriter.WriteAudioBuffer(track.path, audioBuffer)),
        t => {},
        () => notifyWriters(ListenerWriter.WriteAudioDone(track.path))
      )


  }

  private def notifyHandlers(response: OnChange): Unit = {
    uis.foreach(pair => {
      val ui = pair._2
      notifyHandler(ui, response)
    })
  }

  private def setCurrentPlayerPart(current: PlayerPart): PlayerPartManager = {
    notifyHandlers(UI.OnPlayerPartChanged(current))
    playerPartManager.copy(current = current)
  }


}
