package com.logan.feelchakra

sealed trait PlayState
case class Playing(startPos: Int, startTime: Long) extends PlayState
case object NotPlaying extends PlayState
