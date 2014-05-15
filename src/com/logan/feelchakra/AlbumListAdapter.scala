package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

class AlbumListAdapter(activity: Activity, initialAlbumList: List[(String, List[Track])]) extends BaseAdapter {

  private var _albumTupleOp: Option[(String, List[Track])] = None 
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

      _albumTupleOp match {
        case Some(openAlbumTuple) if (albumTuple == openAlbumTuple) =>

          addView {
            new AlbumLayout(activity, album, trackList) 
          }

          /*

          trackList.toIterator.zipWithIndex.foreach(pair => {
            val track = pair._1
            val trackNum = pair._2 + 1

            addView {
              new LinearLayout(activity) {
                setOrientation(HORIZONTAL)
                setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
                addView {
                  new View(activity) {
                    setBackgroundColor(DKGRAY)
                    setLayoutParams(new LLLayoutParams(100, MATCH_PARENT))
                  }
                }

                addView {
                  new LinearLayout(activity) {
                    setOrientation(VERTICAL)
                    setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
                    addView {
                      new View(activity) {
                        setBackgroundColor(DKGRAY)
                        setLayoutParams(new LLLayoutParams(MATCH_PARENT, 1))
                      }
                    }
                    List(trackNum + ". " + track.title, track.artist).foreach(text => {
                      addView {
                        new TextView(activity) {
                          setText(text)
                          setTextColor(WHITE)
                          setBackgroundColor(GRAY)
                          setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
                        }
                      }
                    })
                  }
                }

              }
            }

          })
          */

        case _ =>
          addView {
            new ImageTextLayout(activity, album, trackList.size + " Tracks", "time")
          }

      }

    }

  }

  def setAlbumList(albumList: List[(String, List[Track])]): Unit = {
    _albumList = albumList
    this.notifyDataSetChanged()
  }

  def setAlbumTuple(albumTuple: (String, List[Track])): Unit = {
    _albumTupleOp = Some(albumTuple)
    this.notifyDataSetChanged()
  }

}
