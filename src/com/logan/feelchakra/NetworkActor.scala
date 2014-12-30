package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object NetworkActor {

  def props(): Props = Props[NetworkActor]
  case class Subscribe(ui: Handler)
  case class SetLocalAddress(localAddress: InetSocketAddress)
  case class AddStation(station: Station, stationDeckRef: ActorRef)

}

class NetworkActor extends Actor {

  import NetworkActor._

  def update(
      serviceName: String,
      serviceType: String,
      localAddressOp: Option[InetSocketAddress]
  ) = {
    context.become(mkReceive(serviceName, serviceType, localAddressOp))
  }

  def mkReceive(
      serviceName: String,
      serviceType: String,
      localAddressOp: Option[InetSocketAddress]
  ): Receive = {

    case Subscribe(ui) =>
      List(
          UI.OnServiceNameChanged(serviceName),
          UI.OnServiceTypeChanged(serviceType),
          UI.OnLocalAddressOpChanged(localAddressOp)
      ).foreach(m => notifyHandler(ui, m))

    case SetLocalAddress(_localAddress) =>
      val _localAddressOp = Some(_localAddress)
      mainActorRef ! MainActor.NotifyHandlers(UI.OnLocalAddressOpChanged(_localAddressOp))
      update(serviceName, serviceType, _localAddressOp)

    case AddStation(station, stationDeckRef) =>
      val chakraDomain = List(serviceName, serviceType, "local").mkString(".") + "."
      if (station.domain == chakraDomain) {
        stationDeckRef ! StationDeck.StageStationDiscovery(station)
      }

  }

  val receive = mkReceive("_chakra", "_syncstream._tcp", None)

}
