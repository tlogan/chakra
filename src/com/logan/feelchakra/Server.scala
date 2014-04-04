package com.logan.feelchakra

import android.util.Log

object Server {

  def props(): Props = {
    Props[Server]
  }


  case class BindAddress(localAddress: InetSocketAddress)
  case class ChangeAcceptance(accept: Boolean)
  case class OnNextTrack(track: Track)

}

class Server extends Actor {

  import Tcp._
  import Server._
  import context.system

  var _messengerRefs: HashMap[InetSocketAddress, ActorRef] = HashMap[InetSocketAddress, ActorRef]()

  def receive = { 

    case BindAddress(localAddress) =>
      Log.d("chakra", "Binding Address:  " + localAddress)
      IO(Tcp) ! Bind(self, localAddress)
    case b @ Bound(localAddress) => 
      Log.d("chakra", "Bound: " + localAddress)
    case CommandFailed(x: Bind) => 
      Log.d("chakra", "Binding Failed: " + x)
      context.stop(self)
    case c @ Connected(remote, local) =>
      Log.d("chakra", "server connected to: " + remote)
      val connectionRef = sender
      context.parent ! Network.AddMessenger(remote, connectionRef)

  }

}


