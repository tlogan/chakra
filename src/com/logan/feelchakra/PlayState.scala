package com.logan.feelchakra

import android.util.Log

sealed trait PlayState
case class Playing(startTime: Long) extends PlayState
case object NotPlaying extends PlayState
