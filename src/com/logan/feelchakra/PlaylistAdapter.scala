package com.logan.feelchakra

trait PlaylistAdapter {
  this: BaseAdapter =>

  override def getItem(position: Int): Track
  def setPlaylist(playlist: List[Track]): Unit
  def setTrackIndex(trackIndex: Int): Unit

}

object PlaylistAdapter {

  def create(context: Context): BaseAdapter with PlaylistAdapter = {

    var _playlist: List[Track] = List() 
    var _trackIndex: Int = -1 

    new BaseAdapter with PlaylistAdapter {

      private var _playlist: List[Track] = List() 
      private var _trackIndex: Int = -1 

      override def getCount(): Int = if (_trackIndex < 0) _playlist.size else _playlist.size - 1

      override def getItem(position: Int): Track = _playlist(getItemId(position).toInt)

      override def getItemId(position: Int): Long = {
        val itemId = if (_trackIndex < 0 || position < _trackIndex) position else {
          position + 1
        }
        assert(itemId != _trackIndex)
        itemId
      }

      override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

        val track = getItem(position)
        val color = if (getItemId(position) < _trackIndex) {
          DKGRAY 
        } else GRAY 

        {
          val layout = ImageSplitLayout.create(context, TextLayout.createTextLayout(context, track.title, track.album, track.artist))
          layout.setBackgroundColor(color)
          layout
        }

      }

      override def setPlaylist(playlist: List[Track]): Unit = {
        _playlist = playlist 
        this.notifyDataSetChanged()
      }

      override def setTrackIndex(trackIndex: Int): Unit = {
        _trackIndex = trackIndex
        this.notifyDataSetChanged()
      }

    }

  }

}
