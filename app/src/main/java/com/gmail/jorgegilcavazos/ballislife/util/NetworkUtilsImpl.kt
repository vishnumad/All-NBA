package com.gmail.jorgegilcavazos.ballislife.util

import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication
import javax.inject.Inject

class NetworkUtilsImpl @Inject constructor() : NetworkUtils {
  override fun isNetworkAvailable(): Boolean {
    val context = BallIsLifeApplication.getAppContext() ?: return false
    val connectivityMgr = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityMgr.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
  }
}