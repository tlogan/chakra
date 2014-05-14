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

    Log.d("chakra", "artist pos " + position)

    val artistTuple = getItem(position)
    val artist = artistTuple._1
    val albumMap = artistTuple._2

    new LinearLayout(activity) {
      setOrientation(VERTICAL)
      setBackgroundColor(DKGRAY)
      setLayoutParams(new LVLayoutParams(MATCH_PARENT, WRAP_CONTENT))
      List(artist, albumMap.size + " albums") foreach {
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

            addView {
              new LinearLayout(activity) {
                setOrientation(HORIZONTAL)
                setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
                addView {
                  new TextView(activity) {
                    setText("cover")
                    setBackgroundColor(YELLOW)
                    setLayoutParams(new LLLayoutParams(100, WRAP_CONTENT))
                  }
                }

                addView {

                  new LinearLayout(activity) {
                    setOrientation(VERTICAL)
                    setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
                    albumMap.foreach(tuple => {
                      val album = tuple._1
                      val trackList = tuple._2
                      List(album, trackList.size + " Tracks").foreach(text => {
                        addView {
                          new TextView(activity) {
                            setText(text)
                            setBackgroundColor(GRAY)
                            setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
                          }
                        }
                      })

                    })
                  }
                }

              }

            }


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
