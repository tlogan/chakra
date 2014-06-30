package com.logan.feelchakra

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
        val layout = ImageSplitLayout.create(context, track.album.coverArt, TextLayout.createTextLayout(context, track.title, track.album.title, track.artist))
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
