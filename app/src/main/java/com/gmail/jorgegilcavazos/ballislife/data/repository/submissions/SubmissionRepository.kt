package com.gmail.jorgegilcavazos.ballislife.data.repository.submissions

import com.gmail.jorgegilcavazos.ballislife.features.model.SubmissionWrapper
import com.google.common.base.Optional
import io.reactivex.Single
import net.dean.jraw.models.CommentSort
import net.dean.jraw.models.Submission

/**
 * Stores recently fetched submissions in memory.
 * @see SubmissionWrapper
 */
interface SubmissionRepository {
  /**
   * Returns a [SubmissionWrapper] that matches the given id and comment sort.
   * A cached submission will be returned if available unless a forceReload is specified.
   */
  fun getSubmission(
      id: String,
      sort: CommentSort,
      forceReload: Boolean): Single<SubmissionWrapper>

  /**
   * Returns a submission that matches the given id if it is available from memory.
   * User [getSubmission] if a specific comment sort is needed and if unavailable from cache a
   * network request should be made.
   * */
  fun getCachedSubmission(id: String): Optional<Submission>

    fun saveSubmission(submissionWrapper: SubmissionWrapper)

    fun reset()
}