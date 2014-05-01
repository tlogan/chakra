package com.logan.feelchakra

import android.util.Log

object Network {

  def props(): Props = {
    Props[Network]
  }

  case class AddMessenger(remote: InetSocketAddress, socket: Socket)
  case class SetClientMessenger(remote: InetSocketAddress, socket: Socket)
  case class NotifyMessengers(message: Object) 

}

class Network extends Actor {

  import Network._

  def receive = receiveRemotes(
    None,
    HashMap[InetSocketAddress, ActorRef](),
    None
  ) 

  def receiveRemotes(
    messengerRefOp: Option[ActorRef],
    messengerRefs: HashMap[InetSocketAddress, ActorRef],
    current: Option[Track]
  ): Receive = {

    case SetClientMessenger(remote, socket) => 
      val messengerRef = context.actorOf(Messenger.props())
      messengerRef ! Messenger.SetSocket(socket)
      context.become(
        receiveRemotes(
          Some(messengerRef),
          messengerRefs, current
        ) 
      )

    case AddMessenger(remote, socket) => 
      val messengerRef = context.actorOf(Messenger.props())
      messengerRef ! Messenger.SetSocket(socket)
      messengerRef ! Messenger.WriteTrackOp(current)
      context.become(
        receiveRemotes(
          messengerRefOp,
          messengerRefs.+(remote -> messengerRef), 
          current) 
      )

    case NotifyMessengers(message) => 
      notifyMessengers(messengerRefs, message)
      context.become(
        receiveRemotes(
          messengerRefOp,
          messengerRefs, current) 
      )

  }
  
  private def notifyMessengers(
    messengerRefs: HashMap[InetSocketAddress, ActorRef], 
    message: Object
  ): Unit = {
    messengerRefs.foreach(pair => { 
      val ref = pair._2
      ref.!(message)
    })
  }

}




