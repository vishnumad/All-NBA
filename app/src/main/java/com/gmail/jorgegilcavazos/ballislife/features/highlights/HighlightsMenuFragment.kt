package com.gmail.jorgegilcavazos.ballislife.features.highlights


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gmail.jorgegilcavazos.ballislife.R
import kotlinx.android.synthetic.main.fragment_highlights_menu.*

/**
 * Highlights fragment that contains a viewpager with the regular highlights feed and the favorites
 * section.
 */
class HighlightsMenuFragment : Fragment() {

  companion object {
    fun newInstance() = HighlightsMenuFragment()
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    // Inflate the layout for this fragment
    return inflater!!.inflate(R.layout.fragment_highlights_menu, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewPager.adapter = HighlightsPagerAdapter(activity, childFragmentManager)
    tabLayout.setupWithViewPager(viewPager)
  }

}
