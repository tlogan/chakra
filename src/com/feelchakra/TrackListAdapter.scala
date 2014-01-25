package com.feelchakra

import scala.collection.immutable.List

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams._
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color

class TrackListAdapter(activity: Activity, trackList: List[Track]) extends BaseAdapter {

  override def getCount(): Int = trackList.size

  override def getItem(position: Int): Track = trackList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val track = getItem(position)

    new LinearLayout(activity) {
      setOrientation(LinearLayout.VERTICAL)
      setBackgroundColor(Color.BLUE)
      List(track.title, track.album, track.artist) foreach {
        (term: String) => { 
          addView {
            new TextView(activity) {
              setText(term)
            }
          }
        } 
      }
    }


  }

}
