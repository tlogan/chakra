package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object StationWriter {

  def props(): Props = {
    Props[StationWriter]
  }

  case class SetSocket(socket: Socket)

}

import StationWriter._
import SocketWriter._

class StationWriter extends Actor with SocketWriter {

  def receive = {

    case SetSocket(socket) => 
      Log.d("chakra", "setting socket in station writer")
      setSocket(socket)
      writeSyncRequest()
      context.become(receiveWriteSync)

  }



}




