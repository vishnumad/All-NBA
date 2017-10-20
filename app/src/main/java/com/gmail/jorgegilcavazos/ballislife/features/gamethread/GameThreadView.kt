package com.gmail.jorgegilcavazos.ballislife.features.gamethread

import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadType
import com.gmail.jorgegilcavazos.ballislife.features.model.ThreadItem
import io.reactivex.Observable

import net.dean.jraw.models.Comment
import net.dean.jraw.models.CommentNode

interface GameThreadView {

  fun getThreadType(): GameThreadType

  fun getHome(): String

  fun getVisitor(): String

  fun getGameTimeUtc(): Long

  fun setLoadingIndicator(active: Boolean)

  fun showComments(comments: List<ThreadItem>)

  fun hideComments()

  fun addComment(position: Int, comment: CommentNode)

  fun showNoThreadText()

  fun hideNoThreadText()

  fun showNoCommentsText()

  fun hideNoCommentsText()

  fun showErrorLoadingText()

  fun hideErrorLoadingText()

  fun commentSaves(): Observable<Comment>

  fun commentUnsaves(): Observable<Comment>

  fun upvotes(): Observable<Comment>

  fun downvotes(): Observable<Comment>

  fun novotes(): Observable<Comment>

  fun replies(): Observable<Comment>

  fun submissionReplies(): Observable<Any>

  fun openReplyToCommentActivity(parentComment: Comment)

  fun openReplyToSubmissionActivity(submissionId: String)

  fun showSavingToast()

  fun showSavedToast()

  fun showUnsavingToast()

  fun showUnsavedToast()

  fun showSubmittingCommentToast()

  fun showSubmittedCommentToast()

  fun showMissingParentToast()

  fun showMissingSubmissionToast()

  fun showErrorSavingCommentToast()

  fun showNotLoggedInToast()

  fun showFab()

  fun hideFab()
}
