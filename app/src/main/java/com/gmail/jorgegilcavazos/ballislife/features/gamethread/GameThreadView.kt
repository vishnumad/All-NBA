package com.gmail.jorgegilcavazos.ballislife.features.gamethread

import com.gmail.jorgegilcavazos.ballislife.features.model.CommentWrapper
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

  fun isPremiumPurchased(): Boolean

  fun setLoadingIndicator(active: Boolean)

  fun showComments(comments: List<ThreadItem>)

  fun hideComments()

  fun addComment(position: Int, comment: CommentNode)

  fun showNoThreadText()

  fun hideNoThreadText()

  fun showNoCommentsText()

  fun hideNoCommentsText()

  fun showErrorLoadingText(code: Int)

  fun hideErrorLoadingText()

  fun commentSaves(): Observable<CommentWrapper>

  fun commentUnsaves(): Observable<CommentWrapper>

  fun upvotes(): Observable<CommentWrapper>

  fun downvotes(): Observable<CommentWrapper>

  fun novotes(): Observable<CommentWrapper>

  fun replies(): Observable<CommentWrapper>

  fun submissionReplies(): Observable<Any>

  fun streamChanges(): Observable<Boolean>

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

  fun showErrorSavingCommentToast(code: Int)

  fun showNotLoggedInToast()

  fun showNoNetAvailable()

  fun showFab()

  fun hideFab()

  fun purchasePremium()

  fun setStreamSwitch(isChecked: Boolean)
}
