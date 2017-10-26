package com.gmail.jorgegilcavazos.ballislife.data.repository.comments

import net.dean.jraw.models.Comment
import net.dean.jraw.models.Submission
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContributionRepositoryImpl @Inject constructor() : ContributionRepository {

  private val idToCommentMap = mutableMapOf<String, Comment>()
  private val idToSubmissionMap = mutableMapOf<String, Submission>()

  override fun getComment(id: String): Comment? {
    return idToCommentMap[id]
  }

  override fun saveComment(comment: Comment) {
    idToCommentMap.put(comment.id, comment)
  }

  override fun getSubmission(id: String): Submission? {
    return idToSubmissionMap[id]
  }

  override fun saveSubmission(submission: Submission) {
    idToSubmissionMap.put(submission.id, submission)
  }
}