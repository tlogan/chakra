package com.logan.feelchakra

import android.util.Log

object Client {

  def props(): Props = {
    Props[Client]
  }

  case class Connect(remoteAddress: InetSocketAddress)

}

import Client._

class Client extends Actor {


  def receive = {
    case Connect(remoteAddress) => 
      val socket = new Socket()
      try {
        socket.bind(null);
        socket.connect(remoteAddress, 5000);
        mainActorRef ! MainActor.ChangeStationMessenger(socket)

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
