package com.logan.feelchakra

import android.util.Log

object Server {

  def props(): Props = {
    Props[Server]
  }

  case class BindAddress(localAddress: InetSocketAddress)

}

class Server extends Actor {

  import Server._

  def receive = { 

    case BindAddress(localAddress) =>
      try {
        val port = localAddress.getPort()
        val serverSocket = new ServerSocket(port);

        val newLocalAddress = {
          new InetSocketAddress(localAddress.getHostName(), serverSocket.getLocalPort())
        }

        mainActorRef ! MainActor.SetLocalAddress(newLocalAddress)

        while (true){
          try {
            val socket = serverSocket.accept();
            val remote = {
              new InetSocketAddress(localAddress.getAddress(), socket.getPort())
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


