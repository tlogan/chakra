package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

class TrackListAdapter(activity: Activity) extends BaseAdapter {

  private var _trackList: List[Track] = List() 

  private var _playmap: Map[Track, List[Int]] = HashMap() 
  private var _trackOption: Option[Track] = None 

  override def getCount(): Int = _trackList.size

  override def getItem(position: Int): Track = _trackList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val track = getItem(position)

    new ImageSplitLayout(activity, new TrackLayout(activity, track, _playmap, _trackOption){
      setLayoutParams(new LLLayoutParams(MATCH_PARENT, activity.dp(64)))
    })

  }

  def setTrackList(trackList: List[Track]): Unit = {
    _trackList = trackList
    this.notifyDataSetChanged()
  }

  def setPlaymap(playmap: Map[Track, List[Int]]): Unit = {
    _playmap = playmap
    this.notifyDataSetChanged()
  }

  def setTrackOption(trackOption: Option[Track]): Unit = {
    _trackOption = trackOption 
    this.notifyDataSetChanged()
  }

}
