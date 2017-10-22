package com.gmail.jorgegilcavazos.ballislife.util

interface CrashReporter {
  fun report(e: Throwable)
}