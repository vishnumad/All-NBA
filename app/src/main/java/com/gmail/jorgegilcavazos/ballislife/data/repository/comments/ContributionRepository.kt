package com.gmail.jorgegilcavazos.ballislife.data.repository.comments

import net.dean.jraw.models.Comment
import net.dean.jraw.models.Submission

/**
 * Repository for saving [Comment]s and [Submission]s in memory.
 */
interface ContributionRepository {

  fun getComment(id: String): Comment?

  fun saveComment(comment: Comment)

  fun getSubmission(id: String): Submission?

  fun saveSubmission(submission: Submission)
}