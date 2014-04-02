package com.logan.feelchakra

class TrackListAdapter(activity: Activity, initialTrackList: List[Track]) extends BaseAdapter {

  private var _trackList: List[Track] = initialTrackList

  override def getCount(): Int = _trackList.size

  override def getItem(position: Int): Track = _trackList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val track = getItem(position)

    new LinearLayout(activity) {
      setOrientation(VERTICAL)
      setBackgroundColor(GRAY)
      List(track.title, track.album, track.artist) foreach {
        (term: String) => { 
          addView {
            new TextView(activity) {
              setText(term)
              setTextColor(WHITE)
            }
          }
        } 
      }
    }


  }

  def setTrackList(trackList: List[Track]): Unit = {
    _trackList = trackList
    this.notifyDataSetChanged()
  }

}
