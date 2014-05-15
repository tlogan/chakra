package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

class AlbumListAdapter(activity: Activity, initialAlbumList: List[(String, List[Track])]) extends BaseAdapter {

  private var _albumTupleOp: Option[(String, List[Track])] = None 
  private var _albumList: List[(String, List[Track])] = initialAlbumList

  override def getCount(): Int = _albumList.size

  override def getItem(position: Int): (String, List[Track]) = _albumList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val albumTuple = getItem(position)
    val album = albumTuple._1
    val trackList = albumTuple._2

    _albumTupleOp match {
      case Some(openAlbumTuple) if (albumTuple == openAlbumTuple) =>
        new AlbumLayout(activity, album, trackList) 
      case _ =>
        new ImageTextLayout(activity, album, trackList.size + " Tracks", "time")
    }

  }

  def setAlbumList(albumList: List[(String, List[Track])]): Unit = {
    _albumList = albumList
    this.notifyDataSetChanged()
  }

  def setAlbumTuple(albumTuple: (String, List[Track])): Unit = {
    _albumTupleOp = Some(albumTuple)
    this.notifyDataSetChanged()
  }

}
