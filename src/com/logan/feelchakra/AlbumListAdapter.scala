package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

class AlbumListAdapter(activity: Activity, initialAlbumList: List[(String, List[Track])]) extends BaseAdapter {

  private var _albumList: List[(String, List[Track])] = initialAlbumList

  override def getCount(): Int = _albumList.size

  override def getItem(position: Int): (String, List[Track]) = _albumList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val albumTuple = getItem(position)
    val album = albumTuple._1
    val trackList = albumTuple._2

    new LinearLayout(activity) {
      setOrientation(VERTICAL)
      setBackgroundColor(DKGRAY)
      setLayoutParams(new LVLayoutParams(MATCH_PARENT, WRAP_CONTENT))
      List(album, trackList.size + " Tracks").foreach(term => { 
        addView {
          new TextView(activity) {
            setText(term)
            setTextColor(WHITE)
            setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
          }
        }
      }) 
    }
  }

  def setAlbumList(albumList: List[(String, List[Track])]): Unit = {
    _albumList = albumList
    this.notifyDataSetChanged()
  }

}
