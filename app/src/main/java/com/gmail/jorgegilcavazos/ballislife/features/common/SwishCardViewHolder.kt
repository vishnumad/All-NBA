package com.gmail.jorgegilcavazos.ballislife.features.common

import android.support.v7.widget.RecyclerView
import android.view.View
import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.features.model.SwishCard
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.swish_edu_card.view.*

class SwishCardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

  fun bindData(swishCard: SwishCard,
               actionClicks: PublishSubject<Any>,
               gotItClicks: PublishSubject<Any>) = with(itemView) {

    actionBtn.setOnClickListener { actionClicks.onNext(Any()) }
    gotItBtn.setOnClickListener { gotItClicks.onNext(Any()) }

    when (swishCard) {
      SwishCard.HIGHLIGHT_SORTING -> {
        title.text = itemView.context.getString(R.string.swish_card_highlight_sorting_title)
        content.text = itemView.context.getString(R.string.swish_card_highlight_sorting_content)
        actionBtn.text = itemView.context.getString(R.string.swish_card_highlight_sorting_action)
        actionBtn.visibility = View.VISIBLE
      }
      SwishCard.HIGHLIGHT_FAVORITES -> {
        title.text = itemView.context.getString(R.string.swish_card_highlight_favorites_title)
        content.text = itemView.context.getString(R.string.swish_card_highlight_favorites_content)
        actionBtn.text = itemView.context.getString(R.string.swish_card_highlight_favorites_action)
        actionBtn.visibility = View.VISIBLE
      }
      SwishCard.EMPTY_FAVORITE_HIGHLIGHTS -> {
        title.text = itemView.context.getString(R.string.swish_card_favorites_empty_title)
        content.text = itemView.context.getString(R.string.swish_card_favorites_empty_content)
        actionBtn.visibility = View.GONE
      }
    }
  }
}