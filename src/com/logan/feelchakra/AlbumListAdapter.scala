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
  def setFutureTrackMap(futureTrackMap: Map[Track, Int]): Unit
  def setPresentTrackOption(trackOption: Option[Track]): Unit

}

object AlbumListAdapter {

  def create(context: Context): BaseAdapter with AlbumListAdapter = {

    var _albumTupleOp: Option[(Album, List[Track])] = None 
    var _albumMap: AlbumMap = AlbumMap() 
    var _albumList: List[(Album, List[Track])] = _albumMap.toList
    var _positionMap: Map[(Album, List[Track]), Int] = _albumMap.zipWithIndex
    var _futureTrackMap: Map[Track, Int] = HashMap() 
    var _presentTrackOption: Option[Track] = None 

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

        val totalDuration = trackList.foldLeft[Long](0)((duration, track) => {
          duration + track.duration
        })

        val minutes = totalDuration / 60000 
        val seconds = (totalDuration/1000) % 60
        val time = f"$minutes%02d:$seconds%02d"

        _albumTupleOp match {
          case Some(openAlbumTuple) if (albumTuple == openAlbumTuple) =>
            ImageSplitLayout.create(context, album.coverArt, {
              val v = TextLayout.createAlbumLayout(context, album.title, trackList, _futureTrackMap, _presentTrackOption) 
              v.setOnClick(view => {
                mainActorRef ! MainActor.SelectAlbumTuple(albumTuple) 
              })
              v
            })
          case _ =>
            ImageSplitLayout.createMain(context, album.coverArt, {
              val v = TextLayout.createTextLayout(context, album.title, trackList.size + " Tracks", time) 
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

      override def setFutureTrackMap(futureTrackMap: Map[Track, Int]): Unit = {
        _futureTrackMap = futureTrackMap
        this.notifyDataSetChanged()
      }

      override def setPresentTrackOption(trackOption: Option[Track]): Unit = {
        _presentTrackOption = trackOption 
        this.notifyDataSetChanged()
      }
    }
  }
}
