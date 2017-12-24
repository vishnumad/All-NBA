package com.gmail.jorgegilcavazos.ballislife.data.firebase.remoteconfig

interface RemoteConfig {
  fun getBoolean(s: String): Boolean
}