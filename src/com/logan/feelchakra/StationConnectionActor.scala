package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object StationConnectionActor {

  def props(): Props = Props[StationConnectionActor]

  case class Subscribe(ui: Handler)
  case class WriteTrackToListenersIfStationDisconnected(track: Track, isCurrent: Boolean)
  case class PlayIfDisconnected(track: Track)
  case class Connect(remoteHost: String)
  case object CommitStationConnection
  case class CancelOrRequest(station: Station)
  case object Disconnect

}


class StationConnectionActor extends Actor {

  import StationConnectionActor._
  import UI._


  def update(stationConnection: StationConnection) = {
    mainActorRef ! MainActor.NotifyHandlers(OnStationConnectionChanged(stationConnection))
    context.become(receiveConnection(stationConnection))
  }

  def receiveConnection(stationConnection: StationConnection): Receive = {

    case Subscribe(ui) =>
       notifyHandler(ui, OnStationConnectionChanged(stationConnection))

    case WriteTrackToListenersIfStationDisconnected(track, isCurrent) =>
      if (stationConnection == StationDisconnected) {
        mainActorRef ! MainActor.WriteTrackToListeners(track)
        if (isCurrent) {
          mainActorRef ! MainActor.PlayTrack(track)
        }
      }

    case PlayIfDisconnected(track) =>
      if (stationConnection == StationDisconnected) {
        mainActorRef ! MainActor.PlayTrack(track)
      }

    case Disconnect =>
      stationConnection match {
        case StationDisconnected => 
        case _ => update(StationDisconnected)
      }

    case CancelOrRequest(_station) =>
      stationConnection match {
        case StationDisconnected =>
          update(StationRequested(_station))
        case StationRequested(station) if _station == station =>
          self ! Disconnect
        case StationConnected(station) if _station == station =>
          self ! Disconnect
      }

    case Connect(remoteHost) =>
      stationConnection match {
        case StationRequested(station) =>
          val remoteAddress = new InetSocketAddress(remoteHost, station.record.get("port").toInt)
          mainActorRef ! MainActor.ConnectAsClient(remoteAddress)
        case _ =>
      }

    case CommitStationConnection =>
      stationConnection match {
        case StationRequested(station) => 
          update(StationConnected(station))
        case _ =>
      }

  }

  val receive = receiveConnection(StationDisconnected)

}
