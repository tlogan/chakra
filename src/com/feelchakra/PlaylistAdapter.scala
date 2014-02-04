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

class PlaylistAdapter(activity: Activity, trackIndex: Int, playlist: List[Track]) extends BaseAdapter {

  private var _playlist: List[Track] = playlist
  private var _trackIndex: Int = trackIndex 

  override def getCount(): Int = _playlist.size

  override def getItem(position: Int): Track = _playlist(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {


    new LinearLayout(activity) {

      val track = getItem(position)

      if (getItemId(position) == _trackIndex) {
        setVisibility(View.GONE)
      } else {

        setOrientation(LinearLayout.VERTICAL)
        val bgColor = if (getItemId(position) < _trackIndex) Color.DKGRAY else Color.GRAY
        setBackgroundColor(bgColor)
        List(track.title, track.album, track.artist) foreach {
          (term: String) => { 
            addView {
              new TextView(activity) {
                setTextColor(Color.WHITE)
                setText(term)
              }
            }
          } 
        }

      }
        


    }

  }

  def onPlaylistChanged(trackIndex: Int, playlist: List[Track]): Unit = {
    _trackIndex = trackIndex
    _playlist = playlist 
    this.notifyDataSetChanged()
  }

}
