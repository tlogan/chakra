package com.logan.feelchakra

import RichListView.listView2RichListView
import RichView.view2RichView
import android.util.Log
import android.widget.Toast
import RichContext.context2RichContext

class ArtistListAdapter(activity: Activity) extends BaseAdapter {

  private var _artistTupleOp: Option[(String, AlbumMap)] = None 
  private var _artistMap: ArtistMap = new ArtistMap() 
  private var _artistList: List[(String, AlbumMap)] = _artistMap.toList
  private var _positionMap: Map[(String, AlbumMap), Int] = _artistMap.zipWithIndex

  def artistTuplePosition: Int = _artistTupleOp match {
    case Some(artistTuple) =>
      _positionMap(artistTuple)
    case None => 0
  }

  private var _playmap: Map[Track, List[Int]] = HashMap() 
  private var _trackOption: Option[Track] = None 

  override def getCount(): Int = _artistList.size

  override def getItem(position: Int): (String, AlbumMap) = _artistList(getItemId(position).toInt)

  override def getItemId(position: Int): Long = position 

  override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

    val artistTuple = getItem(position)
    val artist = artistTuple._1
    val albumMap = artistTuple._2

    _artistTupleOp match {
      case Some(openArtistTuple) if (artistTuple == openArtistTuple) => 
        new LinearLayout(activity) {
          setOrientation(VERTICAL)
          setBackgroundColor(GRAY)
          setLayoutParams(new LVLayoutParams(MATCH_PARENT, WRAP_CONTENT))

          val imTxLayout = new MainImageSplitLayout(activity, new TextLayout(activity, artist, albumMap.size + " Albums", "time") {
            setBackgroundColor(LDKGRAY)
            this.setOnClick(view => {
              mainActorRef ! MainActor.SelectArtistTuple(artistTuple) 
            })
          })
          addView(imTxLayout)

          albumMap.foreach(albumTuple => {
            val album = albumTuple._1
            val trackList = albumTuple._2

            addView {
              new View(activity) {
                setBackgroundColor(LTGRAY)
                setLayoutParams(new LLLayoutParams(MATCH_PARENT, 2))
              }
            }

            addView {
              new ImageSplitLayout(activity, new AlbumLayout(activity, album, trackList, _playmap, _trackOption))
            }

          })

        }
      case _ =>
        new MainImageSplitLayout(activity, new TextLayout(activity, artist, albumMap.size + " Albums", "time") {
          setBackgroundColor(DKGRAY)
          this.setOnClick(view => {
            mainActorRef ! MainActor.SelectArtistTuple(artistTuple) 
          })
        })
    }

  }

  def setArtistMap(artistMap: ArtistMap): Unit = {
    _artistMap = artistMap
    _artistList = _artistMap.toList 
    _positionMap = _artistMap.zipWithIndex
    this.notifyDataSetChanged()
  }

  def setArtistTupleOp(artistTupleOp: Option[(String, AlbumMap)]): Unit = {
    _artistTupleOp = artistTupleOp
    this.notifyDataSetChanged()
  }

  def setPlaymap(playmap: Map[Track, List[Int]]): Unit = {
    _playmap = playmap
    this.notifyDataSetChanged()
  }

  def setTrackOption(trackOption: Option[Track]): Unit = {
    _trackOption = trackOption 
    this.notifyDataSetChanged()
  }

}
