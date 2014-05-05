package com.logan.feelchakra

import android.util.Log

object AudioReader {

  def apply(path: String) = {

    Observable.create({
      observer: Observer[Array[Byte]] => {
        try {

          val file = new File(path)
          val fileInput = new BufferedInputStream(new FileInputStream(file))
          val audioBuffer = new Array[Byte](1024)
          var streamAlive = true

          while (streamAlive) {
            val len = fileInput.read(audioBuffer)
            if (len != -1) {
              observer.onNext(audioBuffer.slice(0, len))
            } else {
              streamAlive = false
            }
          }
          observer.onCompleted()

        } catch {
          case e: Throwable =>
            Log.d("chakra", "CATCHING ERROR!!!" + e)
            e.printStackTrace()
            observer.onError(e)
        }

        Subscription() 

      }
    }).subscribeOn(NewThreadScheduler())

  }

}
