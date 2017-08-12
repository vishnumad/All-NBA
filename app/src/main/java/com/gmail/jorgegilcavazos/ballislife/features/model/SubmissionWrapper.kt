package com.gmail.jorgegilcavazos.ballislife.features.model

import net.dean.jraw.models.CommentSort
import net.dean.jraw.models.Submission
import net.dean.jraw.models.VoteDirection

import java.io.Serializable

/**
 * Wraps a [Submission] to allow mutation.
 */
class SubmissionWrapper : Serializable {
  lateinit var id: String
  lateinit var submission: Submission
  lateinit var title: String
  lateinit var author: String
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

  constructor()

  constructor(submission: Submission) {
    this.submission = submission
    id = submission.id
    title = submission.title
    author = submission.author
    created = submission.created.time
    domain = submission.domain
    isSelfPost = submission.isSelfPost
    isStickied = submission.isStickied
    score = submission.score!!
    commentCount = submission.commentCount!!
    thumbnail = submission.thumbnail
    highResThumbnail = submission.oEmbedMedia?.thumbnail?.url?.toString()
    voteDirection = submission.vote
    isSaved = submission.isSaved!!
    selfTextHtml = submission.data("selftext_html")
    url = submission.url
  }
}
