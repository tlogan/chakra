package com.logan.feelchakra

import android.util.Log

object Client {

  def props(): Props = {
    Props[Client]
  }

  case class Connect(remoteAddress: InetSocketAddress, stationWriterRef: ActorRef)

}

import Client._

class Client extends Actor {


  def receive = {
    case Connect(remoteAddress, stationWriterRef) => 
      val socket = new Socket()
      try {
        socket.bind(null);
        socket.connect(remoteAddress, 5000);
        stationWriterRef ! StationWriter.SetSocket(socket)

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
