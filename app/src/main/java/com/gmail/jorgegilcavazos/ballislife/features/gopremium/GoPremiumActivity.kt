package com.gmail.jorgegilcavazos.ballislife.features.gopremium

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.TextView
import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication
import com.gmail.jorgegilcavazos.ballislife.features.main.BaseActionBarActivity
import com.gmail.jorgegilcavazos.ballislife.util.Constants
import kotlinx.android.synthetic.main.activity_go_premium.*


class GoPremiumActivity : BaseActionBarActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_go_premium)

    setTitle(R.string.go_premium)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val benefits = arrayOf(
        "• Stream live game threads",
        "• Add a delay to game threads to avoid spoilers",
        "• Save your favorite highlights",
        "• Sort highlights by popularity",
        "• Hide scores (No spoilers)",
        "• Subscribe to triple-double, quadruple-double, and 5x5 alerts",
        "• 100% ad free. Forever.",
        "• More coming soon!")

    benefits.forEach {
      val benefitText = LayoutInflater.from(this)
          .inflate(R.layout.premium_benefit_text_view, null, false) as TextView
      benefitText.text = it
      benefitContainer.addView(benefitText)
    }

    goPremiumBtn.setOnClickListener {
      (application as BallIsLifeApplication).billingProcessor
          .purchase(this, Constants.PREMIUM_PRODUCT_ID)
    }
  }

  override fun injectAppComponent() {
    BallIsLifeApplication.getAppComponent().inject(this)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    if (!(application as BallIsLifeApplication).billingProcessor
        .handleActivityResult(requestCode, resultCode, data)) {
      super.onActivityResult(requestCode, resultCode, data)
    }
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
}
