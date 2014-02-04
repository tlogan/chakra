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


object PlayerService {

   case class OnMainActorConnected(trackOption: Option[Track], playOncePrepared: Boolean, positionOncePrepared: Int)
   case class OnTrackOptionChanged(trackOption: Option[Track]) 
   case class OnPlayStateChanged(playOncePrepared: Boolean) 
   case class OnPositionChanged(positionOncePrepared: Int) 

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
        case _ => false
      }
    }
  })


  private val mainActorRef = MainActor.mainActorRef
  private val mediaPlayer = new MediaPlayer()
  private val that = this;

  private var _playOncePrepared: Boolean = false
  private var _positionOncePrepared: Int = 0
  private var _prepared: Boolean = false

  override def onBind(intent: Intent): IBinder = {
    return null;
  }

  override def onCreate(): Unit = {

    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      override def onPrepared(mp: MediaPlayer): Unit = {

        mp.seekTo(_positionOncePrepared)
        if (_playOncePrepared) mp.start()
        _prepared = true

      }
    })
    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

      override def onCompletion(mp: MediaPlayer): Unit = {
        //notify mainActor
      }

    })

    mainActorRef ! MainActor.SetPlayerServiceHandler(handler)

  }

  def onMainActorconnected(trackOption: Option[Track], playOncePrepared: Boolean, positionOncePrepared: Int): Unit = {

    _playOncePrepared = playOncePrepared
    _positionOncePrepared = positionOncePrepared
    onTrackOptionChanged(trackOption)

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

  override def onDestroy(): Unit =  super.onDestroy()

}

