package com.gmail.jorgegilcavazos.ballislife.features.posts

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.features.common.OnSubmissionClickListener
import com.gmail.jorgegilcavazos.ballislife.features.model.NBASubChips
import com.gmail.jorgegilcavazos.ballislife.features.model.SubscriberCount
import com.gmail.jorgegilcavazos.ballislife.features.model.SwishTheme
import com.gmail.jorgegilcavazos.ballislife.util.RedditUtils
import kotlinx.android.synthetic.main.rnba_header_layout.view.*

class HeaderViewHolder(itemView: View, theme: SwishTheme) : RecyclerView.ViewHolder(itemView) {

  init {
    when (theme) {
      SwishTheme.DARK -> {
        itemView.chipDailyLocker.setChipBackgroundColor(
            ContextCompat.getColor(itemView.context, R.color.chipBackgroundDark))
        itemView.chipFreeTalkFriday.setChipBackgroundColor(
            ContextCompat.getColor(itemView.context, R.color.chipBackgroundDark))
        itemView.chipTrashTalk.setChipBackgroundColor(
            ContextCompat.getColor(itemView.context, R.color.chipBackgroundDark))
        itemView.chipPowerRankings.setChipBackgroundColor(
            ContextCompat.getColor(itemView.context, R.color.chipBackgroundDark))
      }
      SwishTheme.LIGHT -> {
        itemView.chipDailyLocker.setChipBackgroundColor(
            ContextCompat.getColor(itemView.context, R.color.daily_locker_room))
        itemView.chipFreeTalkFriday.setChipBackgroundColor(
            ContextCompat.getColor(itemView.context, R.color.free_talk_friday))
        itemView.chipTrashTalk.setChipBackgroundColor(
            ContextCompat.getColor(itemView.context, R.color.trash_talk))
        itemView.chipPowerRankings.setChipBackgroundColor(
            ContextCompat.getColor(itemView.context, R.color.power_rankings))
      }
    }
  }

  fun bindData(subscriberCount: SubscriberCount?, subreddit: String,
               nbaSubChips: NBASubChips?, submissionClickListener: OnSubmissionClickListener)
      = with(itemView) {

    ivLogo.setImageResource(RedditUtils.getTeamSnoo(subreddit))
    tvSubreddit.text = "r/" + subreddit

    if (subscriberCount != null) {
      val subscribers = subscriberCount.subscribers.toString()
      val activeUsers = subscriberCount.activeUsers.toString()

      tvSubscribers.visibility = View.VISIBLE
      tvSubscribers.text = itemView.context.getString(R.string.subscriber_count, subscribers,
          activeUsers)
    } else {
      tvSubscribers.visibility = View.INVISIBLE
      tvSubscribers.text = itemView.context.getString(R.string.subscriber_count, 0.toString(),
          0.toString())
    }

    // Hide scroll view if no chips available.
    if (nbaSubChips == null) {
      scrollView.visibility = View.GONE
      return
    }
    if (nbaSubChips.powerRankings == null && nbaSubChips.trashTalk == null
        && nbaSubChips.freeTalkFriday == null && nbaSubChips.dailyLocker == null) {
      scrollView.visibility = View.GONE
      return
    }

    // There's at least 1 chip available.
    scrollView.visibility = View.VISIBLE

    // Set Daily Locker Room Thread chip is available.
    if (nbaSubChips.dailyLocker != null) {
      chipDailyLocker.visibility = View.VISIBLE
      chipDailyLocker.setOnChipClicked {
        submissionClickListener.onSubmissionClick(nbaSubChips.dailyLocker)
      }
    } else {
      chipDailyLocker.visibility = View.GONE
    }

    // Set Free Talk Friday chip is available.
    if (nbaSubChips.freeTalkFriday != null) {
      chipFreeTalkFriday.visibility = View.VISIBLE
      chipFreeTalkFriday.setOnChipClicked {
        submissionClickListener.onSubmissionClick(nbaSubChips.freeTalkFriday)
      }
    } else {
      chipFreeTalkFriday.visibility = View.GONE
    }

    // Set Trash Talk Thread chip is available.
    if (nbaSubChips.trashTalk != null) {
      chipTrashTalk.visibility = View.VISIBLE
      chipTrashTalk.setOnChipClicked {
        submissionClickListener.onSubmissionClick(nbaSubChips.trashTalk)
      }
    } else {
      chipTrashTalk.visibility = View.GONE
    }

    // Set NBA Power Rankings chip is available.
    if (nbaSubChips.powerRankings != null) {
      chipPowerRankings.visibility = View.VISIBLE
      chipPowerRankings.setOnChipClicked {
        submissionClickListener.onSubmissionClick(nbaSubChips.powerRankings)
      }
    } else {
      chipPowerRankings.visibility = View.GONE
    }
  }
}