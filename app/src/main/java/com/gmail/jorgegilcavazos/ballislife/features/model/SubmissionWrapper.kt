package com.gmail.jorgegilcavazos.ballislife.features.model

import net.dean.jraw.models.CommentSort
import net.dean.jraw.models.Submission
import net.dean.jraw.models.VoteDirection

import java.io.Serializable

/**
 * Wraps a [Submission] to allow mutation.
 */
data class SubmissionWrapper(val id: String,
                             val submission: Submission?,
                             val title: String,
                             val author: String) : Serializable {
  var created: Long = 0
  var domain: String? = null
  var isSelfPost: Boolean = false
  var isStickied: Boolean = false
  var score: Int = 0
  var commentCount: Int = 0
  var thumbnail: String? = null
  var highResThumbnail: String? = null
  var voteDirection: VoteDirection? = null
  var isSaved: Boolean = false
  var selfTextHtml: String? = null
  var url: String? = null
  var sort: CommentSort? = null

  init {
    created = submission?.created?.time ?: 0
    domain = submission?.domain
    isSelfPost = submission?.isSelfPost == true
    isStickied = submission?.isStickied == true
    score = submission?.score ?: 0
    commentCount = submission?.commentCount ?: 0
    thumbnail = submission?.thumbnail
    highResThumbnail = submission?.oEmbedMedia?.thumbnail?.url?.toString() ?: ""
    voteDirection = submission?.vote
    isSaved = submission?.isSaved == true
    selfTextHtml = submission?.data("selftext_html")
    url = submission?.url
  }

  constructor(submission: Submission) :
      this(submission.id, submission, submission.title, submission.author)
}
