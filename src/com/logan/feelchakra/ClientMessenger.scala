package com.logan.feelchakra

object ClientMessenger {

  def props(connectionRef: ActorRef): Props = {
    Props(classOf[ClientMessenger], connectionRef)
  }

  sealed trait WaitForData
  case object WaitForTrack extends WaitForData
  case object WaitForTrackFile extends WaitForData


}

class ClientMessenger(connectionRef: ActorRef) extends Actor {

  import Tcp._
  import ClientMessenger._

  val mainActorRef = MainActor.mainActorRef

  def receive = receiveTrack(WaitForTrack)

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
