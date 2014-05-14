package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

class AlbumListAdapter(activity: Activity, initialAlbumList: List[(String, List[Track])]) extends BaseAdapter {

   val xxx = initialAlbumList.map(_._1)

  Toast.makeText(activity, " album " + xxx , Toast.LENGTH_SHORT).show()


  private var _albumList: List[(String, List[Track])] = initialAlbumList

  override def getCount(): Int = _albumList.size

  override def getItem(position: Int): (String, List[Track]) = _albumList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    Log.d("chakra", "album pos " + position)

    val albumTuple = getItem(position)

    new LinearLayout(activity) {
      setOrientation(VERTICAL)
      setBackgroundColor(GRAY)
      setLayoutParams(new LVLayoutParams(MATCH_PARENT, WRAP_CONTENT))
      List(albumTuple._1, albumTuple._2.size + " tracks") foreach {
        (term: String) => { 
          addView {
            new TextView(activity) {
              setText(term)
              setTextColor(WHITE)
              setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
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
