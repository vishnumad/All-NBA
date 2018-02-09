package com.gmail.jorgegilcavazos.ballislife.analytics

enum class SwishEvent(val eventName: String) {
  STREAM("stream"),
  DELAY_COMMENTS("delay_comments"),
  GO_PREMIUM("go_premium"),
  PREMIUM_MONTHLY("premium_monthly_click"),
  PREMIUM_YEARLY("premium_yearly_click"),
  PREMIUM_LIFETIME("premium_lifetime_click")
}