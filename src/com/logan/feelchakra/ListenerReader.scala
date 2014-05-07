package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object ListenerReader {

}

import ListenerReader._
import SocketReader._

class ListenerReader(socket: Socket, writer: ActorRef) extends SocketReader(socket, writer) {

  override def receive = receiveReadSync
  
}
