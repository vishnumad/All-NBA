package com.gmail.jorgegilcavazos.ballislife.common
/**
 * Available products in the Play Store for Swish.
 */
enum class PlayBillingItems(val sku: String) {
  TEST_STATIC_PURCHASED("android.test.purchased"),
  TEST_STATIC_CANCELED("android.test.canceled"),
  TEST_STATIC_REFUNDED("android.test.refunded"),
  TEST_STATIC_ITEM_UNAVAILABLE("android.test.item_unavailable"),

  SWISH_PREMIUM_LIFETIME_IAP("premium"),
  SWISH_PREMIUM_YEARLY_SUB("swish.premium.yearly"),
  SWISH_PREMIUM_MONTHLY_SUB("swish.premium.monthly")
}
