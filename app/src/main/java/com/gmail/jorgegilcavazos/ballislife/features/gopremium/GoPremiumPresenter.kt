package com.gmail.jorgegilcavazos.ballislife.features.gopremium

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.gmail.jorgegilcavazos.ballislife.analytics.EventLogger
import com.gmail.jorgegilcavazos.ballislife.analytics.SwishEvent
import com.gmail.jorgegilcavazos.ballislife.base.BasePresenter
import com.gmail.jorgegilcavazos.ballislife.common.PlayBillingItems
import com.gmail.jorgegilcavazos.ballislife.common.PlayBillingItems.*
import com.gmail.jorgegilcavazos.ballislife.common.RxPlayBilling
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GoPremiumPresenter @Inject constructor(
    private val rxPlayBilling: RxPlayBilling,
    private val schedulerProvider: BaseSchedulerProvider,
    private val disposable: CompositeDisposable,
    private val eventLogger: EventLogger
) : BasePresenter<GoPremiumView>() {

  override fun attachView(view: GoPremiumView) {
    super.attachView(view)

    rxPlayBilling.initialize()
    rxPlayBilling
        .startConnection()
        .subscribeOn(schedulerProvider.ui())
        .subscribe({
          loadPriceAndCurrencies()
          observePurchases()
        }, {
          view.showServiceUnavailable()
          view.closeActivity()
        })
        .addTo(disposable)

    view.monthlyClicks()
        .debounce(400, TimeUnit.MILLISECONDS)
        .doOnNext { eventLogger.logEvent(SwishEvent.PREMIUM_MONTHLY, null) }
        .flatMap {
          rxPlayBilling.purchaseSubscription(
              PlayBillingItems.SWISH_PREMIUM_MONTHLY_SUB.sku,
              view.activity())
              .toObservable()
        }
        .observeOn(schedulerProvider.ui())
        .subscribe({
          Timber.i("Subscription purchase started successfully")
        }, {
          throw RuntimeException("Subscription purchase failed for skuId: "
              + PlayBillingItems.SWISH_PREMIUM_MONTHLY_SUB.sku)
        })
        .addTo(disposable)

    view.yearlyClicks()
        .debounce(400, TimeUnit.MILLISECONDS)
        .doOnNext { eventLogger.logEvent(SwishEvent.PREMIUM_YEARLY, null) }
        .flatMap {
          rxPlayBilling.purchaseSubscription(
              PlayBillingItems.SWISH_PREMIUM_YEARLY_SUB.sku,
              view.activity())
              .toObservable()
        }
        .observeOn(schedulerProvider.ui())
        .subscribe({
          Timber.i("Subscription purchase started successfully")
        }, {
          throw RuntimeException("Subscription purchase failed for skuId: "
              + PlayBillingItems.SWISH_PREMIUM_YEARLY_SUB.sku)
        })
        .addTo(disposable)

    view.lifetimeClicks()
        .debounce(400, TimeUnit.MILLISECONDS)
        .doOnNext { eventLogger.logEvent(SwishEvent.PREMIUM_LIFETIME, null) }
        .flatMap {
          rxPlayBilling.purchaseItem(
              PlayBillingItems.SWISH_PREMIUM_LIFETIME_IAP.sku,
              view.activity())
              .toObservable()
        }
        .observeOn(schedulerProvider.ui())
        .subscribe({
          Timber.i("Lifetime purchase started successfully")
        }, {
          throw RuntimeException("Lifetime purchase failed for skuId: "
              + PlayBillingItems.SWISH_PREMIUM_LIFETIME_IAP.sku)
        })
        .addTo(disposable)
  }

  override fun detachView() {
    disposable.clear()
    super.detachView()
  }

  private fun loadPriceAndCurrencies() {
    Single.zip(
        rxPlayBilling.skuDetails(
            skuIds = listOf(
                SWISH_PREMIUM_MONTHLY_SUB.sku,
                SWISH_PREMIUM_YEARLY_SUB.sku),
            skuType = BillingClient.SkuType.SUBS
        ),
        rxPlayBilling.skuDetails(
            skuIds = listOf(SWISH_PREMIUM_LIFETIME_IAP.sku),
            skuType = BillingClient.SkuType.INAPP
        ),
        BiFunction { subsDetails: List<SkuDetails>, iapDetails: List<SkuDetails> ->
          subsDetails + iapDetails
        })
        .subscribeOn(schedulerProvider.io())
        .observeOn(schedulerProvider.ui())
        .subscribe({ skusDetails ->
          skusDetails.forEach { details ->
            when (details.sku) {
              SWISH_PREMIUM_MONTHLY_SUB.sku -> view.setMonthlyPrice(details.price)
              SWISH_PREMIUM_YEARLY_SUB.sku -> view.setYearlyPrice(details.price)
              SWISH_PREMIUM_LIFETIME_IAP.sku -> view.setLifetimePrice(details.price)
              else -> throw IllegalArgumentException("Unexpected sku ${details.sku}")
            }
          }
        }, {
          // SKU details fetch failed.
        })
        .addTo(disposable)
  }

  private fun observePurchases() {
    rxPlayBilling.purchaseUpdates()
        .observeOn(schedulerProvider.ui())
        .subscribe { update ->
          if (update.responseCode == BillingClient.BillingResponse.OK && update.purchases != null) {
            update.purchases.forEach { purchase ->
              Timber.i("Purchase update received for skuId: %s", purchase.sku)
              view.showSubscriptionActivated()
              view.closeActivity()
            }
          } else {
            Timber.i("Purchase update received with code: %d", update.responseCode)
          }
        }
        .addTo(disposable)
  }
}