package com.logan.feelchakra

import android.util.Log
import android.widget.Toast

import RichListView.listView2RichListView
import RichView.view2RichView
import RichContext.context2RichContext

trait AlbumListAdapter {
  this: BaseAdapter =>

  override def getItem(position: Int): (Album, List[Track])

  def albumTuplePosition: Int
  def setAlbumMap(albumMap: AlbumMap): Unit
  def setAlbumTupleOp(albumTupleOp: Option[(Album, List[Track])]): Unit
  def setPlaymap(playmap: Map[Track, TreeSet[Int]]): Unit
  def setTrackOption(trackOption: Option[Track]): Unit

}

object AlbumListAdapter {

  def create(context: Context): BaseAdapter with AlbumListAdapter = {

    var _albumTupleOp: Option[(Album, List[Track])] = None 
    var _albumMap: AlbumMap = AlbumMap() 
    var _albumList: List[(Album, List[Track])] = _albumMap.toList
    var _positionMap: Map[(Album, List[Track]), Int] = _albumMap.zipWithIndex
    var _playmap: Map[Track, TreeSet[Int]] = HashMap() 
    var _trackOption: Option[Track] = None 

    new BaseAdapter with AlbumListAdapter {

      override def getCount(): Int = _albumMap.size

      override def getItem(position: Int): (Album, List[Track]) = {
        _albumList(getItemId(position).toInt)
      }

      override def getItemId(position: Int): Long = position 

      override def getView(position: Int, view: View, viewGroup: ViewGroup): View = {

        val albumTuple = getItem(position)
        val album = albumTuple._1
        val trackList = albumTuple._2

        _albumTupleOp match {
          case Some(openAlbumTuple) if (albumTuple == openAlbumTuple) =>
            ImageSplitLayout.create(context, album.coverArt, {
              val v = TextLayout.createAlbumLayout(context, album.title, trackList, _playmap, _trackOption) 
              v.setOnClick(view => {
                mainActorRef ! MainActor.SelectAlbumTuple(albumTuple) 
              })
              v
            })
          case _ =>
            ImageSplitLayout.createMain(context, album.coverArt, {
              val v = TextLayout.createTextLayout(context, album.title, trackList.size + " Tracks", "time") 
              v.setLayoutParams(new LLLayoutParams(MATCH_PARENT, context.dp(64)))
              v.setBackgroundColor(DKGRAY)
              v.setOnClick(view => {
                mainActorRef ! MainActor.SelectAlbumTuple(albumTuple) 
              })
              v
            })
        }

      }

      override def albumTuplePosition: Int = _albumTupleOp match {
        case Some(albumTuple) =>
          _positionMap(albumTuple)
        case None => 0
      }


      override def setAlbumMap(albumMap: AlbumMap): Unit = {
        _albumMap = albumMap
        _albumList = _albumMap.toList 
        _positionMap = _albumMap.zipWithIndex
        this.notifyDataSetChanged()
      }

      override def setAlbumTupleOp(albumTupleOp: Option[(Album, List[Track])]): Unit = {
        _albumTupleOp = albumTupleOp
        this.notifyDataSetChanged()
      }

      override def setPlaymap(playmap: Map[Track, TreeSet[Int]]): Unit = {
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
