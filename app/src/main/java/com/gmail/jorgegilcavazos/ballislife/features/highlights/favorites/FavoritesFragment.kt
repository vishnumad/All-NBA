package com.gmail.jorgegilcavazos.ballislife.features.highlights.favorites


import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository

import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication
import com.gmail.jorgegilcavazos.ballislife.features.common.EndlessRecyclerViewScrollListener
import com.gmail.jorgegilcavazos.ballislife.features.highlights.HighlightAdapter
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight
import com.gmail.jorgegilcavazos.ballislife.features.model.HighlightViewType
import com.gmail.jorgegilcavazos.ballislife.util.Constants
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_favorites.*
import javax.inject.Inject


/**
 * Fragment used to show a list of highlights that a user has saved as favorites.
 */
class FavoritesFragment : Fragment(), FavoritesView {

  @Inject lateinit var presenter: FavoritesPresenter
  @Inject lateinit var localRepository: LocalRepository

  private var listState: Parcelable? = null
  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var highlightAdapter: HighlightAdapter
  private lateinit var endlessScroller: EndlessRecyclerViewScrollListener
  private lateinit var viewType: HighlightViewType

  private val favoriteDeletions = PublishRelay.create<Highlight>()

  companion object {
    val LIST_STATE = "listState"

    fun newInstance() = FavoritesFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    BallIsLifeApplication.getAppComponent().inject(this)

    viewType = localRepository.favoriteHighlightViewType
    linearLayoutManager = LinearLayoutManager(activity)
    highlightAdapter = HighlightAdapter(activity, mutableListOf(), viewType, false, isPremium())
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(R.layout.fragment_favorites, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recyclerView.layoutManager = linearLayoutManager
    recyclerView.adapter = highlightAdapter

    endlessScroller = object : EndlessRecyclerViewScrollListener(linearLayoutManager) {
      override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
        presenter.loadMore()
      }
    }

    recyclerView.addOnScrollListener(endlessScroller)

    presenter.attachView(this)
    presenter.loadMore()
  }

  override fun onViewStateRestored(savedInstanceState: Bundle?) {
    super.onViewStateRestored(savedInstanceState)
    listState = savedInstanceState?.getParcelable(LIST_STATE)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    listState = linearLayoutManager.onSaveInstanceState()
    outState.putParcelable(LIST_STATE, listState)
  }

  override fun onDestroy() {
    presenter.detachView()
    super.onDestroy()
  }

  override fun addHighlight(highlight: Highlight, addToTop: Boolean) {
    if (addToTop) {
      highlightAdapter.addHighlightToTop(highlight)
      recyclerView.scrollToPosition(0)
    } else {
      highlightAdapter.addHighlight(highlight)
    }
  }

  override fun removeHighlight(highlight: Highlight) {
    highlightAdapter.removeHighlight(highlight)
  }

  override fun favoriteClicks(): Observable<Highlight> = highlightAdapter.favoriteClicks

  override fun showRemoveFromFavoritesConfirmation(highlight: Highlight) {
    MaterialDialog.Builder(activity)
        .title(getString(R.string.fav_deletion_title))
        .content(getString(R.string.fav_deletion_content))
        .positiveText(getString(R.string.fav_deletion_positive))
        .negativeText(getString(R.string.fav_deletion_negative))
        .onPositive { _, _ ->  favoriteDeletions.accept(highlight)}
        .show()
  }

  override fun favoriteDeletions(): Observable<Highlight> = favoriteDeletions

  override fun isPremium(): Boolean {
    val bp = (activity.application as BallIsLifeApplication).billingProcessor
    return bp.isPurchased(Constants.PREMIUM_PRODUCT_ID) || localRepository.isUserWhitelisted
  }
}
