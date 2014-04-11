
package com.logan.feelchakra

import android.util.Log

object Network {

  def props(): Props = {
    Props[Network]
  }

  case class SetPlaylistSubject(playlistSubject: ReplaySubject[Track])
  case class AddMessenger(remote: InetSocketAddress, socket: Socket)

}

class Network extends Actor {

  import Network._

  private var _messengerRefs: HashMap[InetSocketAddress, ActorRef] = HashMap[InetSocketAddress, ActorRef]()

  def receive = receiveSubject() 
    
  def receiveSubject(): Receive = {

    case SetPlaylistSubject(playlistSubject) => 
      context.become(receiveRemotes(playlistSubject))

  }

  def receiveRemotes(playlistSubject: ReplaySubject[Track]): Receive = {
    case AddMessenger(remote, socket) => startMessenger(remote, socket, playlistSubject)
  }

  def startMessenger(remote: InetSocketAddress, 
    socket: Socket, playlistSubject: ReplaySubject[Track]): Unit = {

    val messengerRef = context.actorOf(Messenger.props())
    messengerRef ! Messenger.SetSocket(socket)
    _messengerRefs = _messengerRefs.+((remote, messengerRef))
    playlistSubject.subscribe(track => {
      messengerRef.!(Messenger.WriteTrack(track: Track))
    })
  }

}


