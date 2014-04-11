
package com.logan.feelchakra

import android.util.Log

object Network {

  def props(): Props = {
    Props[Network]
  }

  case class AddMessenger(remote: InetSocketAddress, socket: Socket)
  case class WriteNextTrackOp(next: Option[Track]) 
  case object Shift
  case class WriteBothTracks(current: Option[Track], next: Option[Track]) 
  case class WritePlayState(play: PlayState)

}

class Network extends Actor {

  import Network._

  def receive = receiveRemotes(
    HashMap[InetSocketAddress, ActorRef](),
    None,
    None,
    NotPlaying
  ) 

  def receiveRemotes(
    messengerRefs: HashMap[InetSocketAddress, ActorRef],
    current: Option[Track],
    next: Option[Track],
    playState: PlayState
  ): Receive = {

    case AddMessenger(remote, socket) => 
      val messengerRef = context.actorOf(Messenger.props())
      messengerRef ! Messenger.SetSocket(socket)
      messengerRef ! Messenger.WriteBothTracks(current, next)
      context.become(
        receiveRemotes(messengerRefs.+(remote -> messengerRef), 
          current, next, playState) 
      )


    case WriteNextTrackOp(next) => 
      disperse(messengerRefs, Messenger.WriteTrackOp(next))
      context.become(
        receiveRemotes(messengerRefs, current, next, playState) 
      )

    case Shift => 
      disperse(messengerRefs, Messenger.Shift)
      context.become(
        receiveRemotes(messengerRefs, next, None, playState) 
      )

    case WriteBothTracks(current, next) => 
      disperse(messengerRefs, Messenger.WriteBothTracks(current, next))
      context.become(
        receiveRemotes(messengerRefs, current, next, playState) 
      )

    case WritePlayState(playState) => 
      disperse(messengerRefs, Messenger.WritePlayState(playState))
      context.become(
        receiveRemotes(messengerRefs, current, next, playState) 
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




