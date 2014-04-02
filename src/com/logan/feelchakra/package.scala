package com.logan {
  package object feelchakra {

    type InetSocketAddress = java.net.InetSocketAddress 

    type Future[+T] = scala.concurrent.Future[T]
    val Promise = scala.concurrent.Promise
    type Promise[T] = scala.concurrent.Promise[T]

    type Database = guava.scalaandroid.Database
    val RichListView = guava.scalaandroid.RichListView
    type RichListView = guava.scalaandroid.RichListView

    val RichView = guava.scalaandroid.RichView
    type RichView = guava.scalaandroid.RichView

    val RichMediaPlayer = guava.scalaandroid.RichMediaPlayer
    type RichMediaPlayer = guava.scalaandroid.RichMediaPlayer

    type LayoutInflater = android.view.LayoutInflater
    type Menu = android.view.Menu
    type MenuItem = android.view.MenuItem
    type Fragment = android.app.Fragment
    type Service = android.app.Service
    val START_STICKY = android.app.Service.START_STICKY
    type View = android.view.View
    val GONE = android.view.View.GONE
    type TextView = android.widget.TextView
    type FrameLayout = android.widget.FrameLayout
    type ListView = android.widget.ListView
    type AdapterView[T <: android.widget.Adapter] = android.widget.AdapterView[T]
    type AdapterViewClick = android.widget.AdapterView.OnItemClickListener
    type ViewClick = android.view.View.OnClickListener
    type BaseAdapter = android.widget.BaseAdapter
    type LinearLayout = android.widget.LinearLayout
    type LLLayoutParams = android.widget.LinearLayout.LayoutParams
    val MATCH_PARENT = android.view.ViewGroup.LayoutParams.MATCH_PARENT
    val VERTICAL =  android.widget.LinearLayout.VERTICAL
    type Handler = android.os.Handler
    type HandlerCallback = android.os.Handler.Callback
    type Bundle = android.os.Bundle


    type ListBuffer[A] = scala.collection.mutable.ListBuffer[A]
    val HashMap = scala.collection.immutable.HashMap
    type HashMap[A, B] = scala.collection.immutable.HashMap[A, B]
    type List[A] = scala.collection.immutable.List[A]

    type IOException = java.io.IOException
    type ServerSocket = java.net.ServerSocket
    type Socket = java.net.Socket

    val Success = scala.util.Success
    type Success[+T] = scala.util.Success[T]
    val Failure = scala.util.Failure
    type Failure[+T] = scala.util.Failure[T]

    val JavaConverters = scala.collection.JavaConverters

    type Activity = android.app.Activity
    type Actionbar = android.app.ActionBar
    val NAVIGATION_MODE_TABS = android.app.ActionBar.NAVIGATION_MODE_TABS

    type TabListener = android.app.ActionBar.TabListener
    type Tab = android.app.ActionBar.Tab

    type FragmentTransaction = android.app.FragmentTransaction
    type Intent = android.content.Intent
    type Message = android.os.Message


    type ViewGroup = android.view.ViewGroup 
    type MediaPlayer = android.media.MediaPlayer
    type IBinder = android.os.IBinder
    type BroadcastReceiver = android.content.BroadcastReceiver
    type Context = android.content.Context
    val WIFI_P2P_SERVICE = android.content.Context.WIFI_P2P_SERVICE
    type IntentFilter = android.content.IntentFilter
    type NetworkInfo = android.net.NetworkInfo
    val WpsInfoPBC = android.net.wifi.WpsInfo.PBC
    type WifiP2pConfig = android.net.wifi.p2p.WifiP2pConfig
    type WifiP2pDevice = android.net.wifi.p2p.WifiP2pDevice
    type WifiP2pInfo = android.net.wifi.p2p.WifiP2pInfo
    type WifiP2pManager = android.net.wifi.p2p.WifiP2pManager
    type ConnectionInfoListener = android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
    type WifiChannel = android.net.wifi.p2p.WifiP2pManager.Channel
    val EXTRA_NETWORK_INFO = android.net.wifi.p2p.WifiP2pManager.EXTRA_NETWORK_INFO
    val WIFI_P2P_STATE_CHANGED_ACTION = 
      android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION
    val WIFI_P2P_PEERS_CHANGED_ACTION = 
      android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
    val WIFI_P2P_THIS_DEVICE_CHANGED_ACTION = 
      android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
    val WIFI_P2P_CONNECTION_CHANGED_ACTION = 
      android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION 

    type WifiActionListener = android.net.wifi.p2p.WifiP2pManager.ActionListener
    type WifiP2pDnsSdServiceInfo = android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
    val newServiceInfo = android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo.newInstance _
    type WifiP2pDnsSdServiceRequest = android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
    val newServiceRequest = android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest.newInstance _

    type DnsSdServiceResponseListener = 
      android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener
    type DnsSdTxtRecordListener = 
      android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener

    type Color = android.graphics.Color
    val YELLOW = android.graphics.Color.YELLOW
    val DKGRAY = android.graphics.Color.DKGRAY
    val GRAY = android.graphics.Color.GRAY
    val WHITE = android.graphics.Color.WHITE
    type MediaStore = android.provider.MediaStore 
    val AUDIO_URI = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val DATA = android.provider.MediaStore.MediaColumns.DATA
    val TITLE = android.provider.MediaStore.MediaColumns.TITLE
    val ALBUM = android.provider.MediaStore.Audio.AudioColumns.ALBUM
    val ARTIST = android.provider.MediaStore.Audio.AudioColumns.ARTIST


    type Table = guava.scalaandroid.Table

    type ActorRef = akka.actor.ActorRef
    type Actor = akka.actor.Actor
    val Props = akka.actor.Props
    type Props = akka.actor.Props
    val ActorSystem = akka.actor.ActorSystem
    type ActorSystem = akka.actor.ActorSystem
    val IO = akka.io.IO
    val Tcp = akka.io.Tcp
    val ByteString = akka.util.ByteString
    type ByteString = akka.util.ByteString

    type ReplaySubject[T] = rx.lang.scala.subjects.ReplaySubject[T]
    type Observer[-T] = rx.lang.scala.Observer[T]

  }
}
