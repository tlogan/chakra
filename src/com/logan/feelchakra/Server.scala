package com.logan.feelchakra

import android.util.Log

object Server {

  def props(): Props = {
    Props[Server]
  }

  case object Accept 

}

class Server extends Actor {

  import Server._

  def receive = { 

    case Accept =>
      try {
        val serverSocket = new ServerSocket(0);
        val newLocalAddress = {
          new InetSocketAddress("localhost", serverSocket.getLocalPort())
        }
        mainActorRef ! MainActor.SetLocalAddress(newLocalAddress)

        while (true){
          try {
            val socket = serverSocket.accept();
            val remote = {
              new InetSocketAddress(socket.getInetAddress(), socket.getPort())
            }
            context.parent ! Network.AddMessenger(remote, socket)
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


