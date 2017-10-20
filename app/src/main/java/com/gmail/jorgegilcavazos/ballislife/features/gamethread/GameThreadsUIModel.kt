package com.gmail.jorgegilcavazos.ballislife.features.gamethread

import net.dean.jraw.models.Submission

data class GameThreadsUIModel(
    val inProgress: Boolean,
    val found: Boolean,
    val notFound: Boolean,
    val submission: Submission?) {

  companion object {
    fun inProgress(): GameThreadsUIModel =
        GameThreadsUIModel(inProgress = true, found = false, notFound = false, submission = null)

    fun found(submission: Submission): GameThreadsUIModel =
        GameThreadsUIModel(
            inProgress = false,
            found = true,
            notFound = false,
            submission = submission)

    fun notFound(): GameThreadsUIModel =
        GameThreadsUIModel(inProgress = false, found = false, notFound = true, submission = null)
  }
}