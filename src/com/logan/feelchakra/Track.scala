package com.logan.feelchakra

case class Track(path: String, title: String, album: Album, artist: String, duration: Long) 

case class Album(title: String, coverArt: Drawable) 

