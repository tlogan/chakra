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
import android.widget._
import android.graphics.Color

import rx.lang.scala.Observer

class PlaylistAdapter(activity: Activity, initialPlaylist: List[Track]) extends BaseAdapter {

  private var _trackList: List[Track] = initialPlaylist

  override def getCount(): Int = _trackList.size

  override def getItem(position: Int): Track = _trackList(getItemId(position).toInt)

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

  def setPlaylist(trackList: List[Track]): Unit = {
    _trackList = trackList
    this.notifyDataSetChanged()
  }

}
