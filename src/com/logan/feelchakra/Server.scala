package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object Server {

  def props(): Props = {
    Props[Server]
  }

  case class Accept(networkRef: ActorRef)

}

class Server extends Actor {

  import Server._

  def receive = { 
    case Accept(networkRef) => accept(networkRef)
  }


  def accept(networkRef: ActorRef): Unit = {
    Future {
      try {
        val serverSocket = new ServerSocket(0);
        val newLocalAddress = {
          new InetSocketAddress("localhost", serverSocket.getLocalPort())
        }
        mainActorRef ! MainActor.SetLocalAddress(newLocalAddress)

        while (true) {
          try {
            val socket = serverSocket.accept();
            val remote = {
              new InetSocketAddress(socket.getInetAddress(), socket.getPort())
            }
            networkRef ! Network.AddMessenger(remote, socket)
          } catch {
            case e: IOException => 
              try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                  serverSocket.close();
                }
                e.printStackTrace();
              } catch {
                case e: IOException => {}
              }
          }
        }

      } catch {
        case e: IOException => e.printStackTrace()
      }
    }
  }

}


