package com.logan.feelchakra

import android.util.Log

class StationManager(
  val map: Map[String, Station],
  val stagedMap: Map[String, Station],
  val currentOp: Option[Station],
  val discovering: Boolean,
  val advertising: Boolean,
  val remotePair: (Option[TrackAudio], Option[TrackAudio])
) { 

  def this() = this(
    HashMap[String, Station](),
    HashMap[String, Station](),
    None,
    false, 
    false,
    (None, None)
  )

  import MainActor._
  import UI._

  def stageStation(station: Station): StationManager = {
    val newStagedMap = stagedMap.+(station.device.deviceAddress -> station)
    new StationManager(map, newStagedMap, currentOp, discovering, advertising, remotePair)
  }

  def commitStation(device: WifiP2pDevice): StationManager = {

    if (stagedMap.isDefinedAt(device.deviceAddress)) {
      val station = stagedMap(device.deviceAddress)
      val newMap = map.+(device.deviceAddress -> station)
      mainActorRef ! NotifyHandlers(OnStationListChanged(newMap.values.toList))
      new StationManager(newMap, stagedMap, currentOp, discovering, advertising, remotePair)
    } else {
      this
    }

  }

  def setCurrentOp(stationOp: Option[Station]): StationManager = {
    mainActorRef ! NotifyHandlers(OnStationOptionChanged(stationOp))
    new StationManager(map, stagedMap, stationOp, discovering, advertising, remotePair)
  }

  def setDiscovering(discovering: Boolean): StationManager = {
    mainActorRef ! NotifyHandlers(OnDiscoveringChanged(discovering))
    new StationManager(map, stagedMap, currentOp, discovering, advertising, remotePair)
  }

  def setAdvertising(advertising: Boolean): StationManager = {
    mainActorRef ! NotifyHandlers(OnAdvertisingChanged(advertising))
    new StationManager(map, stagedMap, currentOp, discovering, advertising, remotePair)
  }

  def setCurrentRemoteTrack(track: Track): StationManager = {
    mainActorRef ! NotifyHandlers(OnRemoteTrackChanged(track))
    new StationManager(
      map, stagedMap, currentOp, discovering, advertising, 
      (Some(new TrackAudio(track)), None)
    )
  }

  def setCurrentRemoteAudio(audioBuffer: Array[Byte]): StationManager = {
    val trackAudioOp = remotePair._1 match {
      case Some(trackAudio) =>
        mainActorRef ! NotifyHandlers(OnRemoteAudioAdded(audioBuffer))
        Some(trackAudio.addAudio(audioBuffer))
        remotePair._1
      case None => 
        Log.d("chakra", "trying to write to a non-remote track")
        remotePair._1
    }

    new StationManager(
      map, stagedMap, currentOp, discovering, 
      advertising, (trackAudioOp, remotePair._2)
    )

  }

  def setNextRemoteTrack(track: Track): StationManager = {
    new StationManager(
      map, stagedMap, currentOp, discovering, advertising, 
      (remotePair._1, Some(new TrackAudio(track)))
    )

  }

  def setNextRemoteAudio(audioBuffer: Array[Byte]): StationManager = {
    val trackAudioOp = remotePair._2 match {
      case Some(trackAudio) =>
        Some(trackAudio.addAudio(audioBuffer))
        remotePair._2
      case None => 
        Log.d("chakra", "trying to write to a non-remote track")
        remotePair._2
    }

    new StationManager(
      map, stagedMap, currentOp, discovering, 
      advertising, (remotePair._1, trackAudioOp)
    )

  }

}
