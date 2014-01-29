package guava.android

import android.database.Cursor

import rx.lang.scala.Observer
import rx.lang.scala.Observable
import rx.lang.scala.Subscription

import scala.collection.mutable.MapBuilder
import scala.collection.immutable.HashMap


class Table(cursor: Cursor) {

  type Row = HashMap[String, String]

  object Row {
    def apply(cursor: Cursor): Row = {

      val builder = new MapBuilder[String, String, Row](new Row())
      cursor.getColumnNames() foreach { columnName =>
        builder += columnName -> cursor.getString(cursor.getColumnIndexOrThrow(columnName))
      }
      builder.result()

    }

  }

  val rowObservable = Observable.create[Row]({
    observer => {
      while (cursor.moveToNext()) {
        observer.onNext(Row(cursor))
      }
      cursor.close()
      observer.onCompleted()
      Subscription() 
    }
  })
 
  def getColumnIndex(name: String) = cursor.getColumnIndex(name: String)
  
  def getColumnName(index: Int) = cursor.getColumnName(index: Int)
  
}
