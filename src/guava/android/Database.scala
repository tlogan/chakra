package guava.android

import android.database.Cursor
import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.None

class Database(context: Context) {

  private val resolver: ContentResolver = context.getContentResolver() 
  
  def query(
    uri: Uri, 
    projection: Array[String], selection: String, 
    selectionArgs: Array[String], sortOrder: String
  ): Future[Table] = {

    Future {
      val cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);
      new Table(cursor)
    }

  }
  
}
