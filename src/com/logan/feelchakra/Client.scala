package com.logan.feelchakra

import android.util.Log

object Client {

  def props(): Props = {
    Props[Client]
  }

  case class Connect(remoteAddress: InetSocketAddress, networkRef: ActorRef)

}

class Client extends Actor {

  import Client._

  def receive = {
    case Connect(remoteAddress, networkRef) => connect(remoteAddress, networkRef)
  }

  def connect(remoteAddress: InetSocketAddress, networkRef: ActorRef): Unit = {
    val socket = new Socket()
    try {
      socket.bind(null);
      socket.connect(remoteAddress, 5000);
      networkRef ! Network.AddMessenger(remoteAddress, socket)

    } catch  {
      case e: IOException =>
        e.printStackTrace()
        try {
          socket.close()
        } catch {
          case e: IOException => e.printStackTrace()
        }
    }
  }

}
