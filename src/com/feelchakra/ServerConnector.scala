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


object ServerConnector {

  def props(localAddress: InetSocketAddress): Props = {
    Props(classOf[ServerConnector], localAddress)
  }

  case class OnNextTrack(track: Track)

}

class ServerConnector(localAddress: InetSocketAddress) extends Actor {

  import Tcp._
  import ServerConnector._
  import context.system

  IO(Tcp) ! Bind(self, localAddress)

  var _messengerRefs: HashMap[InetSocketAddress, ActorRef] = HashMap[InetSocketAddress, ActorRef]()

  def receive = {

    case  b @ Bound(localAddress) =>
    case  CommandFailed(_: Bind) => context.stop(self)
    case  c @ Connected(remote, local) =>
      val connectionRef = sender
      val messengerRef = context.actorOf(ServerMessenger.props(connectionRef))
      _messengerRefs = _messengerRefs.+((remote, messengerRef))
      connectionRef ! Register(messengerRef)

    case OnNextTrack(track: Track) =>
      _messengerRefs.foreach(pair => {
        val messengerRef = pair._2 
        messengerRef.!(ServerMessenger.OnNextTrack(track: Track))
      })



  }

}
