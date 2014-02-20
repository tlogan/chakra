package com.feelchakra

import android.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams._
import android.view._
import android.widget._

import android.graphics.Color
import rx.lang.scala.Subject

import guava.scala.android.RichListView.listView2RichListView


class TrackLayout(context: Context) extends LinearLayout(context) {

   setOrientation(LinearLayout.VERTICAL)
   setLayoutParams {
     new LinearLayout.LayoutParams(MATCH_PARENT, 0, 1)
   }

   def setTrackOption(trackOption: Option[Track]): Unit = {
     removeAllViews()
     trackOption match {
       case Some(track) =>
         //update the track info
         List(track.title, track.album, track.artist) foreach {
           (term: String) => { 
             addView {
               new TextView(context) {
                 setText(term)
               }
             }
           } 
         }
       case None => {}
     }
   }


}
