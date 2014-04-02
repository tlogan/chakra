package com.logan.feelchakra

import android.util.Log
import scala.concurrent.ExecutionContext.Implicits.global
 
object TrackList {

  def apply(database: Database): Future[List[Track]] = {

    val promise = Promise[List[Track]]()
    val trackListBuffer: ListBuffer[Track] = new ListBuffer[Track]() 
    
    database.query (
      AUDIO_URI,
      null, null, null, null
    ) onComplete {
      case Success(table) => { 
        
        table.rowObservable map { row =>  
          Track(row(DATA), row(TITLE), row(ALBUM), row(ARTIST))
        } subscribe ( 
          (track: Track) => { trackListBuffer += track },
          (e: Throwable) => Log.d("chakra", "row observable failed: " + e.getMessage),
          () => { promise.success(trackListBuffer.toList) }
        )
      }

      case Failure(t) => Log.d("chakra", "database query failed: " + t.getMessage) 
    }

    promise.future

  }

}
