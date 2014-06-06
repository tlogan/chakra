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
      try {
        receive(messageType)
      } catch {
        case e: Throwable => 
          Log.d("chakra", "error reading message: " + messageType)
          throw e
      }

    } onComplete {
      case Success(_) => read()
      case Failure(e) => 
        try {
          socketInput.close()
        } catch {
          case e: IOException => 
            Log.d("chakra", "error closing socket")
            e.printStackTrace()
        } finally {
          Log.d("chakra", "error reading")
          throw e
        }
    }

  }

  def receive: PartialFunction[Any, Unit] 


}
