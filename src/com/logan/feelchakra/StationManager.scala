package com.logan.feelchakra

import android.util.Log

import scala.concurrent.ExecutionContext.Implicits.global

case class StationManager(
  fullyDiscoveredStationMap: Map[String, Station],
  partlyDiscoveredStationMap: Map[String, Station],
  currentConnection: StationConnection,
  trackOriginPathOp: Option[String],
  transferredTrackMap: Map[String, Track],
  transferringAudioMap: Map[String, (String, OutputStream)],
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
    HashMap[String, (String, OutputStream)](),
    NotPlaying,
    false, 
    false
  )

  import MainActor._
  import UI._

  def trackOp: Option[Track] = trackOriginPathOp match {
    case Some(path) => transferredTrackMap.get(path)
    case None => None
  }

  def stageStationDiscovery(station: Station): StationManager = {
    val newStagedMap = partlyDiscoveredStationMap.+(station.device.deviceAddress -> station)
    this.copy(partlyDiscoveredStationMap = newStagedMap)
  }

  def commitStationDiscovery(device: WifiP2pDevice): StationManager = {

    if (partlyDiscoveredStationMap.isDefinedAt(device.deviceAddress)) {
      val station = partlyDiscoveredStationMap(device.deviceAddress)
      val newMap = fullyDiscoveredStationMap.+(device.deviceAddress -> station)
      mainActorRef ! NotifyHandlers(OnStationListChanged(newMap.values.toList))
      this.copy(fullyDiscoveredStationMap = newMap)
    } else {
      this
    }

  }

  def setCurrentConnection(stationCon: StationConnection): StationManager = {
    mainActorRef ! NotifyHandlers(OnStationConnectionChanged(stationCon))
    this.copy(currentConnection = stationCon)
  }

  def commitStationConnection(): StationManager = {
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
        transferredTrackMap.get(trackOriginPath) match {
          case Some(track) => mainActorRef ! NotifyHandlers(OnStationTrackOpChanged(Some(track)))
          case None => mainActorRef ! NotifyHandlers(OnStationTrackOpChanged(None))
        }
      case None => mainActorRef ! NotifyHandlers(OnStationTrackOpChanged(None))
    }

    this.copy(trackOriginPathOp = trackOriginPathOp)
  }

  def commitTrackTransfer(originPath: String, track: Track): StationManager = {

    trackOriginPathOp match {
      case Some(trackOriginPath) if trackOriginPath == originPath =>
        mainActorRef ! NotifyHandlers(OnStationTrackOpChanged(Some(track)))
      case _ => 
    }
    this.copy(transferredTrackMap = transferredTrackMap.+(originPath -> track), transferringAudioMap = transferringAudioMap.-(originPath))

  }

  def addTrackAudio(originPath: String, path: String, fileOutput: OutputStream): StationManager = {
    this.copy(transferringAudioMap = transferringAudioMap.+(originPath -> (path -> fileOutput)))
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
