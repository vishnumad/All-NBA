package com.gmail.jorgegilcavazos.ballislife.features.gopremium

import android.app.Activity
import io.reactivex.Observable

interface GoPremiumView {

  fun showServiceUnavailable()

  fun closeActivity()

  fun setMonthlyPrice(price: String)

  fun setYearlyPrice(price: String)

  fun setLifetimePrice(price: String)

  fun monthlyClicks(): Observable<Unit>

  fun yearlyClicks(): Observable<Unit>

  fun lifetimeClicks(): Observable<Unit>

  fun activity(): Activity

  fun showSubscriptionActivated()
}