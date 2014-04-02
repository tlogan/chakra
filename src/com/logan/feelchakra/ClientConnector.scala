package com.logan.feelchakra

object ClientConnector {

  def props(remoteAddress: InetSocketAddress): Props = {
    Props(classOf[ClientConnector], remoteAddress)
  }

}

class ClientConnector(remoteAddress: InetSocketAddress) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remoteAddress)

  var _messengerRef: ActorRef = _ 

  def receive = {

    case  CommandFailed(_: Connect) => context.stop(self)
    case  c @ Connected(remote, local) =>
      val connectionRef = sender
      _messengerRef = context.actorOf(ClientMessenger.props(connectionRef))
      connectionRef ! Register(_messengerRef)

  }

}
