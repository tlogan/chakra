package com.logan.feelchakra

import RichListView.listView2RichListView
import android.util.Log
import android.widget.Toast

class ArtistListAdapter(activity: Activity, initialArtistList: List[(String, AlbumMap)]) extends BaseAdapter {

  private var _artistList: List[(String, AlbumMap)] = initialArtistList
  private var _artistTupleOp: Option[(String, AlbumMap)] = None 

  override def getCount(): Int = _artistList.size

  override def getItem(position: Int): (String, AlbumMap) = _artistList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val artistTuple = getItem(position)
    val artist = artistTuple._1
    val albumMap = artistTuple._2

    new LinearLayout(activity) {
      setOrientation(VERTICAL)
      setBackgroundColor(DKGRAY)
      setLayoutParams(new LVLayoutParams(MATCH_PARENT, WRAP_CONTENT))
      List(artist, albumMap.size + " Albums") foreach {
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
      
      _artistTupleOp match {
        case Some(openArtistTuple) =>
          if (artistTuple == openArtistTuple) {

            setBackgroundColor(GRAY)

            albumMap.foreach(tuple => {
              val album = tuple._1
              val trackList = tuple._2

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
                      List(album, trackList.size + " Tracks").foreach(text => {
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
          }
        case None => {}
      }


    }


  }

  def setArtistList(artistList: List[(String, AlbumMap)]): Unit = {
    _artistList = artistList
    this.notifyDataSetChanged()
  }

  def setArtistTuple(artistTuple: (String, AlbumMap)): Unit = {
    _artistTupleOp = Some(artistTuple)
    this.notifyDataSetChanged()
  }

}
