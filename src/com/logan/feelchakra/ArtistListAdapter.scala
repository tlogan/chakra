package com.logan.feelchakra

import RichListView.listView2RichListView
import RichView.view2RichView
import android.util.Log
import android.widget.Toast
import RichContext.context2RichContext

trait ArtistListAdapter {
  this: BaseAdapter =>

  override def getItem(position: Int): (String, AlbumMap)

  def setArtistMap(artistMap: ArtistMap): Unit
  def setArtistTupleOp(artistTupleOp: Option[(String, AlbumMap)]): Unit
  def setPlaymap(playmap: Map[Track, Int]): Unit
  def setTrackOption(trackOption: Option[Track]): Unit
  def artistTuplePosition: Int

}

object ArtistListAdapter {

  def create(context: Context): BaseAdapter with ArtistListAdapter = {

    var _artistTupleOp: Option[(String, AlbumMap)] = None 
    var _artistMap: ArtistMap = new ArtistMap() 
    var _artistList: List[(String, AlbumMap)] = _artistMap.toList
    var _positionMap: Map[(String, AlbumMap), Int] = _artistMap.zipWithIndex
    var _playmap: Map[Track, Int] = HashMap() 
    var _trackOption: Option[Track] = None 


    new BaseAdapter() with ArtistListAdapter {

      override def getCount(): Int = _artistList.size
      override def getItem(position: Int): (String, AlbumMap) = _artistList(getItemId(position).toInt)
      override def getItemId(position: Int): Long = position 
      override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

        val artistTuple = getItem(position)
        val artist = artistTuple._1
        val albumMap = artistTuple._2
        val lastAlbum = albumMap.last._1

        _artistTupleOp match {
          case Some(openArtistTuple) if (artistTuple == openArtistTuple) => 
            new LinearLayout(context) {
              setOrientation(VERTICAL)
              setBackgroundColor(GRAY)
              setLayoutParams(new LVLayoutParams(MATCH_PARENT, WRAP_CONTENT))

              val imTxLayout = ImageSplitLayout.createMain(context, lastAlbum.coverArt, {
                val t = TextLayout.createTextLayout(context, artist, albumMap.size + " Albums", "time") 
                t.setBackgroundColor(LDKGRAY)
                t.setOnClick(view => {
                  mainActorRef ! MainActor.SelectArtistTuple(artistTuple) 
                })
                t
              })
              addView(imTxLayout)

              albumMap.foreach(albumTuple => {
                val album = albumTuple._1
                val trackList = albumTuple._2

                addView {
                  new View(context) {
                    setBackgroundColor(LTGRAY)
                    setLayoutParams(new LLLayoutParams(MATCH_PARENT, 2))
                  }
                }

                addView {
                  ImageSplitLayout.create(context, album.coverArt, TextLayout.createAlbumLayout(context, album.title, trackList, _playmap, _trackOption))
                }

              })

            }
          case _ =>
             ImageSplitLayout.createMain(context, lastAlbum.coverArt, {
              val t = TextLayout.createTextLayout(context, artist, albumMap.size + " Albums", "time") 
              t.setBackgroundColor(DKGRAY)
              t.setOnClick(view => {
                mainActorRef ! MainActor.SelectArtistTuple(artistTuple) 
              })
              t
            })
        }

      }

      override def artistTuplePosition: Int = _artistTupleOp match {
        case Some(artistTuple) =>
          _positionMap(artistTuple)
        case None => 0
      }

      override def setArtistMap(artistMap: ArtistMap): Unit = {
        _artistMap = artistMap
        _artistList = _artistMap.toList 
        _positionMap = _artistMap.zipWithIndex
        this.notifyDataSetChanged()
      }

      override def setArtistTupleOp(artistTupleOp: Option[(String, AlbumMap)]): Unit = {
        _artistTupleOp = artistTupleOp
        this.notifyDataSetChanged()
      }

      override def setPlaymap(playmap: Map[Track, Int]): Unit = {
        _playmap = playmap
        this.notifyDataSetChanged()
      }

      override def setTrackOption(trackOption: Option[Track]): Unit = {
        _trackOption = trackOption 
        this.notifyDataSetChanged()
      }

    }
  }

}
