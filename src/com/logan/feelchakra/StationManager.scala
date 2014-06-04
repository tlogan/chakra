package com.logan.feelchakra

import android.util.Log

case class StationManager(
  map: Map[String, Station],
  stagedMap: Map[String, Station],
  currentOp: Option[Station],
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
    None,
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

  def setCurrentOp(stationOp: Option[Station]): StationManager = {
    mainActorRef ! NotifyHandlers(OnStationOptionChanged(stationOp))
    this.copy(currentOp = stationOp)
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
        Log.d("chakra", "commit track: has trackAudio: " + originPath)
        val track = trackAudio._1
        Log.d("chakra", "commit track: has track: " + track.path)
        trackOriginPathOp match {
          case Some(trackOriginPath) if trackOriginPath == originPath =>
            mainActorRef ! NotifyHandlers(OnStationTrackOpChanged(Some(track)))
            Log.d("chakra", "commit track: has current track: " + track.path)
          case _ => mainActorRef ! NotifyHandlers(OnStationTrackOpChanged(None))
            Log.d("chakra", "commit track: no current track: " + track.path)
        }
        this.copy(trackMap = trackMap.+(originPath -> track))
      case None => 
        Log.d("chakra", "commit track: not in audio map: " + originPath)
        this
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
