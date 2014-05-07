package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object StationMessenger {

  def props(): Props = {
    Props[StationMessenger]
  }

  case class SetSocket(socket: Socket)

}

import StationMessenger._
import Messenger._

class StationMessenger extends Actor with Messenger {

  def receive = {

    case SetSocket(socket) => 
      setSocket(socket)
      val stationReader = new StationReader(socket, self)
      stationReader.read()

      writeSyncRequest()
      context.become(receiveWriteSync)

  }



}




