package com.logan.feelchakra

import android.util.Log

object Messenger {

  def props(): Props = {
    Props[Messenger]
  }

  case class SetConnectionRef(connectionRef: ActorRef)

  case class OnNextTrack(track: Track)
  case object WaitForData

  sealed trait WaitMode
  case object TrackMode extends WaitMode
  case object TrackFileMode extends WaitMode

}

class Messenger(connectionRef: ActorRef) extends Actor {

  import Tcp._
  import Messenger._

  case object Ack extends Event
  
  var _connectionRef: ActorRef = _

  val mainActorRef = MainActor.mainActorRef

  def receive = receiveConnectionRef

  def receiveConnectionRef: Receive = {
    case SetConnectionRef(connectionRef) =>
      Log.d("chakra", "setting connnectionRef " + connectionRef)
      _connectionRef = connectionRef
      context.become(receiveMessages(TrackMode))
  }

  def receiveMessages(waitMode: WaitMode): Receive = {

    case Received(data) => 
      waitMode match {
        case TrackMode =>
          mainActorRef ! MainActor.SetRemoteTrack(Track(data.toString, "", "", ""))
          context.become(receiveMessages(TrackFileMode))
        case TrackFileMode =>
          context.become(receiveMessages(TrackMode))
      }
    case PeerClosed => context.stop(self)

    case OnNextTrack(track) => 
      Log.d("chakra", "sending through serverMessenger: " + track.path)
      val message = CompoundWrite(Write(ByteString(track.path)), WriteFile(track.path, 0, 0, Ack)) 
      connectionRef.!(message)

  }

}
