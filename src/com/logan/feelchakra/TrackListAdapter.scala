package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

trait TrackListAdapter {
  this: BaseAdapter =>

  override def getItem(position: Int): Track
  def setTrackList(trackList: List[Track]): Unit
  def setFutureTrackMap(futureTrackMap: Map[Track, Int]): Unit
  def setPresentTrackOption(trackOption: Option[Track]): Unit

}

object TrackListAdapter {

  def create(context: Context): BaseAdapter with TrackListAdapter = {

    var _trackList: List[Track] = List() 
    var _futureTrackMap: Map[Track, Int] = HashMap() 
    var _presentTrackOption: Option[Track] = None 

    new BaseAdapter() with TrackListAdapter {

      override def getCount(): Int = _trackList.size
      override def getItem(position: Int): Track = _trackList(getItemId(position).toInt)
      override def getItemId(position: Int): Long = position 

      override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {
        val track = getItem(position)
        ImageSplitLayout.createMain(context, track.album.coverArt, SlideLayout.createTrackLayout(context, track, _futureTrackMap, _presentTrackOption))
      }

      override def setTrackList(trackList: List[Track]): Unit = {
        _trackList = trackList
        this.notifyDataSetChanged()
      }

      override def setPresentTrackOption(trackOption: Option[Track]): Unit = {
        _presentTrackOption = trackOption 
        this.notifyDataSetChanged()
      }

      override def setFutureTrackMap(futureTrackMap: Map[Track, Int]): Unit = {
        _futureTrackMap = futureTrackMap
        this.notifyDataSetChanged()
      }

    }

  }

}
