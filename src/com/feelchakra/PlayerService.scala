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

import java.net.ServerSocket
import java.net.Socket

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

import java.net.InetSocketAddress 

class PlayerService extends Service {


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

  private var _serviceInfoOp: Option[WifiP2pDnsSdServiceInfo] = None
  private var _serviceRequestOp: Option[WifiP2pDnsSdServiceRequest] = None 
  private var _isStation: Boolean = false 

  private val handler = new Handler(new Handler.Callback() {
    override def handleMessage(msg: Message): Boolean = {
      import OutputHandler._
      msg.obj match {
        case OnDiscoveringChanged(discovering: Boolean) => 
          if (discovering) discoverServices() else stopDiscovering(); true
        case OnTrackOptionChanged(trackOption) => 
          that.prepareTrack(trackOption); true
        case OnPlayStateChanged(playOncePrepared) =>
          that.setPlayOncePrepared(playOncePrepared); true
        case OnPositionChanged(positionOncePrepared) =>
          that.setPositionOncePrepared(positionOncePrepared); true
        case OnProfileChanged(localAddress, serviceName, serviceType) =>
          that.setServiceInfo(localAddress, serviceName, serviceType); true
        case OnStationOptionChanged(stationOption) =>
          that.changeStation(stationOption); true
        case OnRemoteTrackChanged(track) =>
          Toast.makeText(that, "remote track received: " + track, Toast.LENGTH_SHORT).show()
          true
        case _ => false
      }
    }
  })

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


    _manager = that.getSystemService(Context.WIFI_P2P_SERVICE) match {
      case m: WifiP2pManager => m
    }
    _channel = _manager.initialize(that, that.getMainLooper(), null)

    _broadcastReceiver = new BroadcastReceiver() {
      
      override def onReceive(context: Context, intent: Intent): Unit = {
        import WifiP2pManager._

        intent.getAction() match {
          case WIFI_P2P_STATE_CHANGED_ACTION => {
           //Toast.makeText(that, "state changed", Toast.LENGTH_SHORT).show()
          }

          case WIFI_P2P_PEERS_CHANGED_ACTION => {
           //Toast.makeText(that, "peers changed", Toast.LENGTH_SHORT).show()
          }

          case WIFI_P2P_THIS_DEVICE_CHANGED_ACTION => {
           //Toast.makeText(that, "this device changed", Toast.LENGTH_SHORT).show()
          }

          case WIFI_P2P_PEERS_CHANGED_ACTION => {
          // Toast.makeText(that, "peers changed", Toast.LENGTH_SHORT).show()
          }

          case WIFI_P2P_CONNECTION_CHANGED_ACTION => {
            val networkInfo: NetworkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO)
            if (networkInfo.isConnected()) {
              _manager.requestConnectionInfo(_channel, new ConnectionInfoListener() {
                override def onConnectionInfoAvailable(info: WifiP2pInfo): Unit = {
                  if (info.groupFormed) {
                    if (info.isGroupOwner) {
                      Toast.makeText(that, "X Connected as Server", Toast.LENGTH_SHORT).show()
                      //mainActorRef ! MainActor.StartServer 
                    } else {
                      Toast.makeText(that, "X Connected as Client", Toast.LENGTH_SHORT).show()
                      //val remoteHost = info.groupOwnerAddress.getHostAddress()
                      //mainActorRef ! MainActor.StartClient(remoteHost)
                    }
                  } 
                  
                }
              })
            } else {
              Toast.makeText(that, "network disconnected", Toast.LENGTH_SHORT).show()
            }
          }

        }
      }

    }
    val intentFilter = {
      val i = new IntentFilter();  
      import WifiP2pManager._
      List(WIFI_P2P_STATE_CHANGED_ACTION, 
        WIFI_P2P_PEERS_CHANGED_ACTION, 
        WIFI_P2P_THIS_DEVICE_CHANGED_ACTION, 
        WIFI_P2P_CONNECTION_CHANGED_ACTION) foreach {
        action => i.addAction(action) 
      }; i
    }
    registerReceiver(_broadcastReceiver, intentFilter)

    mainActorRef ! MainActor.Subscribe(this.toString, handler)

  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = Service.START_STICKY

  override def onDestroy(): Unit =  {
    super.onDestroy()
    stopDiscovering()
    removeLegacyConnection()
    unregisterReceiver(_broadcastReceiver)
    mainActorRef ! MainActor.Unsubscribe(this.toString)
  }

  import WifiP2pManager._

  private def stopDiscovering(): Unit = {

    Toast.makeText(that, "stopping discovery", Toast.LENGTH_SHORT).show()

    _serviceRequestOp match {
      case None => {}
      case Some(serviceRequest) =>
        _manager.removeServiceRequest(_channel, serviceRequest, new ActionListener() {
          override def onSuccess(): Unit = {
          }
          override def onFailure(code: Int): Unit = {
          }
        })
        _serviceRequestOp = None

    }
  }

  private def discoverServices(): Unit = {

    Toast.makeText(that, "starting discovery", Toast.LENGTH_SHORT).show()

    val serviceListener = new DnsSdServiceResponseListener() {
      override def onDnsSdServiceAvailable(name: String, 
        regType: String, device: WifiP2pDevice): Unit = {
        mainActorRef ! MainActor.CommitStation(device)
      }
    }

    val recordListener = new DnsSdTxtRecordListener() {
      override def onDnsSdTxtRecordAvailable(domain: String, record: java.util.Map[String, String], 
        device: WifiP2pDevice
      ): Unit = {
        val station = Station(domain, record, device)
        mainActorRef ! MainActor.AddStation(station)
      }
    }

    _manager.setDnsSdResponseListeners(_channel, serviceListener, recordListener)

    val serviceRequest = WifiP2pDnsSdServiceRequest.newInstance()
    _manager.addServiceRequest(_channel, serviceRequest, new ActionListener() {
      override def onSuccess(): Unit = {
        //Toast.makeText(that, "serviceRequest Success", Toast.LENGTH_SHORT).show()
      }
      override def onFailure(code: Int): Unit = {
        Toast.makeText(that, "serviceRequest Failed: " + code, Toast.LENGTH_SHORT).show()
      }
    })

    _serviceRequestOp = Some(serviceRequest)

    _manager.discoverServices(_channel, new ActionListener() {
      override def onSuccess(): Unit = {
        //Toast.makeText(that, "discover Success", Toast.LENGTH_SHORT).show()
      }
      override def onFailure(code: Int): Unit = {
        Toast.makeText(that, "discover Failed: " + code, Toast.LENGTH_SHORT).show()
      }
    })


  }

  private def setServiceInfo(localAddress: InetSocketAddress, serviceName: String, serviceType: String): Unit = {

    val record = new java.util.HashMap[String, String]()
    record.put("port", localAddress.getPort().toString)

    val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceName, serviceType, record)
    Toast.makeText(that, "isStation: " + _isStation, Toast.LENGTH_SHORT).show()
    if (_isStation) { 
      _serviceInfoOp match {
        case None =>
          //if this device should be the station but the service info wasn't available before
          //then become the station now that it is available
          becomeTheStation(serviceInfo)
        case Some(oldServiceInfo) =>
          //if there is an old serviceInfo then this device already became the station
          //so just readvertise with the new serviceInfo 
          readvertise(oldServiceInfo, serviceInfo)
      }
    }
    _serviceInfoOp = Some(serviceInfo)

  }

  private def tryBecomingTheStation(): Unit = {
    _isStation = true
    _serviceInfoOp match {
      case Some(serviceInfo) => becomeTheStation(serviceInfo)
      case None => {}
    }
  }


  private def readvertise(oldServiceInfo: WifiP2pDnsSdServiceInfo, 
    newServiceInfo: WifiP2pDnsSdServiceInfo): Unit = {
    _manager.removeLocalService(_channel, oldServiceInfo, null)
    _manager.addLocalService(_channel, newServiceInfo, null)
  }



  private def removeLegacyConnection(): Unit = {

    if (_isStation) { 
      _isStation = false
      _serviceInfoOp match {
        case Some(oldServiceInfo) => 
          _manager.removeLocalService(_channel, oldServiceInfo, null)
        case None => {} 
      }
    } else {
      _manager.cancelConnect(_channel, new WifiP2pManager.ActionListener() {
        override def onSuccess(): Unit = { 
        }
        override def onFailure(reason: Int): Unit = {
          Toast.makeText(that, "failed canceling connect: " + reason, Toast.LENGTH_SHORT).show()
        }
      }) 
    }

    _manager.removeGroup(_channel, new WifiP2pManager.ActionListener() {
      override def onSuccess(): Unit = { 
        //Toast.makeText(that, "server group removed", Toast.LENGTH_SHORT).show()
      }
      override def onFailure(reason: Int): Unit = {
        Toast.makeText(that, "failed removing group: " + reason, Toast.LENGTH_SHORT).show()
      }
    }) 

  }

  private def changeStation(stationOption: Option[Station]): Unit = {

    removeLegacyConnection()

    stationOption match {
      case Some(station) => tuneIntoStation(station)
      case None => tryBecomingTheStation() 
    }

  }

  private def becomeTheStation(serviceInfo: WifiP2pDnsSdServiceInfo): Unit = {

    _manager.addLocalService(_channel, serviceInfo, new WifiP2pManager.ActionListener() {
      override def onSuccess(): Unit = { 
        _manager.createGroup(_channel, new WifiP2pManager.ActionListener() {
          override def onSuccess(): Unit = { 
            Toast.makeText(that, "creating group", Toast.LENGTH_SHORT).show()
          }
          override def onFailure(reason: Int): Unit = {
            Toast.makeText(that, "failed creating group", Toast.LENGTH_SHORT).show()
          }
        })
      }
      override def onFailure(reason: Int): Unit = {
        Toast.makeText(that, "failed advertising", Toast.LENGTH_SHORT).show()
      }
    })
  }


  private def tuneIntoStation(station: Station): Unit = {
    
    val config: WifiP2pConfig = { 
      val c = new WifiP2pConfig(); c.deviceAddress = station.device.deviceAddress 
      c.wps.setup = WpsInfo.PBC; c
    }
    _manager.connect(_channel, config, new WifiP2pManager.ActionListener() {
      override def onSuccess(): Unit = { 
        Toast.makeText(that, "requesting connection", Toast.LENGTH_SHORT).show()
      }
      override def onFailure(reason: Int): Unit = {
        Toast.makeText(that, "failed requesting connection: " + reason, Toast.LENGTH_SHORT).show()
      }
    })

  }


  def prepareTrack(trackOption: Option[Track]): Unit = {

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

  def setPlayOncePrepared(playOncePrepared: Boolean): Unit = {

    _playOncePrepared = playOncePrepared
    if (_prepared) mediaPlayer.start() 

  }

  def setPositionOncePrepared(positionOncePrepared: Int): Unit = {

    _positionOncePrepared = positionOncePrepared 
    if (_prepared) mediaPlayer.seekTo(_positionOncePrepared) 

  }





}

