
package com.logan.feelchakra

import android.util.Log

object Network {

  def props(): Props = {
    Props[Network]
  }

  case class AddMessenger(remote: InetSocketAddress, socket: Socket)
  case class SetClientMessenger(remote: InetSocketAddress, socket: Socket)
  case class WriteNextTrackOp(next: Option[Track]) 
  case object Shift
  case class WriteBothTracks(current: Option[Track], next: Option[Track]) 
  case class WritePlayState(playState: PlayState)

}

class Network extends Actor {

  import Network._

  def receive = receiveRemotes(
    None,
    HashMap[InetSocketAddress, ActorRef](),
    None,
    None,
    new PlayState 
  ) 

  def receiveRemotes(
    messengerRefOp: Option[ActorRef],
    messengerRefs: HashMap[InetSocketAddress, ActorRef],
    current: Option[Track],
    next: Option[Track],
    playState: PlayState
  ): Receive = {

    case SetClientMessenger(remote, socket) => 
      val messengerRef = context.actorOf(Messenger.props())
      messengerRef ! Messenger.SetSocket(socket)
      context.become(
        receiveRemotes(
          Some(messengerRef),
          messengerRefs, current, next, playState
        ) 
      )

    case AddMessenger(remote, socket) => 
      val messengerRef = context.actorOf(Messenger.props())
      messengerRef ! Messenger.SetSocket(socket)
      messengerRef ! Messenger.WriteBothTracks(current, next)
      messengerRef ! Messenger.WritePlayState(playState)
      context.become(
        receiveRemotes(
          messengerRefOp,
          messengerRefs.+(remote -> messengerRef), 
          current, next, playState) 
      )


    case WriteNextTrackOp(next) => 
      disperse(messengerRefs, Messenger.WriteNextTrackOp(next))
      context.become(
        receiveRemotes(
          messengerRefOp,
          messengerRefs, current, next, playState) 
      )

    case Shift => 
      disperse(messengerRefs, Messenger.Shift)
      context.become(
        receiveRemotes(
          messengerRefOp,
          messengerRefs, next, None, playState) 
      )

    case WriteBothTracks(current, next) => 
      disperse(messengerRefs, Messenger.WriteBothTracks(current, next))
      context.become(
        receiveRemotes(
          messengerRefOp,
          messengerRefs, current, next, playState) 
      )

    case WritePlayState(playState) => 
      disperse(messengerRefs, Messenger.WritePlayState(playState))
      context.become(
        receiveRemotes(
          messengerRefOp,
          messengerRefs, current, next, playState) 
      )

  }
  
  private def disperse(
    messengerRefs: HashMap[InetSocketAddress, ActorRef], 
    message: Object
  ): Unit = {
    messengerRefs.foreach(pair => { 
      val ref = pair._2
      ref.!(message)
    })
  }

}




