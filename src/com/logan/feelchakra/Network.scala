
package com.logan.feelchakra

import android.util.Log

object Network {

  def props(): Props = {
    Props[Network]
  }


  case class AcceptRemotes(localAddress: InetSocketAddress)
  case class ConnectRemote(remoteAddress: InetSocketAddress)

  case class AddMessenger(remote: InetSocketAddress, connectionRef: ActorRef)

  case class OnNextTrack(track: Track)
  case class WaitFrom(remoteAddress: InetSocketAddress)

}

class Network extends Actor {

  import Network._

  private val serverRef: ActorRef = context.actorOf(Server.props(), "Server")

  private var clientRef: ActorRef = context.actorOf(Client.props(), "Client")

  private var _messengerRefs: HashMap[InetSocketAddress, ActorRef] = HashMap[InetSocketAddress, ActorRef]()

  def receive = {

    case AcceptRemotes(localAddress) =>
      serverRef.!(Server.BindAddress(localAddress))

    case ConnectRemote(remoteAddress) =>
      clientRef.!(Client.ConnectAddress(remoteAddress))

    case AddMessenger(remote, connectionRef) =>
      val messengerRef = context.actorOf(Messenger.props())
      messengerRef ! Messenger.SetConnectionRef(connectionRef)
      connectionRef ! Tcp.Register(messengerRef)
      _messengerRefs = _messengerRefs.+((remote, messengerRef))


    case OnNextTrack(track) =>
      Log.d("chakra", "sending through network messengers: " + _messengerRefs.size)
      _messengerRefs.foreach(pair => {
        val messengerRef = pair._2 
        Log.d("chakra", "sending through network: " + track.path)
        messengerRef.!(Messenger.OnNextTrack(track: Track))
      })

  }


}


