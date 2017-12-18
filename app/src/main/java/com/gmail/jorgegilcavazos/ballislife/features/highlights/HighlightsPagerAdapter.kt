package com.gmail.jorgegilcavazos.ballislife.features.highlights

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.gmail.jorgegilcavazos.ballislife.R

/**
 * Pager adapter for the highlights section. Hosts the regular highlights feed and the favorites
 * section.
 */
class HighlightsPagerAdapter(
    val context: Context,
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {

  companion object {
    val ALL_HIGHLIGHTS = 0
    val FAVORITE_HIGHLIGHTS = 1
  }

  override fun getItem(position: Int): Fragment = when (position) {
    ALL_HIGHLIGHTS -> HighlightsFragment.newInstance()
    FAVORITE_HIGHLIGHTS -> HighlightsFragment.newInstance()
    else -> throw IllegalArgumentException("Unsupported position: " + position)
  }

  override fun getCount(): Int = 2

  override fun getPageTitle(position: Int): CharSequence = when (position) {
    ALL_HIGHLIGHTS -> context.getString(R.string.home)
    FAVORITE_HIGHLIGHTS -> context.getString(R.string.favorites)
    else -> throw IllegalArgumentException("Unsupported position: " + position)
  }
}