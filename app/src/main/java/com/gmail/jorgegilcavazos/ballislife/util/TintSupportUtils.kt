package com.gmail.jorgegilcavazos.ballislife.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat

/**
 * Sets the tint list of the given drawable to the specified color.
 */
fun setDrawableTintList(context: Context, drawable: Drawable, color: Int) {
  DrawableCompat.setTintList(
      DrawableCompat.wrap(drawable).mutate(),
      ColorStateList.valueOf(ContextCompat.getColor(context, color)))
}
