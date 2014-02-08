package com.feelchakra

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.app.ActionBar
import android.app.FragmentTransaction
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view._
import android.widget._
import android.widget.LinearLayout.LayoutParams._
import android.view.ViewGroup.LayoutParams._ 

import scala.collection.immutable.List
import guava.scala.android.Database

import android.graphics.Color

import android.util.Log 
import scala.util.{Success,Failure}
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

import java.io.IOException

import guava.scala.android.RichMediaPlayer._

import java.net.ServerSocket;
import java.net.Socket;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.IBinder
import android.widget.Toast

import android.net.NetworkInfo

import scala.collection.JavaConverters._


object PlayerService {

   case class OnMainActorConnected(trackOption: Option[Track], playOncePrepared: Boolean, positionOncePrepared: Int)
   case class OnTrackOptionChanged(trackOption: Option[Track]) 
   case class OnPlayStateChanged(playOncePrepared: Boolean) 
   case class OnPositionChanged(positionOncePrepared: Int) 

  case class OnStationOptionChanged(stationOption: Option[Station], serviceName: String, serviceType: String, record: java.util.Map[String, String])

}

class PlayerService extends Service {

  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      import PlayerService._
      msg.obj match {
        case OnMainActorConnected(
          trackOption, playOncePrepared, positionOncePrepared
        ) =>
          that.onMainActorconnected(trackOption, playOncePrepared, positionOncePrepared); true
        case OnTrackOptionChanged(trackOption) => 
          that.onTrackOptionChanged(trackOption); true
        case OnPlayStateChanged(playOncePrepared) =>
          that.onPlayStateChanged(playOncePrepared); true
        case OnPositionChanged(positionOncePrepared) =>
          that.onPositionChanged(positionOncePrepared); true
        case OnStationOptionChanged(stationOption, serviceName, serviceType, record) =>
          that.onStationOptionChanged(stationOption, serviceName, serviceType, record); true
        case _ => false
      }
    }
  })



  private val mainActorRef = MainActor.mainActorRef
  private val mediaPlayer = new MediaPlayer()
  private val that = this

  private var _playOncePrepared: Boolean = false
  private var _positionOncePrepared: Int = 0
  private var _prepared: Boolean = false

  private var _manager: WifiP2pManager = _
  private var _channel: WifiP2pManager.Channel = _
  private var _broadcastReceiver: BroadcastReceiver = _
  private var _serverSocket: ServerSocket = _
  private var _serviceInfo: WifiP2pDnsSdServiceInfo = _
  private var _serviceRequest: WifiP2pDnsSdServiceRequest = _

  override def onBind(intent: Intent): IBinder = {
    return null;
  }



  override def onCreate(): Unit = {


    mediaPlayer.setOnPrepared(mp => {
      mp.seekTo(_positionOncePrepared)
      if (_playOncePrepared) mp.start()
      _prepared = true

    })
    mediaPlayer.setOnCompletion(mp => {/*notify main actor*/})
    mainActorRef ! MainActor.SetPlayerServiceHandler(handler)


    _manager = that.getSystemService(Context.WIFI_P2P_SERVICE) match {
      case m: WifiP2pManager => m
    }
    _channel = _manager.initialize(that, that.getMainLooper(), null)

    _broadcastReceiver = new BroadcastReceiver() {
      
      override def onReceive(context: Context, intent: Intent): Unit = {
        import WifiP2pManager._
        intent.getAction() match {
          case WIFI_P2P_STATE_CHANGED_ACTION => {}
          case WIFI_P2P_CONNECTION_CHANGED_ACTION => {
            val networkInfo: NetworkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
            if (networkInfo.isConnected()) {
              _manager.requestConnectionInfo(_channel, new ConnectionInfoListener() {
                override def onConnectionInfoAvailable(info: WifiP2pInfo): Unit = {
                  val groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

                  if (info.groupFormed && info.isGroupOwner) {
                    Toast.makeText(that, "Connected as Server", Toast.LENGTH_SHORT).show()
                  } else {
                    Toast.makeText(that, "Connected as Client", Toast.LENGTH_SHORT).show()
                  }
                  
                }
              })
            }
          }
        }
      }

    }
    val intentFilter = {
      val i = new IntentFilter();  
      import WifiP2pManager._
      List(WIFI_P2P_STATE_CHANGED_ACTION, WIFI_P2P_CONNECTION_CHANGED_ACTION) foreach {
        action => i.addAction(action) 
      }; i
    }
    registerReceiver(_broadcastReceiver, intentFilter)

    try {
      _serverSocket = new ServerSocket(0);
    } catch  {
      case e: IOException => e.printStackTrace()
    }

    mainActorRef ! MainActor.SetServerPort(String.valueOf(_serverSocket.getLocalPort()))

  }

  def onMainActorconnected(trackOption: Option[Track], playOncePrepared: Boolean, positionOncePrepared: Int): Unit = {

    _playOncePrepared = playOncePrepared
    _positionOncePrepared = positionOncePrepared
    onTrackOptionChanged(trackOption)

  }

  private def onStationOptionChanged(
    stationOption: Option[Station], serviceName: String, 
    serviceType: String, record: java.util.Map[String, String]
  ): Unit = {

    Toast.makeText(that, "onStationOptionChanged", Toast.LENGTH_SHORT).show()

    val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceType, record);
    _manager.removeGroup(_channel, null)
    stationOption match {
      case None => {
        _manager.addLocalService(_channel, serviceInfo, new WifiP2pManager.ActionListener() {
          override def onSuccess(): Unit = { 
            Toast.makeText(that, "local service added", Toast.LENGTH_SHORT).show()
          }

          override def onFailure(reason: Int): Unit = {
            Toast.makeText(that, "local service failed: " + reason, Toast.LENGTH_SHORT).show()
          }
        })
      }
      case Some(station) => {
        _manager.removeLocalService(_channel, serviceInfo, new WifiP2pManager.ActionListener() {
          override def onSuccess(): Unit = { 
            Toast.makeText(that, "removedLocalServive", Toast.LENGTH_SHORT).show()
            val config: WifiP2pConfig = { 
              val c = new WifiP2pConfig(); c.deviceAddress = station.device.deviceAddress 
              c.wps.setup = WpsInfo.PBC; c
            }
            _manager.connect(_channel, config, new WifiP2pManager.ActionListener() {
              override def onSuccess(): Unit = { 
                Toast.makeText(that, "success requesting connection", Toast.LENGTH_SHORT).show()
              }

              override def onFailure(reason: Int): Unit = {
                Toast
                  .makeText(that, "failure requesting connection: " + reason, Toast.LENGTH_SHORT)
                  .show()
              }
            })
          }

          override def onFailure(reason: Int): Unit = {
            Toast.makeText(that, "removedLocalServive Failed: " + reason, Toast.LENGTH_SHORT).show()
          }
        })
      }
    }



  }


  def onTrackOptionChanged(trackOption: Option[Track]): Unit = {

    try {
      mediaPlayer.reset()
      _prepared = false

      trackOption match {
        case Some(track) => {
          mediaPlayer.setDataSource(track.path)
          mediaPlayer.prepareAsync()
        }
        case None => {}
      }
    } catch {
      case e: IOException => e.printStackTrace()
    }

  }

  def onPlayStateChanged(playOncePrepared: Boolean): Unit = {

    _playOncePrepared = playOncePrepared
    if (_prepared) mediaPlayer.start() 


  }

  def onPositionChanged(positionOncePrepared: Int): Unit = {

    _positionOncePrepared = positionOncePrepared 
    if (_prepared) mediaPlayer.seekTo(_positionOncePrepared) 

  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = Service.START_STICKY

  override def onDestroy(): Unit =  {
    super.onDestroy()
    unregisterReceiver(_broadcastReceiver)
  }



}

