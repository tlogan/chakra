package com.logan.feelchakra

import android.util.Log

object Client {

  def props(): Props = {
    Props[Client]
  }

  case class ConnectAddress(remoteAddress: InetSocketAddress)

}

class Client extends Actor {

  import Client._
  import context.system


  var _messengerRef: ActorRef = _ 
  var _r: InetSocketAddress = _

  def receive = {

    case ConnectAddress(remoteAddress) =>
      Log.d("chakra", "Connecting: " + remoteAddress)
      IO(Tcp) ! Tcp.Connect(remoteAddress)

    case  Tcp.CommandFailed(x: Tcp.Connect) => 
      Log.d("chakra", "Command Failed: " + x)
      context.stop(self)

    case  c @ Tcp.Connected(remote, local) =>
      Log.d("chakra", "client connected to: " + remote)
      val connectionRef = sender
      context.parent ! Network.AddMessenger(remote, connectionRef)

  }

}
