package com.gmail.jorgegilcavazos.ballislife.data.premium

import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.common.PlayBillingItems
import com.gmail.jorgegilcavazos.ballislife.common.RxPlayBilling
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumService @Inject constructor(
    private val context: Context,
    private val rxPlayBilling: RxPlayBilling,
    private val localRepository: LocalRepository,
    schedulerProvider: BaseSchedulerProvider,
    disposable: CompositeDisposable
) {

  private val isPremiumUpdates = PublishRelay.create<Boolean>()

  init {
    rxPlayBilling.initialize()
    rxPlayBilling
        .startConnection()
        .doOnComplete { isPremiumUpdates.accept(isPremium()) }
        .andThen(rxPlayBilling.purchaseUpdates())
        .subscribeOn(schedulerProvider.ui())
        .subscribe({ purchaseUpdate ->
          if (purchaseUpdate.responseCode == BillingClient.BillingResponse.OK
              && purchaseUpdate.purchases != null) {
            purchaseUpdate.purchases.forEach { purchase ->
              when (purchase.sku) {
                PlayBillingItems.SWISH_PREMIUM_MONTHLY_SUB.sku,
                PlayBillingItems.SWISH_PREMIUM_YEARLY_SUB.sku,
                PlayBillingItems.SWISH_PREMIUM_LIFETIME_IAP.sku -> {
                  Toast.makeText(context, R.string.purchase_complete, Toast.LENGTH_SHORT).show()
                }
              }
            }
          }
        }, {

        })
        .addTo(disposable)
  }

  fun isPremium(): Boolean {
    return premiumPurchased() || localRepository.isUserWhitelisted
  }

  fun isPremiumUpdates(): Observable<Boolean> = isPremiumUpdates

  private fun premiumPurchased(): Boolean {
    if (rxPlayBilling.isReady()) {
      return Single.zip(
          rxPlayBilling.queryPurchases(BillingClient.SkuType.SUBS),
          rxPlayBilling.queryPurchases(BillingClient.SkuType.INAPP),
          BiFunction { subs: List<Purchase>, iaps: List<Purchase> -> subs + iaps })
          .blockingGet()
          .any { purchase ->
            purchase.sku == PlayBillingItems.SWISH_PREMIUM_MONTHLY_SUB.sku
                || purchase.sku == PlayBillingItems.SWISH_PREMIUM_YEARLY_SUB.sku
                || purchase.sku == PlayBillingItems.SWISH_PREMIUM_LIFETIME_IAP.sku
          }
    }
    // Unlock premium features while the client connects.
    return true
  }

  companion object {
    private const val USERS_COLLECTION = "users"
  }
}