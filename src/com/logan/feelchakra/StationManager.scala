package com.logan.feelchakra

import android.util.Log

case class StationManager(
  map: Map[String, Station],
  stagedMap: Map[String, Station],
  currentOp: Option[Station],
  discovering: Boolean,
  advertising: Boolean
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

  def setDiscovering(discovering: Boolean): StationManager = {
    mainActorRef ! NotifyHandlers(OnDiscoveringChanged(discovering))
    this.copy(discovering = discovering)
  }

  def setAdvertising(advertising: Boolean): StationManager = {
    mainActorRef ! NotifyHandlers(OnAdvertisingChanged(advertising))
    this.copy(advertising = advertising)
  }

}
