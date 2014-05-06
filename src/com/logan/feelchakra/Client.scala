package com.logan.feelchakra

import android.util.Log

object Client {

  def props(): Props = {
    Props[Client]
  }

  case class Connect(remoteAddress: InetSocketAddress, stationMessengerRef: ActorRef)

}

import Client._

class Client extends Actor {


  def receive = {
    case Connect(remoteAddress, stationMessengerRef) => 
      val socket = new Socket()
      try {
        socket.bind(null);
        socket.connect(remoteAddress, 5000);
        stationMessengerRef ! StationMessenger.SetSocket(socket)

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
