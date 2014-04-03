package com.logan.feelchakra

import android.util.Log

object ServerMessenger {

  def props(connectionRef: ActorRef): Props = {
    Props(classOf[ServerMessenger], connectionRef)
  }

  case class OnNextTrack(track: Track)

}

class ServerMessenger(connectionRef: ActorRef) extends Actor {

  import Tcp._
  import ServerMessenger._

  case object Ack extends Event

  val mainActorRef = MainActor.mainActorRef

  def receive = {

    case Received(data) => 
    case PeerClosed => context.stop(self)


    case OnNextTrack(track) => 
      Log.d("chakra", "sending through serverMessenger: " + track.path)
      val message = CompoundWrite(Write(ByteString(track.path)), WriteFile(track.path, 0, 0, Ack)) 
      connectionRef.!(message)

  }

}
