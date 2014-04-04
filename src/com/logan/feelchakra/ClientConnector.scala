package com.logan.feelchakra

import android.util.Log

object ClientConnector {

  def props(): Props = {
    Props[ClientConnector]
  }

  case class ConnectAddress(remoteAddress: InetSocketAddress)

}

class ClientConnector extends Actor {

  import ClientConnector._
  import context.system


  var _messengerRef: ActorRef = _ 
  var _r: InetSocketAddress = _

  def receive = {

    case ConnectAddress(remoteAddress) =>
      Log.d("chakra", "Connecting: " + remoteAddress)
      _r = remoteAddress
      IO(Tcp) ! Tcp.Connect(remoteAddress)

    case  Tcp.CommandFailed(_: Tcp.Connect) => 
      Log.d("chakra", "Command Failed")
      context.stop(self)

    case  c @ Tcp.Connected(remote, local) =>
      Log.d("chakra", "Connected")
      val connectionRef = sender
      _messengerRef = context.actorOf(ClientMessenger.props())
      _messengerRef ! ClientMessenger.SetConnectionRef(connectionRef)
      connectionRef ! Tcp.Register(_messengerRef)

  }

}
