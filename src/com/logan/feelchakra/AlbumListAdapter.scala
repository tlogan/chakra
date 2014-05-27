package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

import RichListView.listView2RichListView
import RichView.view2RichView

class AlbumListAdapter(activity: Activity) extends BaseAdapter {

  private var _albumTupleOp: Option[(String, List[Track])] = None 
  private var _albumMap: AlbumMap = new AlbumMap() 
  private var _albumList: List[(String, List[Track])] = _albumMap.toList
  private var _positionMap: Map[(String, List[Track]), Int] = _albumMap.zipWithIndex

  def albumTuplePosition: Int = _albumTupleOp match {
    case Some(albumTuple) =>
      _positionMap(albumTuple)
    case None => 0
  }

  private var _playmap: Map[Track, List[Int]] = HashMap() 
  private var _trackOption: Option[Track] = None 

  override def getCount(): Int = _albumMap.size

  override def getItem(position: Int): (String, List[Track]) = {
    _albumList(getItemId(position).toInt)
  }

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val albumTuple = getItem(position)
    val album = albumTuple._1
    val trackList = albumTuple._2

    val rightLayout = _albumTupleOp match {
      case Some(openAlbumTuple) if (albumTuple == openAlbumTuple) =>
        new AlbumLayout(activity, album, trackList, _playmap, _trackOption) 
      case _ =>
        new TextLayout(activity, album, trackList.size + " Tracks", "time") {
          setBackgroundColor(DKGRAY)
        }
    }

    rightLayout.setOnClick(view => {
      mainActorRef ! MainActor.SelectAlbumTuple(albumTuple) 
    })

    new ImageSplitLayout(activity, rightLayout)
  }

  def setAlbumMap(albumMap: AlbumMap): Unit = {
    _albumMap = albumMap
    _albumList = _albumMap.toList 
    _positionMap = _albumMap.zipWithIndex
    this.notifyDataSetChanged()
  }

  def setAlbumTupleOp(albumTupleOp: Option[(String, List[Track])]): Unit = {
    _albumTupleOp = albumTupleOp
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
