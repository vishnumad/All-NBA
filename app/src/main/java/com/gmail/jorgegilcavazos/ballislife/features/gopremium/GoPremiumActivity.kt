package com.gmail.jorgegilcavazos.ballislife.features.gopremium

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication
import com.gmail.jorgegilcavazos.ballislife.features.main.BaseNoActionBarActivity
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_go_premium.benefitContainer
import kotlinx.android.synthetic.main.activity_go_premium.closeBtn
import kotlinx.android.synthetic.main.activity_go_premium.priceOptionLifetime
import kotlinx.android.synthetic.main.activity_go_premium.priceOptionMonthly
import kotlinx.android.synthetic.main.activity_go_premium.priceOptionYearly
import kotlinx.android.synthetic.main.sub_price_option.view.priceOptionHeaderTitle
import kotlinx.android.synthetic.main.sub_price_option.view.priceOptionPrice
import kotlinx.android.synthetic.main.sub_price_option.view.priceOptionText
import javax.inject.Inject


class GoPremiumActivity : BaseNoActionBarActivity(), GoPremiumView {

  @Inject lateinit var presenter: GoPremiumPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_go_premium)

    closeBtn.setOnClickListener { onBackPressed() }

    val benefits = arrayOf(
        "• 100% ad free. Forever.",
        "• Stream live game threads",
        "• Add a delay to game threads to avoid spoilers",
        "• Save your favorite highlights",
        "• Sort highlights by popularity",
        "• Hide scores (No spoilers)",
        "• Subscribe to triple-double, quadruple-double, and 5x5 alerts",
        "• More coming soon!")

    benefits.forEach {
      val benefitText = LayoutInflater.from(this)
          .inflate(R.layout.premium_benefit_text_view, null, false) as TextView
      benefitText.text = it
      benefitContainer.addView(benefitText)
    }

    priceOptionMonthly.apply {
      priceOptionHeaderTitle.text = getString(R.string.monthly_header)
      priceOptionText.text = getString(R.string.monthly)
    }

    priceOptionYearly.apply {
      priceOptionHeaderTitle.text = getString(R.string.yearly_header)
      priceOptionText.text = getString(R.string.yearly)
    }

    priceOptionLifetime.apply {
      priceOptionHeaderTitle.text = getString(R.string.lifetime_header)
      priceOptionText.text = getString(R.string.lifetime)
    }

    presenter.attachView(this)
  }

  override fun onDestroy() {
    presenter.detachView()
    super.onDestroy()
  }

  override fun injectAppComponent() {
    BallIsLifeApplication.getAppComponent().inject(this)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        onBackPressed()
        return true
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun showServiceUnavailable() {
    Toast.makeText(this, "Service unavailable", Toast.LENGTH_SHORT).show()
  }

  override fun closeActivity() {
    finish()
  }

  override fun setMonthlyPrice(price: String) {
    priceOptionMonthly.priceOptionPrice.text = price
  }

  override fun setYearlyPrice(price: String) {
    priceOptionYearly.priceOptionPrice.text = price
  }

  override fun setLifetimePrice(price: String) {
    priceOptionLifetime.priceOptionPrice.text = price
  }

  override fun monthlyClicks(): Observable<Unit> {
    return priceOptionMonthly.clicks()
  }

  override fun yearlyClicks(): Observable<Unit> {
    return priceOptionYearly.clicks()
  }

  override fun lifetimeClicks(): Observable<Unit> {
    return priceOptionLifetime.clicks()
  }

  override fun activity(): Activity {
    return this
  }

  override fun showSubscriptionActivated() {
    Toast.makeText(activity(), R.string.purchase_complete, Toast.LENGTH_SHORT).show()
  }
}
