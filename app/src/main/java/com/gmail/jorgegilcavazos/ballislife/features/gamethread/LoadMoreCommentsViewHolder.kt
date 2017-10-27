package com.gmail.jorgegilcavazos.ballislife.features.gamethread

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.features.model.CommentItem
import com.gmail.jorgegilcavazos.ballislife.features.model.ThreadItem
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.layout_load_more_comments.view.*

class LoadMoreCommentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

	fun bindData(threadItem: ThreadItem,
							 loadMoreComments: PublishSubject<CommentItem>) = with(itemView) {
		loadMoreText.setOnClickListener {
			threadItem.loading = true
			setLoadingIndicator(true)
			loadMoreComments.onNext(threadItem.commentItem!! /* Should never be null */)
		}

		setLoadingIndicator(threadItem.loading)
		setBackgroundAndPadding(threadItem.depth)
	}

	private fun setLoadingIndicator(loading: Boolean) = with(itemView) {
		if (loading) {
			loadMoreText.setText(R.string.load_more_comments_loading)
		} else {
			loadMoreText.setText(R.string.load_more_comments)
		}
	}

	private fun setBackgroundAndPadding(depth: Int) = with(itemView) {
		val paddingDp = 5
		val scale = itemView.context.resources.displayMetrics.density
		val paddingPx = (paddingDp * scale + 0.5f).toInt()

		// Add color if it is not a top-level comment.
		if (depth > 1) {
			val depthFromZero = depth - 2
			val res = depthFromZero % 5
			when (res) {
				0 -> innerLayout.setBackgroundResource(R.drawable.borderblue)
				1 -> innerLayout.setBackgroundResource(R.drawable.bordergreen)
				2 -> innerLayout.setBackgroundResource(R.drawable.borderbrown)
				3 -> innerLayout.setBackgroundResource(R.drawable.borderorange)
				4 -> innerLayout.setBackgroundResource(R.drawable.borderred)
			}
		} else {
			innerLayout.setBackgroundColor(ContextCompat.getColor(itemView.context, R
					.color.commentBgLight))
		}
		// Add padding depending on level.
		itemView.setPadding(paddingPx * (depth - 2), 0, 0, 0)
	}

}