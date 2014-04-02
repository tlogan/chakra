package com.logan.feelchakra

class TrackLayout(context: Context) extends LinearLayout(context) {

   setOrientation(VERTICAL)
   setLayoutParams {
     new LLLayoutParams(MATCH_PARENT, 0, 1)
   }

   def setTrackOption(trackOption: Option[Track]): Unit = {
     removeAllViews()
     trackOption match {
       case Some(track) =>
         //update the track info
         List(track.title, track.album, track.artist) foreach {
           (term: String) => { 
             addView {
               new TextView(context) {
                 setText(term)
               }
             }
           } 
         }
       case None => {}
     }
   }


}
