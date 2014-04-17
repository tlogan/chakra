package com.logan.feelchakra

import android.util.Log

class StationManager(
  val map: Map[String, Station],
  val stagedMap: Map[String, Station],
  val currentOp: Option[Station],
  val discovering: Boolean,
  val advertising: Boolean
) { 

  def this() = this(
    HashMap[String, Station](),
    HashMap[String, Station](),
    None,
    false, 
    false
  )

  import MainActor._
  import UI._

  def stageStation(station: Station): StationManager = {
    val newStagedMap = stagedMap.+(station.device.deviceAddress -> station)
    new StationManager(map, newStagedMap, currentOp, discovering, advertising)
  }

  def commitStation(device: WifiP2pDevice): StationManager = {

    if (stagedMap.isDefinedAt(device.deviceAddress)) {
      val station = stagedMap(device.deviceAddress)
      val newMap = map.+(device.deviceAddress -> station)
      mainActorRef ! NotifyHandlers(OnStationListChanged(newMap.values.toList))
      new StationManager(newMap, stagedMap, currentOp, discovering, advertising)
    } else {
      this
    }

  }

  def setCurrentOp(stationOp: Option[Station]): StationManager = {
    mainActorRef ! NotifyHandlers(OnStationOptionChanged(stationOp))
    new StationManager(map, stagedMap, stationOp, discovering, advertising)
  }

  def setDiscovering(discovering: Boolean): StationManager = {
    mainActorRef ! NotifyHandlers(OnDiscoveringChanged(discovering))
    new StationManager(map, stagedMap, currentOp, discovering, advertising)
  }

  def setAdvertising(advertising: Boolean): StationManager = {
    mainActorRef ! NotifyHandlers(OnAdvertisingChanged(advertising))
    new StationManager(map, stagedMap, currentOp, discovering, advertising)
  }

}
