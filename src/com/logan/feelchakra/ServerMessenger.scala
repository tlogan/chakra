package com.logan.feelchakra

import android.util.Log

object ServerMessenger {

  def props(): Props = {
    Props[ServerMessenger]
  }

  case class SetConnectionRef(connectionRef: ActorRef)

  case class OnNextTrack(track: Track)

}

class ServerMessenger(connectionRef: ActorRef) extends Actor {

  import Tcp._
  import ServerMessenger._

  case object Ack extends Event
  
  var _connectionRef: ActorRef = _

  val mainActorRef = MainActor.mainActorRef

  def receive = receiveConnectionRef

  def receiveConnectionRef: Receive = {
    case SetConnectionRef(connectionRef) =>
      Log.d("chakra", "setting connnectionRef " + connectionRef)
      _connectionRef = connectionRef
      context.become(receiveMessages)
  }

  def receiveMessages: Receive = {

    case Received(data) => 
    case PeerClosed => context.stop(self)


    case OnNextTrack(track) => 
      Log.d("chakra", "sending through serverMessenger: " + track.path)
      val message = CompoundWrite(Write(ByteString(track.path)), WriteFile(track.path, 0, 0, Ack)) 
      connectionRef.!(message)

  }

}
