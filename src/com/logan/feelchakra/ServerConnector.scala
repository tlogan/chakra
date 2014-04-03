package com.logan.feelchakra

import android.util.Log

class ServerConnector extends Actor {

  import Tcp._
  import ServerConnector._
  import context.system

  var _messengerRefs: HashMap[InetSocketAddress, ActorRef] = HashMap[InetSocketAddress, ActorRef]()

  def receive = {

    case BindAddress(localAddress) =>
      Log.d("chakra", "Binding Address:  " + localAddress)
      IO(Tcp) ! Bind(self, localAddress)
    case b @ Bound(localAddress) => {}
    case CommandFailed(_: Bind) => context.stop(self)
    case c @ Connected(remote, local) =>
      val connectionRef = sender
      val messengerRef = context.actorOf(ServerMessenger.props(connectionRef))
      _messengerRefs = _messengerRefs.+((remote, messengerRef))
      connectionRef ! Register(messengerRef)

    case OnNextTrack(track: Track) =>
      Log.d("chakra", "sending through serverConnector messengers: " + _messengerRefs.size)
      _messengerRefs.foreach(pair => {
        val messengerRef = pair._2 
        Log.d("chakra", "sending through serverConnector: " + track.path)
        messengerRef.!(ServerMessenger.OnNextTrack(track: Track))
      })

  }

}

object ServerConnector {

  def props(): Props = {
    Props[ServerConnector]
  }

  case class OnNextTrack(track: Track)
  case class BindAddress(localAddress: InetSocketAddress)

}

