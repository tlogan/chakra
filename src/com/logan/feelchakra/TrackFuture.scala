package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
 
object TrackFuture {

  def apply(trackPath: String): Future[Track] = {
    Future {
      val retriever = new MediaMetadataRetriever()
      retriever.setDataSource(trackPath)
      val title = retriever.extractMetadata(METADATA_KEY_TITLE)
      val artist = retriever.extractMetadata(METADATA_KEY_ARTIST)
      val albumTitle = retriever.extractMetadata(METADATA_KEY_ALBUM)
      val albumArt = retriever.getEmbeddedPicture() match {
        case null => null
        case byteArray: Array[Byte] => new BitmapDrawable(createBitmap(byteArray))
      }
      val duration = retriever.extractMetadata(METADATA_KEY_DURATION).toInt

      Track(trackPath, title, Album(albumTitle, albumArt), artist, duration)

    }
  }

}
