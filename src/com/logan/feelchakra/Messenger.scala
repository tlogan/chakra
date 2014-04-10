package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global

object Messenger {

  def props(): Props = {
    Props[Messenger]
  }

  case class SetSocket(socket: Socket)
  case class OnNextTrack(track: Track)

}

class Messenger(connectionRef: ActorRef) extends Actor {

  import Messenger._

  def receive = receiveSocket

  def receiveSocket: Receive = {
    case SetSocket(socket) =>
      Log.d("chakra", "setting socket " + socket)
      Future {
        while (true){
          try {
            readTrack(socket);
          } catch {
            case e: IOException => 
              e.printStackTrace();
              try {
                socket.close();
              } catch {
                case e: IOException => e.printStackTrace();
              }
          }
        }
      }
      context.become(receiveMessages(socket))

  }

  def receiveMessages(socket: Socket): Receive = {

    case OnNextTrack(track) => 
      Log.d("chakra", "sending through serverMessenger: " + track.path)
      writeTrack(track, socket)

  }

  def writeTrack(track: Track, socket: Socket): Unit = {
    try {
      val socketOutput = socket.getOutputStream()
      val dataOutput = new DataOutputStream(socketOutput)

      //write the file path
      dataOutput.writeLong(track.path.length())
      dataOutput.flush()
      socketOutput.write(track.path.getBytes())

    } catch {
      case e: IOException => e.printStackTrace();
    }
  }


  def readTrack(socket: Socket): Unit = {

    val socketInput = socket.getInputStream()
    val dataInput = new DataInputStream(socketInput)

    val trackPathSize = dataInput.readLong().toInt
    val trackPathBuffer = new Array[Byte](trackPathSize)
    socketInput.read(trackPathBuffer)
    val path = new String(trackPathBuffer, 0, trackPathSize)

    mainActorRef ! MainActor.SetRemoteTrack(Track(path, "", "", ""))

  }

}
