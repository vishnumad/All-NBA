package com.gmail.jorgegilcavazos.ballislife.util

interface ErrorHandler {
  fun handleError(t: Throwable): Int
}