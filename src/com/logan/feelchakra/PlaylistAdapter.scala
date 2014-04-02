package com.logan.feelchakra

class PlaylistAdapter(activity: Activity) extends BaseAdapter {

  private var _playlist: List[Track] = List() 
  private var _trackIndex: Int = -1 

  override def getCount(): Int = _playlist.size

  override def getItem(position: Int): Track = _playlist(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {


    new LinearLayout(activity) {

      val track = getItem(position)

      if (getItemId(position) == _trackIndex) {
        setVisibility(GONE)
      } else {

        setOrientation(VERTICAL)
        val bgColor = if (getItemId(position) < _trackIndex) DKGRAY else GRAY
        setBackgroundColor(bgColor)
        List(track.title, track.album, track.artist) foreach {
          (term: String) => { 
            addView {
              new TextView(activity) {
                setTextColor(WHITE)
                setText(term)
              }
            }
          } 
        }

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
