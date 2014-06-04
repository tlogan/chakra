package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import akka.util.Timeout

object SocketReader {
}

import SocketReader._

abstract class SocketReader(socket: Socket, val writerRef: ActorRef) {

  var socketInput: InputStream = socket.getInputStream()
  var dataInput: DataInputStream = new DataInputStream(socketInput)

  def read(): Unit = {
    val f = Future {
      val messageType = dataInput.readInt()

      if (receive.isDefinedAt(messageType)) {
        receive(messageType)
      } else {
        Log.d("chakra", "read problem: not defined at " + messageType)
      }

    } onComplete {
      case Success(_) => read()
      case Failure(e) => 
        try {
          socketInput.close()
          Log.d("chakra", "read fail, closing socket")
          e.printStackTrace()
        } catch {
          case e: IOException => 
            Log.d("chakra", "error closing socket")
            e.printStackTrace()
        }
    }

  }

  def receive: PartialFunction[Any, Unit] 


}
