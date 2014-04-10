
package com.logan.feelchakra

import android.util.Log

object Network {

  def props(): Props = {
    Props[Network]
  }

  case class SetSubject(subject: ReplaySubject[Track])
  case object AcceptRemotes
  case class ConnectRemote(remoteAddress: InetSocketAddress)

  case class AddMessenger(remote: InetSocketAddress, socket: Socket)

  case class OnNextTrack(track: Track)
  case class WaitFrom(remoteAddress: InetSocketAddress)

}

class Network extends Actor {

  import Network._

  private val serverRef: ActorRef = context.actorOf(Server.props(), "Server")

  private var clientRef: ActorRef = context.actorOf(Client.props(), "Client")

  private var _playlistSubject: ReplaySubject[Track] = _

  private var _messengerRefs: HashMap[InetSocketAddress, ActorRef] = HashMap[InetSocketAddress, ActorRef]()

  def receive = {

    case SetSubject(subject) =>
      _playlistSubject = subject

    case AcceptRemotes =>
      serverRef.!(Server.Accept)

    case ConnectRemote(remoteAddress) =>
      Log.d("chakra", "Network connecting remotes: " + remoteAddress)
      clientRef.!(Client.Connect(remoteAddress))

    case AddMessenger(remote, socket) =>
      Log.d("chakra", "adding new messenger: " + remote)
      val messengerRef = context.actorOf(Messenger.props())
      messengerRef ! Messenger.SetSocket(socket)
      _messengerRefs = _messengerRefs.+((remote, messengerRef))
      _playlistSubject.subscribe(track => {
        Log.d("chakra", "sending through network: " + track.path)
        messengerRef.!(Messenger.OnNextTrack(track: Track))
      })

  }

}


