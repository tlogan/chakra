package com.logan {
  package object feelchakra {

    type FileOutputStream = java.io.FileOutputStream
    type DataOutputStream = java.io.DataOutputStream
    type DataInputStream = java.io.DataInputStream
    type File = java.io.File

    type FileInputStream = java.io.FileInputStream
    type BufferedInputStream = java.io.BufferedInputStream
    type BufferedOutputStream = java.io.BufferedOutputStream

    type InputStream = java.io.InputStream
    type OutputStream = java.io.OutputStream

    type InetAddress = java.net.InetAddress
    type ServerSocket = java.net.ServerSocket
    type Socket = java.net.Socket

    type InetSocketAddress = java.net.InetSocketAddress 

    val Platform = scala.compat.Platform

    val Future = scala.concurrent.Future
    type Future[+T] = scala.concurrent.Future[T]
    val Promise = scala.concurrent.Promise
    type Promise[T] = scala.concurrent.Promise[T]

    type Database = guava.scalaandroid.Database
    val RichListView = guava.scalaandroid.RichListView
    type RichListView = guava.scalaandroid.RichListView

    val RichContext = guava.scalaandroid.RichContext
    type RichContext = guava.scalaandroid.RichContext

    val RichMenuItem = guava.scalaandroid.RichMenuItem
    type RichMenuItem = guava.scalaandroid.RichMenuItem


    val RichView = guava.scalaandroid.RichView
    type RichView = guava.scalaandroid.RichView

    val RichMediaPlayer = guava.scalaandroid.RichMediaPlayer
    type RichMediaPlayer = guava.scalaandroid.RichMediaPlayer

    type Animator = android.animation.Animator
    type AnimatorListenerAdapter = android.animation.AnimatorListenerAdapter

    type SimpleOnGestureListener = android.view.GestureDetector.SimpleOnGestureListener
    type GestureDetector = android.view.GestureDetector

    val ACTION_CANCEL = android.view.MotionEvent.ACTION_CANCEL
    val ACTION_DOWN = android.view.MotionEvent.ACTION_DOWN
    val ACTION_UP = android.view.MotionEvent.ACTION_UP
    val ACTION_MOVE = android.view.MotionEvent.ACTION_MOVE
    type MotionEvent = android.view.MotionEvent

    type LayoutInflater = android.view.LayoutInflater
    type Menu = android.view.Menu
    type MenuItem = android.view.MenuItem
    val SHOW_AS_ACTION_ALWAYS =  android.view.MenuItem.SHOW_AS_ACTION_ALWAYS

    type Fragment = android.app.Fragment
    type Service = android.app.Service
    val START_STICKY = android.app.Service.START_STICKY
    type View = android.view.View
    val GONE = android.view.View.GONE
    val VISIBLE = android.view.View.VISIBLE

    type ImageView = android.widget.ImageView

    type TextView = android.widget.TextView
    type FrameLayout = android.widget.FrameLayout
    type ListView = android.widget.ListView
    type AdapterView[T <: android.widget.Adapter] = android.widget.AdapterView[T]
    type AdapterViewClick = android.widget.AdapterView.OnItemClickListener
    type ViewClick = android.view.View.OnClickListener
    type BaseAdapter = android.widget.BaseAdapter
    type ListAdapter = android.widget.ListAdapter

    val GTOP = android.view.Gravity.TOP
    type RelativeLayout = android.widget.RelativeLayout
    val RIGHT_OF = android.widget.RelativeLayout.RIGHT_OF
    val LEFT_OF = android.widget.RelativeLayout.LEFT_OF

    type LinearLayout = android.widget.LinearLayout
    type LLLayoutParams = android.widget.LinearLayout.LayoutParams
    type RLLayoutParams = android.widget.RelativeLayout.LayoutParams
    type VGLayoutParams = android.view.ViewGroup.LayoutParams
    type LVLayoutParams = android.widget.AbsListView.LayoutParams
    val MATCH_PARENT = android.view.ViewGroup.LayoutParams.MATCH_PARENT
    val WRAP_CONTENT = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
    val VERTICAL =  android.widget.LinearLayout.VERTICAL
    val HORIZONTAL =  android.widget.LinearLayout.HORIZONTAL
    type Handler = android.os.Handler
    type HandlerCallback = android.os.Handler.Callback
    type Bundle = android.os.Bundle


    type Stream[+A] = scala.collection.immutable.Stream[A]

    type ListBuffer[A] = scala.collection.mutable.ListBuffer[A]

    val HashMap = scala.collection.immutable.HashMap
    type HashMap[A, B] = scala.collection.immutable.HashMap[A, B]
    val TreeMap = scala.collection.immutable.TreeMap
    type TreeMap[A, +B] = scala.collection.immutable.TreeMap[A, B]

    val TreeSet = scala.collection.immutable.TreeSet
    type TreeSet[A] = scala.collection.immutable.TreeSet[A]

    type AlbumMap = TreeMap[Album, List[Track]]
    type ArtistMap = TreeMap[String, AlbumMap]

    type List[A] = scala.collection.immutable.List[A]

    type IOException = java.io.IOException
    type EOFException = java.io.EOFException

    val Success = scala.util.Success
    type Success[+T] = scala.util.Success[T]
    val Failure = scala.util.Failure
    type Failure[+T] = scala.util.Failure[T]

    val JavaConverters = scala.collection.JavaConverters

    type Activity = android.app.Activity
    type Actionbar = android.app.ActionBar
    val NAVIGATION_MODE_TABS = android.app.ActionBar.NAVIGATION_MODE_TABS
    val NAVIGATION_MODE_STANDARD = android.app.ActionBar.NAVIGATION_MODE_STANDARD

    type TabListener = android.app.ActionBar.TabListener
    type Tab = android.app.ActionBar.Tab

    type FragmentTransaction = android.app.FragmentTransaction
    type Intent = android.content.Intent
    type Message = android.os.Message


    type ViewGroup = android.view.ViewGroup 
    type MediaPlayer = android.media.MediaPlayer
    type AudioTrack = android.media.AudioTrack
    val MODE_STREAM = android.media.AudioTrack.MODE_STREAM
    val MODE_STATIC = android.media.AudioTrack.MODE_STATIC
    val PLAYSTATE_PAUSED = android.media.AudioTrack.PLAYSTATE_PAUSED
    val PLAYSTATE_PLAYING = android.media.AudioTrack.PLAYSTATE_PLAYING
    val PLAYSTATE_STOPPED = android.media.AudioTrack.PLAYSTATE_STOPPED
    val STATE_INITIALIZED = android.media.AudioTrack.STATE_INITIALIZED
    val STATE_UNINITIALIZED = android.media.AudioTrack.STATE_UNINITIALIZED
    val STATE_NO_STATIC_DATA = android.media.AudioTrack.STATE_NO_STATIC_DATA
    val SUCCESS = android.media.AudioTrack.SUCCESS

    type AudioManager = android.media.AudioManager
    val STREAM_MUSIC = android.media.AudioManager.STREAM_MUSIC

    type AudioFormat = android.media.AudioFormat
    val CHANNEL_OUT_STEREO = android.media.AudioFormat.CHANNEL_OUT_STEREO
    val ENCODING_PCM_16BIT = android.media.AudioFormat.ENCODING_PCM_16BIT


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

    type Point = android.graphics.Point

    type Color = android.graphics.Color
    val TRANSPARENT = android.graphics.Color.TRANSPARENT
    val YELLOW = android.graphics.Color.YELLOW
    val BLUE = android.graphics.Color.parseColor("#0099ff")
    val DKBLUE = android.graphics.Color.parseColor("#0033ff")
    val BLACK = android.graphics.Color.BLACK
    val DKGRAY = android.graphics.Color.parseColor("#333333")
    val LDKGRAY = android.graphics.Color.parseColor("#666666")
    val GRAY = android.graphics.Color.parseColor("#999999")
    val LTGRAY = android.graphics.Color.parseColor("#cccccc")
    val WHITE = android.graphics.Color.WHITE
    type MediaStore = android.provider.MediaStore 
    val AUDIO_URI = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val ALBUM_URI = android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

    val DATA = android.provider.MediaStore.MediaColumns.DATA
    val TITLE = android.provider.MediaStore.MediaColumns.TITLE

    val DURATION = android.provider.MediaStore.Audio.AudioColumns.DURATION

    val ALBUM = android.provider.MediaStore.Audio.AlbumColumns.ALBUM
    val ALBUM_KEY = android.provider.MediaStore.Audio.AlbumColumns.ALBUM_KEY
    val ALBUM_ID = android.provider.MediaStore.Audio.AlbumColumns.ALBUM_ID
    val ALBUM_ARTIST = android.provider.MediaStore.Audio.AlbumColumns.ARTIST
    val ALBUM_ART = android.provider.MediaStore.Audio.AlbumColumns.ALBUM_ART
    val ALBUM_NUMBER_OF_SONGS = android.provider.MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS


    val ARTIST = android.provider.MediaStore.Audio.ArtistColumns.ARTIST
    val ARTIST_KEY = android.provider.MediaStore.Audio.ArtistColumns.ARTIST_KEY
    val ARTIST_NUMBER_OF_ALBUMS = android.provider.MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS
    val ARTIST_NUMBER_OF_TRACKS = android.provider.MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS

    val smallDp = 16 
    val medDp = 64

    type ColorDrawable = android.graphics.drawable.ColorDrawable

    val createDrawableFromPath = android.graphics.drawable.Drawable.createFromPath _

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

    val Subscription = rx.lang.scala.Subscription
    type Subscription = rx.lang.scala.Subscription

    val TrampolineScheduler = rx.lang.scala.schedulers.TrampolineScheduler
    val NewThreadScheduler = rx.lang.scala.schedulers.NewThreadScheduler

    val Observable = rx.lang.scala.Observable
    type Observable[+T] = rx.lang.scala.Observable[T]
    type Observer[-T] = rx.lang.scala.Observer[T]
    val ReplaySubject = rx.lang.scala.subjects.ReplaySubject
    type ReplaySubject[T] = rx.lang.scala.subjects.ReplaySubject[T]

    type Bitmap = android.graphics.Bitmap

    type Runnable = java.lang.Runnable

    val mainActorRef = ActorSystem("actorSystem").actorOf(MainActor.props(), "mainActor")

    def dimension(activity: Activity) = {
      val d = new Point()
      val display = activity.getWindowManager().getDefaultDisplay()
      display.getSize(d)
      d
    }

  }
}
