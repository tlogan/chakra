package com.logan.feelchakra

class TrackListAdapter(activity: Activity) extends BaseAdapter {

  private var _trackList: List[Track] = List() 

  private var _playmap: Map[Track, List[Int]] = HashMap() 
  private var _trackOption: Option[Track] = None 

  override def getCount(): Int = _trackList.size

  override def getItem(position: Int): Track = _trackList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val track = getItem(position)

    new ImageTextLayout(activity, track.title, track.artist, track.album) {
      _playmap.get(track) match {
        case Some(posList) => 
          _trackOption match {
            case Some(currentTrack) if (currentTrack == track) =>
              blueify()
            case _ => lighten()
          }
        case None => darken()
      }
    }

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
