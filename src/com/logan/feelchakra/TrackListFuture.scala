package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
 
object TrackListFuture {

  private def createAlbumIdMap(database: Database): Future[Map[String, Album]] = {

    val promise = Promise[Map[String, Album]]()
    val buffer = new scala.collection.mutable.HashMap[String, Album]() 

    database.query (
      ALBUM_URI,
      null, null, null, null
    ) onComplete {
      case Success(table) => { 
        table.rowObservable.subscribe( 
          (row) => { 
            buffer += (row(ALBUM_KEY) -> Album(row(ALBUM), createDrawableFromPath(row(ALBUM_ART))))
          },
          (e: Throwable) => Log.d("chakra", "album ID row observable failed: " + ALBUM_URI + " : "+ e.getMessage),
          () => { 
            Log.d("chakra", "album map size: " + buffer.toMap[String, Album].size)
            promise.success(buffer.toMap[String, Album])
          }
        )

      }

      case Failure(t) => Log.d("chakra", "database query failed: " + t.getMessage) 
    }

    promise.future

  }

  def apply(database: Database): Future[List[Track]] = {

    val promise = Promise[List[Track]]()
    val trackListBuffer: ListBuffer[Track] = new ListBuffer[Track]() 
    val albumIdMapFuture = createAlbumIdMap(database)

    albumIdMapFuture onComplete {
      case Success(albumIdMap) =>
        database.query (
          AUDIO_URI,
          null, null, null, TITLE + " ASC" 
        ) onComplete {
          case Success(table) => { 
            
            table.rowObservable.subscribe( 
              (row) => { 
                val album = albumIdMap.get(row(ALBUM_KEY)) match {
                  case Some(album) => album
                  case None => Album("", null)
                }
                trackListBuffer += Track(row(DATA), row(TITLE), album, row(ARTIST), row(DURATION).toLong)
              },
              (e: Throwable) => Log.d("chakra", "row observable failed: " + e.getMessage),
              () => { promise.success(trackListBuffer.toList) }
            )

          }

          case Failure(t) => Log.d("chakra", "database query failed: " + t.getMessage) 
        }

      case Failure(t) => Log.d("chakra", "albumIdMapFuture failed: " + t.getMessage) 
    }
    

    promise.future

  }

}
