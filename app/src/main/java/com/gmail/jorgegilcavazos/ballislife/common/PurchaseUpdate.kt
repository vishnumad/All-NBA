package com.gmail.jorgegilcavazos.ballislife.common

import com.android.billingclient.api.Purchase

/**
 * Wrapper for purchase updates of the Play Billing client.
 */
data class PurchaseUpdate(val responseCode: Int, val purchases: List<Purchase>?)
