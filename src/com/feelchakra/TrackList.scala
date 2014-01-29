
package com.feelchakra

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem
import akka.dispatch.RequiresMessageQueue
import akka.dispatch.UnboundedMessageQueueSemantics

import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.Observer
import scala.concurrent.Future
import scala.concurrent.Promise
import android.provider.MediaStore 

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ListBuffer

import android.os.Handler

import guava.android.Database
import guava.android.Table

import android.util.Log 
import scala.util.{Success,Failure}
 
object TrackList {

  def apply(database: Database): Future[List[Track]] = {

    val promise = Promise[List[Track]]()
    val trackListBuffer: ListBuffer[Track] = new ListBuffer[Track]() 
    
    database.query (
      MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
      null, null, null, null
    ) onComplete {
      case Success(table) => { 
        
        table.rowObservable map { row =>  
          import MediaStore.MediaColumns._
          import MediaStore.Audio.AudioColumns._
          Track(row(DATA), row(TITLE), row(ALBUM), row(ARTIST))
        } subscribe ( 
          (track: Track) => { trackListBuffer += track },
          (e: Throwable) => Log.d("row observable", "failed: " + e.getMessage),
          () => { promise.success(trackListBuffer.toList) }
        )
      }

      case Failure(t) => Log.d("database query", "failed: " + t.getMessage) 
    }

    promise.future

  }

}
