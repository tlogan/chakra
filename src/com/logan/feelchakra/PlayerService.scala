package com.logan.feelchakra

import android.widget.Toast
import android.util.Log
import RichMediaPlayer.mediaPlayer2RichMediaPlayer 

class PlayerService extends Service {

  private val that = this

  private var _mediaPlayer: MediaPlayer = _ 

  private var _playing: Boolean = false 
  private var _startPos: Int = 0
  private var _lastSeek: Int = 0
  private var _prepared: Boolean = false

  private var _manager: WifiP2pManager = _
  private var _channel: WifiChannel = _
  private var _broadcastReceiver: BroadcastReceiver = _
  private var _serverSocket: ServerSocket = _

  private var _serviceInfoOp: Option[WifiP2pDnsSdServiceInfo] = None
  private var _serviceRequestOp: Option[WifiP2pDnsSdServiceRequest] = None 
  private var _localServiceAdded: Boolean = false 
  private var _groupFormed: Boolean = false 

  private var _playState: PlayState = NotPlaying

  private var _stationConnection: StationConnection = StationDisconnected

  private def seek(): Unit = {
    if (_startPos != _lastSeek) {
      _mediaPlayer.seekTo(_startPos)
      _lastSeek = _startPos
    }
  }

  private def playOrPause(): Unit = {


     if (_playing != _mediaPlayer.isPlaying()) {
       if (_playing) { 

         //wrap the starting of the player and time query in a delayed block
         //to keep the station synchronized with the listeners
         //after a quick succession of pause then play user requests.
         //perhaps the mediaplayer becomes backedup so that the starting of the player
         //doesn't happen as quickly as the sending of the message to the listeners.
         //the time delay helps to give the mediaplayer time to catch up.
         handler.postDelayed(new Runnable() { 
           def run() = {

             val pos = _mediaPlayer.getCurrentPosition()
             //the following seekTo call to the current position 
             //keeps the local player synchronized with the listeners' players
             //each of which calls seekTo before starting.
             //perhaps the position after seeking to the current position is different
             //because the queried position is rounded.
             _mediaPlayer.seekTo(pos)
             _mediaPlayer.start() 
             val time = Platform.currentTime
             mainActorRef ! MainActor.WriteListenerPlayState(Playing(pos, time))
           }
         }, 500)

       } else {
         _mediaPlayer.pause()
         mainActorRef ! MainActor.WriteListenerPlayState(NotPlaying)
       }
     }

  }

  private def reset(): Unit = {
    _mediaPlayer.reset()
    _prepared = false
  }

  private def adjustMediaPlayer(): Unit = {
    _playState match {
      case NotPlaying => 
        _mediaPlayer.pause()
      case Playing(startPos, startTime) => 
        _mediaPlayer.seekTo((startPos + Platform.currentTime - startTime).toInt)
        _mediaPlayer.start() 
    }
  }

  private val handler = new Handler(new HandlerCallback() {
    override def handleMessage(msg: Message): Boolean = {
      import UI._
      msg.obj match {
        case OnDiscoveringChanged(discovering: Boolean) => 
          if (discovering) discoverServices() else stopDiscovering()
          true
        case OnProfileChanged(networkProfile) =>
          that.setServiceInfo(networkProfile)
          true
        case OnStationConnectionChanged(stationConnection) =>
          _stationConnection = stationConnection
          that.changeStation(stationConnection)
          true
        case OnPresentTrackOptionChanged(presentTrackOp) if _stationConnection == StationDisconnected =>
          Log.d("chakra", "OnLocalTrackChanged")
          reset()
          presentTrackOp match {
            case Some(track) =>
              _mediaPlayer.setDataSource(track.path)
              _mediaPlayer.prepareAsync()
            case None => 
          }
          true
        case OnLocalPlayingChanged(playing) if _stationConnection == StationDisconnected =>
          Log.d("chakra", "OnLocalPlayingChanged: " + playing)
          _playing = playing
          if (_prepared) {
            playOrPause()
          }
          true
        case OnLocalStartPosChanged(startPos) if _stationConnection == StationDisconnected =>
          Log.d("chakra", "OnLocalStartPosChanged: " + startPos)
          _startPos = startPos 
          if (_prepared) { seek() }
          true
        case OnStationTrackOpChanged(trackOp) =>
          reset()
          trackOp match {
            case Some(track) =>
              _mediaPlayer.setDataSource(track.path)
              _mediaPlayer.prepareAsync()
            case None => {}
          }
          true
        case OnStationPlayStateChanged(playState) =>
          Log.d("chakra", "OnStationPlayState: " + playState)
          _playState = playState
          if (_prepared) {
            adjustMediaPlayer()
          }
          true
        case _ => false
      }
    }
  })

  override def onBind(intent: Intent): IBinder = {
    return null;
  }

  override def onCreate(): Unit = {

    _mediaPlayer = new MediaPlayer()
    _manager = that.getSystemService(WIFI_P2P_SERVICE) match {
      case m: WifiP2pManager => m
    }
    _channel = _manager.initialize(that, that.getMainLooper(), null)

    _broadcastReceiver = new BroadcastReceiver() {

      override def onReceive(context: Context, intent: Intent): Unit = {

        intent.getAction() match {

          case WIFI_P2P_STATE_CHANGED_ACTION => {}
          case WIFI_P2P_PEERS_CHANGED_ACTION => {}
          case WIFI_P2P_THIS_DEVICE_CHANGED_ACTION => {}

          case WIFI_P2P_CONNECTION_CHANGED_ACTION => {
            val networkInfo: NetworkInfo = intent.getParcelableExtra(EXTRA_NETWORK_INFO)
            if (networkInfo.isConnected()) {
              _manager.requestConnectionInfo(_channel, new ConnectionInfoListener() {
                override def onConnectionInfoAvailable(info: WifiP2pInfo): Unit = {

                  _groupFormed = info.groupFormed
                  if (info.groupFormed && !info.isGroupOwner) {
                    Log.d("chakra", "Connected as Client")
                    val remoteHost = info.groupOwnerAddress.getHostAddress()
                    mainActorRef ! MainActor.ConnectStation(remoteHost)
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
    mainActorRef ! MainActor.AcceptListeners 

  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = START_STICKY

  override def onDestroy(): Unit =  {
    super.onDestroy()
    _mediaPlayer.release()
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
          }
          override def onFailure(code: Int): Unit = {
          }
        })
        _serviceRequestOp = None

    }
  }

  private def discoverServices(): Unit = {

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
        Log.d("chakra", "domain: " + domain)
        val station = Station(domain, record, device)
        mainActorRef ! MainActor.AddStation(station)
      }
    }

    _manager.setDnsSdResponseListeners(_channel, serviceListener, recordListener)

    val serviceRequest = newServiceRequest()
    _manager.addServiceRequest(_channel, serviceRequest, new WifiActionListener() {
      override def onSuccess(): Unit = {
      }
      override def onFailure(code: Int): Unit = {
        Log.d("chakra", "serviceRequest Failed: " + code)
      }
    })

    _serviceRequestOp = Some(serviceRequest)

    _manager.discoverServices(_channel, new WifiActionListener() {
      override def onSuccess(): Unit = {
      }
      override def onFailure(code: Int): Unit = {
        Log.d("chakra", "discover Failed: " + code)
      }
    })


  }

  private def setServiceInfo(networkProfile: NetworkProfile): Unit = {

    networkProfile.localAddressOp match {
      case Some(localAddress) =>
        val serviceName = networkProfile.serviceName
        val serviceType = networkProfile.serviceType
        val record = new java.util.HashMap[String, String]()
        record.put("port", localAddress.getPort().toString)

        val serviceInfo = newServiceInfo(serviceName, serviceType, record)
        if (_localServiceAdded) { 
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
      case None =>
    }


  }

  private def tryBecomingTheStation(): Unit = {
    _localServiceAdded = true
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
          Log.d("chakra", "removing group ")
        }
        override def onFailure(reason: Int): Unit = {
          Log.d("chakra", "failed removing group: " + reason)
        }
      }) 

      if (_localServiceAdded) { 
        _serviceInfoOp match {
          case Some(oldServiceInfo) => 
            _manager.removeLocalService(_channel, oldServiceInfo, null)
          case None => {} 
        }
        _localServiceAdded = false
      } 

    } else {
    }

  }

  private var count = 0

  private def changeStation(stationConnection: StationConnection): Unit = {

    stationConnection match {
      case StationDisconnected => 
        _mediaPlayer.setOnCompletion(mp => {
          count = count + 1
          mainActorRef ! MainActor.SetPresentTrackToNext
        })
        removeLegacyConnection()
        tryBecomingTheStation() 
      case StationRequested(station) => 
        _mediaPlayer.setOnCompletion(mp => {})
        removeLegacyConnection()
        tuneIntoStation(station)
      case _ =>
    }

  }

  private def becomeTheStation(serviceInfo: WifiP2pDnsSdServiceInfo): Unit = {
     Log.d("chakra", "Becoming the Station")

    _mediaPlayer.setOnPrepared(mp => {
      _prepared = true
      if (_playing && !_mediaPlayer.isPlaying()) { 
        seek()
        playOrPause()
      }
    })

    _manager.addLocalService(_channel, serviceInfo, new WifiActionListener() {
      override def onSuccess(): Unit = { 
        mainActorRef ! MainActor.Discover
        Log.d("chakra", "Discover")
      }
      override def onFailure(reason: Int): Unit = {
        Log.d("chakra", "failed advertising" + reason)
      }
    })

  }


  private def tuneIntoStation(station: Station): Unit = {
    _mediaPlayer.setOnPrepared(mp => {
      _prepared = true
      adjustMediaPlayer()
    })


    val config: WifiP2pConfig = { 
      val c = new WifiP2pConfig() 
      c.deviceAddress = station.device.deviceAddress 
      c.groupOwnerIntent = 0 
      c.wps.setup = WpsInfoPBC; c
    }
    _manager.connect(_channel, config, new WifiActionListener() {
      override def onSuccess(): Unit = { 
        Log.d("chakra", "requesting connection")
      }
      override def onFailure(reason: Int): Unit = {
        Log.d("chakra", "failed requesting connection: " + reason)
      }
    })

  }

}

