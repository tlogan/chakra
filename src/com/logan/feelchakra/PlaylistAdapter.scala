package com.logan.feelchakra

import android.util.Log
import android.widget.Toast
import RichView.view2RichView
import RichContext.context2RichContext

trait PlaylistAdapter {
  this: BaseAdapter =>

  override def getItem(position: Int): Track
  def setPastTrackList(list: List[Track]): Unit
  def setPresentTrackOption(trackOption: Option[Track]): Unit
  def setFutureTrackList(list: List[Track]): Unit
  def isPast(position: Int): Boolean 
  def pastIndex(position: Int): Int
  def futureIndex(position: Int): Int
  def firstFuturePosition(): Int

}

object PlaylistAdapter {

  def create(context: Context): BaseAdapter with PlaylistAdapter = {

    var _pastTrackList: List[Track] = List()
    var _futureTrackList: List[Track] = List()
    var _presentTrackOp: Option[Track] = None 

    new BaseAdapter with PlaylistAdapter {

      private var _playlist: List[Track] = List() 
      private var _trackIndex: Int = -1 

      override def getCount(): Int = _pastTrackList.size + _futureTrackList.size 

      override def firstFuturePosition(): Int = _pastTrackList.size

      override def isPast(position: Int): Boolean = position < _pastTrackList.size

      override def pastIndex(position: Int): Int = {
        require(position < _pastTrackList.size)
        position 
      }

      override def futureIndex(position: Int): Int = {
        require(position >= _pastTrackList.size)
        position - _pastTrackList.size 
      }

      override def getItem(position: Int): Track = {
        val pastSize = _pastTrackList.size
        if (position < pastSize) {
          _pastTrackList(position)
        } else {
          _futureTrackList(position - pastSize)
        }
      }

      override def getItemId(position: Int): Long = position 

      override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

        val track = getItem(position)
        val color = if (position < _pastTrackList.size) DKGRAY else GRAY 

        val hl = new LinearLayout(context)
        hl.setOrientation(HORIZONTAL)
        hl.addView {
          val t = TextLayout.createTextLayout(context, track.title, track.album.title, track.artist, "", "", "")
          t.setLayoutParams(new LLLayoutParams(0, MATCH_PARENT, 20))
          t
        }
        hl.addView {
          val tv = new TextView(context)
          tv.setLayoutParams(new LLLayoutParams(context.dp(32), MATCH_PARENT))
          val playOrder = if (isPast(position)) {
            (pastIndex(position) - _pastTrackList.size).toString
          } else {
            "+" + (futureIndex(position) + 1)
          }
          tv.setText(playOrder)
          tv.setTextSize(context.sp(10))
          tv.setTextColor(WHITE)
          tv
        }

        val layout = ImageSplitLayout.create(context, track.album.coverArt, hl)
        layout.setBackgroundColor(color)
        layout

      }

      override def setPastTrackList(list: List[Track]): Unit = {
        _pastTrackList = list 
        this.notifyDataSetChanged()
      }

      override def setPresentTrackOption(trackOption: Option[Track]): Unit = {
        _presentTrackOp = trackOption
        this.notifyDataSetChanged()
      }

      override def setFutureTrackList(list: List[Track]): Unit = {
        _futureTrackList = list 
        this.notifyDataSetChanged()
      }

    }

  }

}
