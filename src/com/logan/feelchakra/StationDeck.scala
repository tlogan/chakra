package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object StationDeck {

  def props(): Props = Props[StationDeck]

  case class Subscribe(ui: Handler)
  case class StageStationDiscovery(station: Station)
  case class CommitStationDiscovery(device: WifiP2pDevice)

}

class StationDeck extends Actor {

  import StationDeck._
  import UI._

  private def update(
    fullyDiscoveredStationMap: Map[String, Station],
    partlyDiscoveredStationMap: Map[String, Station]
  ) = {
    context.become(receiveStations(
      fullyDiscoveredStationMap,
      partlyDiscoveredStationMap
    ))
  }


  def receiveStations(
    fullyDiscoveredStationMap: Map[String, Station],
    partlyDiscoveredStationMap: Map[String, Station]
  ): Receive = {

    case Subscribe(ui: Handler) =>
      List(
        OnStationListChanged(fullyDiscoveredStationMap.values.toList)
      ).foreach(m => notifyHandler(ui, m))

    case StageStationDiscovery(station: Station) =>
      val newStagedMap = partlyDiscoveredStationMap.+(station.device.deviceAddress -> station)
      update(fullyDiscoveredStationMap, newStagedMap)

    case CommitStationDiscovery(device: WifiP2pDevice) =>
      if (partlyDiscoveredStationMap.isDefinedAt(device.deviceAddress)) {
        val station = partlyDiscoveredStationMap(device.deviceAddress)
        val newMap = fullyDiscoveredStationMap.+(device.deviceAddress -> station)
        mainActorRef ! MainActor.NotifyHandlers(OnStationListChanged(newMap.values.toList))
        update(newMap, partlyDiscoveredStationMap)
      } 
  }

  val receive = receiveStations(HashMap[String, Station](), HashMap[String, Station]())


}
