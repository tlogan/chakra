package com.feelchakra

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import akka.dispatch.RequiresMessageQueue
import akka.dispatch.UnboundedMessageQueueSemantics

import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.Observer
import scala.concurrent.Future
import scala.concurrent.Promise
import android.provider.MediaStore 

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ListBuffer

import android.os.Handler

import guava.scala.android.Database
import guava.scala.android.Table

import android.util.Log 
import scala.util.{Success,Failure}

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager._

import java.net.InetSocketAddress 

import akka.actor.{Actor, ActorRef, Props}
 
object OutputHandler {

  sealed trait OnChange 
  case class OnSelectionListChanged(selectionList: List[Selection]) extends OnChange
  case class OnPlayerOpenChanged(playerOpen: Boolean) extends OnChange
  case class OnSelectionChanged(selection: Selection) extends OnChange
  case class OnTrackListChanged(trackList: List[Track]) extends OnChange
  case class OnStationOptionChanged(stationOption: Option[Station]) extends OnChange
  case class OnStationListChanged(stationList: List[Station]) extends OnChange
  case class OnTrackIndexChanged(trackIndex: Int) extends OnChange
  case class OnPlaylistChanged(playlist: List[Track]) extends OnChange

  case class OnTrackOptionChanged(trackOption: Option[Track]) extends OnChange
  case class OnPlayStateChanged(playOncePrepared: Boolean) extends OnChange
  case class OnPositionChanged(positionOncePrepared: Int) extends OnChange
  case class OnProfileChanged(localAddress: InetSocketAddress, serviceName: String, serviceType: String) extends OnChange
  case class OnRemoteTrackChanged(track: Track) extends OnChange


}
