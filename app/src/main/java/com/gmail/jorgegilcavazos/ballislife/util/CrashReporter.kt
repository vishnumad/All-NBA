package com.gmail.jorgegilcavazos.ballislife.util

interface CrashReporter {

  fun log(message: String)

  fun report(e: Throwable)
}