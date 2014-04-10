package com.logan.feelchakra

import android.util.Log

object Client {

  def props(): Props = {
    Props[Client]
  }

  case class Connect(remoteAddress: InetSocketAddress)

}

class Client extends Actor {

  import Client._

  def receive = {

    case Connect(remoteAddress) =>
      Log.d("chakra", "Connecting: " + remoteAddress)
      val socket = new Socket()
      try {
        socket.bind(null);
        socket.connect(remoteAddress, 5000);
        context.parent ! Network.AddMessenger(remoteAddress, socket)

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
