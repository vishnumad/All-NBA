package com.gmail.jorgegilcavazos.ballislife.features.highlights

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight
import com.gmail.jorgegilcavazos.ballislife.features.model.HighlightViewType
import com.gmail.jorgegilcavazos.ballislife.util.StringUtils
import com.jakewharton.rxrelay2.PublishRelay
import com.squareup.picasso.Picasso
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.row_highlight_small.view.*

/**
 * View holder for a highlight.
 */
class HighlightHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bindData(isPremium: Boolean,
               contentViewType: HighlightViewType,
               highlight: Highlight,
               viewClickSubject: PublishSubject<Highlight>,
               shareClickSubject: PublishSubject<Highlight>,
               favoriteClicks: PublishRelay<Highlight>,
               submissionClickSubject: PublishSubject<Highlight>) = with(itemView) {
    title.text = highlight.title

    var thumbnailAvailable = true
    if (!StringUtils.isNullOrEmpty(highlight.hdThumbnail)) {
      Picasso.with(itemView.context).load(highlight.hdThumbnail).into(thumbnail)
    } else if (!StringUtils.isNullOrEmpty(highlight.thumbnail)) {
      Picasso.with(itemView.context).load(highlight.thumbnail).into(thumbnail)
    } else {
      Picasso.with(itemView.context).cancelRequest(thumbnail)
      thumbnail.setImageDrawable(null)
      thumbnailAvailable = false
    }

    // Set bball background visibility only for list type view.
    thumbnailUnavailable.visibility = if (contentViewType === HighlightViewType.SMALL
        && !thumbnailAvailable) {
      VISIBLE
    } else {
      GONE
    }

    container.setOnClickListener { viewClickSubject.onNext(highlight) }

    viewThreadText.setOnClickListener { submissionClickSubject.onNext(highlight) }

    shareBtn.setOnClickListener { shareClickSubject.onNext(highlight) }

    // Send favorite click events only if user is premium.
    container.setOnLongClickListener{
      if (isPremium) {
        favoriteClicks.accept(highlight)
      }
      isPremium
    }
  }

}