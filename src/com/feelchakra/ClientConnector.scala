
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


object ClientConnector {

  def props(remoteAddress: InetSocketAddress): Props = {
    Props(classOf[ClientConnector], remoteAddress)
  }

}

class ClientConnector(remoteAddress: InetSocketAddress) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remoteAddress)

  var _messengerRef: ActorRef = _ 

  def receive = {

    case  CommandFailed(_: Connect) => context.stop(self)
    case  c @ Connected(remote, local) =>
      val connectionRef = sender
      _messengerRef = context.actorOf(ClientMessenger.props(connectionRef))
      connectionRef ! Register(_messengerRef)

  }

}
