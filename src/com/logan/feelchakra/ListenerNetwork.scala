package com.logan.feelchakra

import android.util.Log

object ListenerNetwork {

  def props(): Props = {
    Props[ListenerNetwork]
  }

  case class AddMessenger(remote: InetSocketAddress, socket: Socket)
  case class SetClientMessenger(remote: InetSocketAddress, socket: Socket)
  case class NotifyMessengers(message: Object) 

}

import ListenerNetwork._

class ListenerNetwork extends Actor {

  var messengerRefs = HashMap[InetSocketAddress, ActorRef]()
  var currentTrack: Option[Track] = None

  def receive = { 

    case AddMessenger(remote, socket) => 
      val messengerRef = context.actorOf(ListenerMessenger.props())
      messengerRef ! ListenerMessenger.SetSocket(socket)
      messengerRef ! ListenerMessenger.WriteTrackOp(currentTrack)
      messengerRefs = messengerRefs.+(remote -> messengerRef)
      context.become(receiveNotify)

  }

  def receiveNotify: Receive = { 

    case NotifyMessengers(message) => 
      notifyMessengers(message)

  }

  private def notifyMessengers(message: Object): Unit = {
    messengerRefs.foreach(pair => { 
      val ref = pair._2
      ref.!(message)
    })
  }
}
