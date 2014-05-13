package com.logan.feelchakra

class ArtistListAdapter(activity: Activity, initialArtistList: List[(String, AlbumMap)]) extends BaseAdapter {

  private var _artistList: List[(String, AlbumMap)] = initialArtistList

  override def getCount(): Int = _artistList.size

  override def getItem(position: Int): (String, AlbumMap) = _artistList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val artistTuple = getItem(position)

    new LinearLayout(activity) {
      setOrientation(VERTICAL)
      setBackgroundColor(GRAY)
      List(artistTuple._1, artistTuple._2.size + " albums") foreach {
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

  def setArtistList(artistList: List[(String, AlbumMap)]): Unit = {
    _artistList = artistList
    this.notifyDataSetChanged()
  }

}
