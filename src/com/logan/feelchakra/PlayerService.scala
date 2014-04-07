package com.logan.feelchakra

import RichMediaPlayer.mediaPlayer2RichMediaPlayer
import android.widget.Toast
import android.util.Log

class PlayerService extends Service {

  private val that = this
  private val mediaPlayer = new MediaPlayer()

  private var _playOncePrepared: Boolean = false
  private var _positionOncePrepared: Int = 0
  private var _prepared: Boolean = false

  private var _manager: WifiP2pManager = _
  private var _channel: WifiChannel = _
  private var _broadcastReceiver: BroadcastReceiver = _
  private var _serverSocket: ServerSocket = _

  private var _serviceInfoOp: Option[WifiP2pDnsSdServiceInfo] = None
  private var _serviceRequestOp: Option[WifiP2pDnsSdServiceRequest] = None 
  private var _isStation: Boolean = false 
  private var _groupFormed: Boolean = false 

  private val handler = new Handler(new HandlerCallback() {
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
          Log.d("chakra", "remote track received: " + track)
          Toast.makeText(that, "remote track received: " + track.path, Toast.LENGTH_SHORT).show()
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


    _manager = that.getSystemService(WIFI_P2P_SERVICE) match {
      case m: WifiP2pManager => m
    }
    _channel = _manager.initialize(that, that.getMainLooper(), null)

    _broadcastReceiver = new BroadcastReceiver() {

      override def onReceive(context: Context, intent: Intent): Unit = {

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
            val networkInfo: NetworkInfo = intent.getParcelableExtra(EXTRA_NETWORK_INFO)
            if (networkInfo.isConnected()) {
              _manager.requestConnectionInfo(_channel, new ConnectionInfoListener() {
                override def onConnectionInfoAvailable(info: WifiP2pInfo): Unit = {
                  if (info.groupFormed) {

                    _groupFormed = true

                    if (info.isGroupOwner) {
                      Toast.makeText(that, "X Connected as Server", Toast.LENGTH_SHORT).show()
                      Log.d("chakra", "X Connected as Server")
                    } else {
                      Toast.makeText(that, "X Connected as Client", Toast.LENGTH_SHORT).show()
                      Log.d("chakra", "X Connected as Client")
                      val remoteHost = info.groupOwnerAddress.getHostAddress()
                      mainActorRef ! MainActor.ConnectRemote(remoteHost)
                    }
                  } else {
                    _groupFormed = false 
                  }
                  
                }
              })
            } else {
              _groupFormed = false 
            }
          }

        }
      }

    }
    val intentFilter = {
      val i = new IntentFilter();  
      List(WIFI_P2P_STATE_CHANGED_ACTION, 
        WIFI_P2P_PEERS_CHANGED_ACTION, 
        WIFI_P2P_THIS_DEVICE_CHANGED_ACTION, 
        WIFI_P2P_CONNECTION_CHANGED_ACTION) foreach {
        action => i.addAction(action) 
      }; i
    }
    registerReceiver(_broadcastReceiver, intentFilter)

    mainActorRef ! MainActor.Subscribe(this.toString, handler)

    mainActorRef ! MainActor.AcceptRemotes 

  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = START_STICKY

  override def onDestroy(): Unit =  {
    super.onDestroy()
    stopDiscovering()
    removeLegacyConnection()
    unregisterReceiver(_broadcastReceiver)
    mainActorRef ! MainActor.Unsubscribe(this.toString)
  }

  private def stopDiscovering(): Unit = {

    _serviceRequestOp match {
      case None => {}
      case Some(serviceRequest) =>
        _manager.removeServiceRequest(_channel, serviceRequest, new WifiActionListener() {
          override def onSuccess(): Unit = {
            Toast.makeText(that, "stopping discovery", Toast.LENGTH_SHORT).show()
          }
          override def onFailure(code: Int): Unit = {
            Toast.makeText(that, "failed stopping discovery", Toast.LENGTH_SHORT).show()
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

    val serviceRequest = newServiceRequest()
    _manager.addServiceRequest(_channel, serviceRequest, new WifiActionListener() {
      override def onSuccess(): Unit = {
        //Toast.makeText(that, "serviceRequest Success", Toast.LENGTH_SHORT).show()
      }
      override def onFailure(code: Int): Unit = {
        Toast.makeText(that, "serviceRequest Failed: " + code, Toast.LENGTH_SHORT).show()
        Log.d("chakra", "serviceRequest Failed: " + code)
      }
    })

    _serviceRequestOp = Some(serviceRequest)

    _manager.discoverServices(_channel, new WifiActionListener() {
      override def onSuccess(): Unit = {
        //Toast.makeText(that, "discover Success", Toast.LENGTH_SHORT).show()
      }
      override def onFailure(code: Int): Unit = {
        Toast.makeText(that, "discover Failed: " + code, Toast.LENGTH_SHORT).show()
        Log.d("chakra", "discover Failed: " + code)
      }
    })


  }

  private def setServiceInfo(localAddress: InetSocketAddress, serviceName: String, serviceType: String): Unit = {

    val record = new java.util.HashMap[String, String]()
    record.put("port", localAddress.getPort().toString)

    val serviceInfo = newServiceInfo(serviceName, serviceType, record)
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

    if (_groupFormed) {
      _manager.removeGroup(_channel, new WifiActionListener() {
        override def onSuccess(): Unit = { 
          Toast.makeText(that, "legacy group removed", Toast.LENGTH_SHORT).show()
        }
        override def onFailure(reason: Int): Unit = {
          Toast.makeText(that, "failed removing group: " + reason, Toast.LENGTH_SHORT).show()
          Log.d("chakra", "failed removing group: " + reason)
        }
      }) 

      if (_isStation) { 
        _serviceInfoOp match {
          case Some(oldServiceInfo) => 
            _manager.removeLocalService(_channel, oldServiceInfo, null)
          case None => {} 
        }
        _isStation = false
      } 

      /*

      if (false) {
        _manager.cancelConnect(_channel, new WifiActionListener() {
          override def onSuccess(): Unit = { 
          }
          override def onFailure(reason: Int): Unit = {
            Toast.makeText(that, "failed canceling connect: " + reason, Toast.LENGTH_SHORT).show()
            Log.d("chakra", "failed canceling connect: " + reason)
          }
        }) 
      }
      */

    } else {
      Toast.makeText(that, "nothing removed", Toast.LENGTH_SHORT).show()
    }

  }

  private def changeStation(stationOption: Option[Station]): Unit = {

    removeLegacyConnection()

    stationOption match {
      case Some(station) => tuneIntoStation(station)
      case None => tryBecomingTheStation() 
    }

  }

  private def becomeTheStation(serviceInfo: WifiP2pDnsSdServiceInfo): Unit = {

    _manager.addLocalService(_channel, serviceInfo, new WifiActionListener() {
      override def onSuccess(): Unit = { 
        Toast.makeText(that, "advertising", Toast.LENGTH_SHORT).show()
      }
      override def onFailure(reason: Int): Unit = {
        Toast.makeText(that, "failed advertising", Toast.LENGTH_SHORT).show()
        Log.d("chakra", "failed advertising" + reason)
      }
    })
  }


  private def tuneIntoStation(station: Station): Unit = {

    val config: WifiP2pConfig = { 
      val c = new WifiP2pConfig() 
      c.deviceAddress = station.device.deviceAddress 
      c.groupOwnerIntent = 0
      c.wps.setup = WpsInfoPBC; c
    }
    _manager.connect(_channel, config, new WifiActionListener() {
      override def onSuccess(): Unit = { 
        Toast.makeText(that, "requesting connection", Toast.LENGTH_SHORT).show()
        Log.d("chakra", "requesting connection")
      }
      override def onFailure(reason: Int): Unit = {
        Toast.makeText(that, "failed requesting connection: " + reason, Toast.LENGTH_SHORT).show()
        Log.d("chakra", "failed requesting connection: " + reason)
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

