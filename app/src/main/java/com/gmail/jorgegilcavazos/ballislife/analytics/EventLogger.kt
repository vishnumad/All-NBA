package com.gmail.jorgegilcavazos.ballislife.analytics

import android.app.Activity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventLogger @Inject constructor(private val firebaseAnalytics: FirebaseAnalytics) {

  fun logEvent(event: SwishEvent, params: Bundle?) {
    firebaseAnalytics.logEvent(event.eventName, params)
  }

  fun setCurrentScreen(activity: Activity, swishScreen: SwishScreen) {
    firebaseAnalytics.setCurrentScreen(activity, swishScreen.screenName, null)
  }
}