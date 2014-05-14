package com.logan.feelchakra

import RichListView.listView2RichListView
import RichView.view2RichView
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

      addView {
        new TextView(activity) {
          setText(artist)
          setTextSize(20)
          setTextColor(WHITE)
        }
      }

      addView {
        new TextView(activity) {
          setText(albumMap.size + " Albums")
          setTextSize(14)
          setTextColor(WHITE)
        }
      }
      
      _artistTupleOp match {
        case Some(openArtistTuple) =>
          if (artistTuple == openArtistTuple) {

            albumMap.foreach(albumTuple => {
              val album = albumTuple._1
              val trackList = albumTuple._2

              addView {
                new View(activity) {
                  setBackgroundColor(BLACK)
                  setLayoutParams(new LLLayoutParams(MATCH_PARENT, 2))
                }
              }

              addView {
                val albumLL = new LinearLayout(activity) {
                  setOrientation(HORIZONTAL)
                  setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
                  setBackgroundColor(BLACK)
                  addView {
                    new View(activity) {
                      setBackgroundColor(YELLOW)
                      setLayoutParams(new LLLayoutParams(80, 80))
                    }
                  }

                  addView {
                    new LinearLayout(activity) {
                      setOrientation(VERTICAL)
                      setBackgroundColor(DKGRAY)
                      setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
                      setPadding(10, 10, 10, 10)

                      addView {
                        new TextView(activity) {
                          setText(album)
                          setTextSize(20)
                          setTextColor(WHITE)
                        }
                      }

                      addView {
                        new TextView(activity) {
                          setText(trackList.size + " Tracks")
                          setTextSize(14)
                          setTextColor(WHITE)
                        }
                      }

                      addView {
                        new View(activity) {
                          setLayoutParams(new LLLayoutParams(MATCH_PARENT, 8))
                        }
                      }

                      trackList.toIterator.zipWithIndex.foreach(pair => {
                        val track = pair._1
                        val trackNum = pair._2 + 1

                        addView {
                          new LinearLayout(activity) {
                            setOrientation(VERTICAL)
                            setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))

                            addView {
                              new TextView(activity) {
                                setText(trackNum + ". " + track.title)
                                setTextSize(18)
                                setTextColor(WHITE)
                                setPadding(0, 6, 0, 6)
                                setLayoutParams(new LLLayoutParams(MATCH_PARENT, WRAP_CONTENT))
                              }
                            }
                            
                          }
                        }

                        if (trackNum != trackList.size) {
                          addView {
                            new View(activity) {
                              setBackgroundColor(WHITE)
                              setLayoutParams(new LLLayoutParams(MATCH_PARENT, 1))
                            }
                          }
                        }

                      })

                    }
                  }

                }
                albumLL
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
