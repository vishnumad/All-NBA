package com.gmail.jorgegilcavazos.ballislife.features.games


import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker

import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil
import kotlinx.android.synthetic.main.fragment_games_home.*
import java.util.*


/**
 * Main fragment for the games screen. Contains a view pager for individual pages of day games.
 */
class GamesHomeFragment : Fragment(), DatePickerDialog.OnDateSetListener {

  companion object {
    fun newInstance() = GamesHomeFragment()
  }

  private lateinit var adapter: GamesHomePagerAdapter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_games_home, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    adapter = GamesHomePagerAdapter(activity, childFragmentManager)
    viewPager.adapter = adapter
    viewPager.currentItem = adapter.count / 2

    setNavigatorText()

    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrollStateChanged(state: Int) {
      }

      override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
      }

      override fun onPageSelected(position: Int) {
        setNavigatorText()
      }
    })

    navigatorLeftBtn.setOnClickListener { viewPager.currentItem = viewPager.currentItem - 1 }
    navigatorRightBtn.setOnClickListener { viewPager.currentItem = viewPager.currentItem + 1 }
    navigatorText.setOnClickListener {
      val today = Calendar.getInstance()
      val datePicker = DatePickerDialog(
          activity,
          this,
          today.get(Calendar.YEAR),
          today.get(Calendar.MONTH),
          today.get(Calendar.DAY_OF_MONTH)
      )
      // First available game in backend.
      datePicker.datePicker.minDate = 1507204822000L
      datePicker.show()
    }
  }

  override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, day: Int) {
    val date = Calendar.getInstance()
    date.set(Calendar.DAY_OF_MONTH, day)
    date.set(Calendar.MONTH, month)
    date.set(Calendar.YEAR, year)

    // Move to selected date.
    viewPager.currentItem = adapter.getPositionForDate(date.timeInMillis)
  }

  private fun setNavigatorText() {
    navigatorText.text = DateFormatUtil.formatNavigatorDate(
        Date(adapter.getDateForPosition(viewPager.currentItem)))
  }
}
