package com.logan.feelchakra

object ServerConnector {

  def props(localAddress: InetSocketAddress): Props = {
    Props(classOf[ServerConnector], localAddress)
  }

  case class OnNextTrack(track: Track)

}

class ServerConnector(localAddress: InetSocketAddress) extends Actor {

  import Tcp._
  import ServerConnector._
  import context.system

  IO(Tcp) ! Bind(self, localAddress)

  var _messengerRefs: HashMap[InetSocketAddress, ActorRef] = HashMap[InetSocketAddress, ActorRef]()

  def receive = {

    case  b @ Bound(localAddress) =>
    case  CommandFailed(_: Bind) => context.stop(self)
    case  c @ Connected(remote, local) =>
      val connectionRef = sender
      val messengerRef = context.actorOf(ServerMessenger.props(connectionRef))
      _messengerRefs = _messengerRefs.+((remote, messengerRef))
      connectionRef ! Register(messengerRef)

    case OnNextTrack(track: Track) =>
      _messengerRefs.foreach(pair => {
        val messengerRef = pair._2 
        messengerRef.!(ServerMessenger.OnNextTrack(track: Track))
      })

  }

}
