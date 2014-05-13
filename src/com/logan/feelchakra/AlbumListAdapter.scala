package com.logan.feelchakra

class AlbumListAdapter(activity: Activity, initialAlbumList: List[(String, List[Track])]) extends BaseAdapter {

  private var _albumList: List[(String, List[Track])] = initialAlbumList

  override def getCount(): Int = _albumList.size

  override def getItem(position: Int): (String, List[Track]) = _albumList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val albumTuple = getItem(position)

    new LinearLayout(activity) {
      setOrientation(VERTICAL)
      setBackgroundColor(GRAY)
      List(albumTuple._1, albumTuple._2.size + " tracks") foreach {
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

  def setAlbumList(albumList: List[(String, List[Track])]): Unit = {
    _albumList = albumList
    this.notifyDataSetChanged()
  }

}
