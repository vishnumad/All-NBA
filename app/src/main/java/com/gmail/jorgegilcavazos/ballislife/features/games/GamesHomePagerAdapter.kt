package com.gmail.jorgegilcavazos.ballislife.features.games

import android.content.Context
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import java.util.*

/**
 * Pager Adapter for the Games home screen. Paginates between days of games.
 */
class GamesHomePagerAdapter(
    val context: Context,
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm) {

  companion object {
    // Number of pages available for pagination. The center page corresponds to today so there
    // would be 250 pages (days) to the left and 250 pages (days) to the right.
    private val NUM_PAGES = 501
  }

  override fun getItem(position: Int) = GamesFragment.newInstance(getDateForPosition(position))

  override fun getCount() = NUM_PAGES

  /**
   * Returns the date of a given page of the adapter. E.g. if the position is the center page
   * (NUM_PAGES / 2) then the date returned is today, if the position is the center page + 1 then
   * the date returned is tomorrow.
   */
  fun getDateForPosition(position: Int): Long {
    val todayPage = NUM_PAGES / 2
    val date = Calendar.getInstance()

    return if (position == todayPage) {
      date.timeInMillis
    } else {
      date.add(Calendar.DAY_OF_YEAR, -1 * (todayPage - position))
      date.timeInMillis
    }
  }

  /**
   * Returns the position in the adapter that a given page corresponds to. E.g. if the date is
   * today then the position should be the page in the center (NUM_PAGES / 2), if the date is
   * yesterday then the position is the center most page - 1.
   */
  fun getPositionForDate(date: Long): Int {
    val selectedDate = Calendar.getInstance()
    selectedDate.timeInMillis = date
    val today = Calendar.getInstance()

    val dayDiff = getDayDifferenceBetweenDates(today, selectedDate)

    return NUM_PAGES / 2 - dayDiff
  }

  private fun getDayDifferenceBetweenDates(today: Calendar, date: Calendar): Int {
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)

    date.set(Calendar.HOUR_OF_DAY, 0)
    date.set(Calendar.MINUTE, 0)
    date.set(Calendar.SECOND, 0)

    val millisDiff = today.timeInMillis - date.timeInMillis
    return (millisDiff / (1000 * 60 * 60 * 24)).toInt()
  }
}