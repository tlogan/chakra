package com.logan.feelchakra

import android.util.Log

object ClientMessenger {

  def props(): Props = {
    Props[ClientMessenger]
  }

  case class SetConnectionRef(connectionRef: ActorRef)

  sealed trait WaitForData
  case object WaitForTrack extends WaitForData
  case object WaitForTrackFile extends WaitForData

}

class ClientMessenger extends Actor {

  import Tcp._
  import ClientMessenger._

  var _connectionRef: ActorRef = _

  val mainActorRef = MainActor.mainActorRef

  def receive = receiveConnectionRef

  def receiveConnectionRef: Receive = {
    case SetConnectionRef(connectionRef) =>
      Log.d("chakra", "setting connnectionRef " + connectionRef)
      _connectionRef = connectionRef
      context.become(receiveTrack(WaitForTrack))
  }

  def receiveTrack(waitForData: WaitForData): Receive = {


    case Received(data) => 
      waitForData match {
        case WaitForTrack =>
          mainActorRef ! MainActor.SetRemoteTrack(Track(data.toString, "", "", ""))
          context.become(receiveTrack(WaitForTrackFile))
        case WaitForTrackFile =>
          context.become(receiveTrack(WaitForTrack))
      }

    case PeerClosed => context.stop(self)
  }

}
