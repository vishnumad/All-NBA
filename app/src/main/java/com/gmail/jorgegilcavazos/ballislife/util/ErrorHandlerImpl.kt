package com.gmail.jorgegilcavazos.ballislife.util

import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

class ErrorHandlerImpl @Inject constructor(
    private val crashReporter: CrashReporter
) : ErrorHandler {

  companion object {
    private const val SOCKET_TIMEOUT = 700
  }

  override fun handleError(t: Throwable): Int {
    return when (t) {
      is HttpException -> t.code()
      is SocketTimeoutException -> SOCKET_TIMEOUT
      else -> {
        crashReporter.log("Unknown error")
        crashReporter.report(t)
        -1
      }
    }
  }
}