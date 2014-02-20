package com.feelchakra

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import akka.dispatch.RequiresMessageQueue
import akka.dispatch.UnboundedMessageQueueSemantics

import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.Observer
import scala.concurrent.Future
import android.provider.MediaStore 

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.HashMap

import android.os.Handler

import guava.scala.android.Database
import guava.scala.android.Table
import android.util.Log 
import scala.util.{Success,Failure}

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager._



import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress 


object ClientMessenger {

  def props(connectionRef: ActorRef): Props = {
    Props(classOf[ClientMessenger], connectionRef)
  }

  sealed trait WaitForData
  case object WaitForTrack extends WaitForData
  case object WaitForTrackFile extends WaitForData


}

class ClientMessenger(connectionRef: ActorRef) extends Actor {

  import Tcp._
  import ClientMessenger._

  val mainActorRef = MainActor.mainActorRef

  def receive = receiveTrack(WaitForTrack)

  def receiveTrack(waitForData: WaitForData): Receive = {

    case Received(data) => 
      waitForData match {
        case WaitForTrack =>
          mainActorRef ! MainActor.SetRemoteTrack(Track(data.toString, "", "", ""))
          context.become(receiveTrack(WaitForTrackFile))
        case WaitForTrackFile =>
          context.become(receiveTrack(WaitForTrack))
      }

    case PeerClosed => context.stop(self)
  }

}
