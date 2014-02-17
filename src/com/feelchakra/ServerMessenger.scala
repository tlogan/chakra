
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

import scala.pickling._
import json._ 

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress 

object ServerMessenger {

  def props(connectionRef: ActorRef): Props = {
    Props(classOf[ServerMessenger], connectionRef)
  }

  case class OnNextTrack(track: Track)

}

class ServerMessenger(connectionRef: ActorRef) extends Actor {

  import Tcp._
  import ServerMessenger._

  case object Ack extends Event

  val mainActorRef = MainActor.mainActorRef

  def receive = {

    case Received(data) => 
    case PeerClosed => context.stop(self)


    case OnNextTrack(track) => 
      val message = CompoundWrite(Write(ByteString(track.path)), WriteFile(track.path, 0, 0, Ack)) 
      connectionRef.!(message)

  }

}
