package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import akka.util.Timeout

object ListenerReader {

  def create(socket: Socket, writerRef: ActorRef): Runnable = {
    val socketInput: InputStream = socket.getInputStream()
    val dataInput: DataInputStream = new DataInputStream(socketInput)
    new Runnable() {
      override def run(): Unit = Reader.read( 
          socketInput, 
          dataInput,
          Reader.receiveReadSyncRequest(writerRef)
      )
    }
  }

}
