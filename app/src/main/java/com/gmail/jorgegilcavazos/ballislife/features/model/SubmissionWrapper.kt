package com.gmail.jorgegilcavazos.ballislife.features.model

import net.dean.jraw.models.CommentSort
import net.dean.jraw.models.OEmbed
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
  var isHidden: Boolean = false
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
    isHidden = submission?.isHidden == true
    score = submission?.score ?: 0
    commentCount = submission?.commentCount ?: 0
    thumbnail = submission?.thumbnail
    highResThumbnail = getOEmbedMedia()?.thumbnail?.url?.toString() ?: ""
    voteDirection = submission?.vote
    isSaved = submission?.isSaved == true
    selfTextHtml = submission?.data("selftext_html")
    url = submission?.url
  }

  constructor(submission: Submission) :
      this(submission.id, submission, submission.title, submission.author)

  /**
   * Get OEmbed media ourselves. [Submission.getOEmbedMedia] only checks if "media"
   * exists and does not make sure that an "oembed" field exists before creating an [OEmbed]
   * object causing NullPointerExceptions if the "oembed" field does not exist.
   *
   * Reddit posts containing embedded videos do not have an "oembed" field.
   */
  private fun getOEmbedMedia(): OEmbed? {
    return submission?.dataNode?.run {
      when {
        // Same as in [Submission.getOEmbedMedia]
        has("media").not() -> null
        get("media").size() == 0 -> null
        // Check that the node contains an "oembed" field
        get("media").has("oembed") -> OEmbed(get("media").get("oembed"))
        else -> null
      }
    }
  }
}
