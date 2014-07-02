package com.logan.feelchakra

import android.util.Log

case class StationManager(
  map: Map[String, Station],
  stagedMap: Map[String, Station],
  currentConnection: StationConnection,
  trackOriginPathOp: Option[String],
  trackMap: Map[String, Track],
  trackAudioMap: Map[String, (Track, OutputStream)],
  playState: PlayState,
  discovering: Boolean,
  advertising: Boolean
) { 

  def this() = this(
    HashMap[String, Station](),
    HashMap[String, Station](),
    StationDisconnected,
    None,
    HashMap[String, Track](),
    HashMap[String, (Track, OutputStream)](),
    NotPlaying,
    false, 
    false
  )

  import MainActor._
  import UI._

  def trackOp: Option[Track] = trackOriginPathOp match {
    case Some(path) => trackMap.get(path)
    case None => None
  }

  def stageStation(station: Station): StationManager = {
    val newStagedMap = stagedMap.+(station.device.deviceAddress -> station)
    this.copy(stagedMap = newStagedMap)
  }

  def commitStation(device: WifiP2pDevice): StationManager = {

    if (stagedMap.isDefinedAt(device.deviceAddress)) {
      val station = stagedMap(device.deviceAddress)
      val newMap = map.+(device.deviceAddress -> station)
      mainActorRef ! NotifyHandlers(OnStationListChanged(newMap.values.toList))
      this.copy(map = newMap)
    } else {
      this
    }

  }

  def setCurrentConnection(stationCon: StationConnection): StationManager = {
    mainActorRef ! NotifyHandlers(OnStationConnectionChanged(stationCon))
    this.copy(currentConnection = stationCon)
  }
  def commitConnection(): StationManager = {
    currentConnection match {
      case StationRequested(station) =>
        val stationCon = StationConnected(station)
        mainActorRef ! NotifyHandlers(OnStationConnectionChanged(stationCon))
        this.copy(currentConnection = stationCon)
      case _ => 
        assert(false)
        this
    }
    
  }

  def setTrackOriginPathOp(trackOriginPathOp: Option[String]): StationManager = {

    trackOriginPathOp match {
      case Some(trackOriginPath) =>
        trackMap.get(trackOriginPath) match {
          case Some(track) => mainActorRef ! NotifyHandlers(OnStationTrackOpChanged(Some(track)))
          case None => mainActorRef ! NotifyHandlers(OnStationTrackOpChanged(None))
        }
      case None => mainActorRef ! NotifyHandlers(OnStationTrackOpChanged(None))
    }

    this.copy(trackOriginPathOp = trackOriginPathOp)
  }

  def commitTrack(originPath: String): StationManager = {
    trackAudioMap.get(originPath) match {
      case Some(trackAudio) =>
        val track = trackAudio._1
        trackOriginPathOp match {
          case Some(trackOriginPath) if trackOriginPath == originPath =>
            mainActorRef ! NotifyHandlers(OnStationTrackOpChanged(Some(track)))
          case _ => 
        }
        this.copy(trackMap = trackMap.+(originPath -> track))
      case None => this
    }

  }

  def addTrackAudio(originPath: String, track: Track, fileOutput: OutputStream): StationManager = {
    this.copy(trackAudioMap = trackAudioMap.+(originPath -> (track, fileOutput)))
  }

  def setPlayState(playState: PlayState): StationManager = {
    mainActorRef ! NotifyHandlers(OnStationPlayStateChanged(playState))
    this.copy(playState = playState)
  }


  def setDiscovering(discovering: Boolean): StationManager = {
    mainActorRef ! NotifyHandlers(OnDiscoveringChanged(discovering))
    this.copy(discovering = discovering)
  }

  def setAdvertising(advertising: Boolean): StationManager = {
    mainActorRef ! NotifyHandlers(OnAdvertisingChanged(advertising))
    this.copy(advertising = advertising)
  }

}
