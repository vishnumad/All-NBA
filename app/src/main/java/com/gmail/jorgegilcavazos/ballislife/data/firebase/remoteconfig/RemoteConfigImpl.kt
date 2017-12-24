package com.gmail.jorgegilcavazos.ballislife.data.firebase.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class RemoteConfigImpl @Inject constructor() : RemoteConfig {
  override fun getBoolean(s: String): Boolean {
    return FirebaseRemoteConfig.getInstance().getBoolean(s)
  }
}