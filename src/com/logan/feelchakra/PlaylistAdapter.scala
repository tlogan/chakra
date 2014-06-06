package com.logan.feelchakra

class PlaylistAdapter(activity: Activity) extends BaseAdapter {

  private var _playlist: List[Track] = List() 
  private var _trackIndex: Int = -1 

  override def getCount(): Int = if (_trackIndex < 0) _playlist.size else _playlist.size - 1

  override def getItem(position: Int): Track = _playlist(getItemId(position).toInt)

  override def getItemId(position: Int): Long = {
    if (_trackIndex < 0 || position < _trackIndex) position else {
      position + 1
    }
  }

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val track = getItem(position)

    if (getItemId(position) == _trackIndex) {
      //ERROR
      new LinearLayout(activity) {
        setVisibility(GONE)
      }
    } else {
      val color = if (getItemId(position) < _trackIndex) {
        DKGRAY 
      } else GRAY 
      new ImageSplitLayout(activity, new TextLayout(activity, track.title, track.album, track.artist)) {
        setBackgroundColor(color)
      }
    }

  }

  def setPlaylist(playlist: List[Track]): Unit = {
    _playlist = playlist 
    this.notifyDataSetChanged()
  }

  def setTrackIndex(trackIndex: Int): Unit = {
    _trackIndex = trackIndex
    this.notifyDataSetChanged()
  }

}
