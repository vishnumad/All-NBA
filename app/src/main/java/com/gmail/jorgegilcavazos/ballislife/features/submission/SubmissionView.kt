package com.gmail.jorgegilcavazos.ballislife.features.submission

import com.gmail.jorgegilcavazos.ballislife.features.model.CommentItem
import com.gmail.jorgegilcavazos.ballislife.features.model.ThreadItem
import io.reactivex.Observable

import net.dean.jraw.models.Comment
import net.dean.jraw.models.CommentNode
import net.dean.jraw.models.Submission

interface SubmissionView {

  fun commentSaves(): Observable<Comment>

  fun commentUnsaves(): Observable<Comment>

  fun commentUpvotes(): Observable<Comment>

  fun commentDownvotes(): Observable<Comment>

  fun commentNovotes(): Observable<Comment>

  fun submissionSaves(): Observable<Submission>

  fun submissionUnsaves(): Observable<Submission>

  fun submissionUpvotes(): Observable<Submission>

  fun submissionDownvotes(): Observable<Submission>

  fun submissionNovotes(): Observable<Submission>

  fun commentReplies(): Observable<Comment>

  fun submissionReplies(): Observable<Any>

  fun submissionContentClicks(): Observable<String>

  fun setLoadingIndicator(active: Boolean)

  fun showComments(commentNodes: List<ThreadItem>, submission: Submission)

  fun addComment(comment: CommentNode, position: Int)

  fun addCommentItem(commentItem: CommentItem, parentFullname: String)

  fun showErrorAddingComment()

  fun showNotLoggedInError()

  fun showSavedCommentToast()

  fun showUnsavedCommentToast()

  fun openReplyToCommentActivity(parentComment: Comment)

  fun openReplyToSubmissionActivity(submissionId: String)

  fun openContentTab(url: String)

  fun openStreamable(shortcode: String)

  fun showContentUnavailableToast()

  fun scrollToComment(index: Int)

  fun hideFab()

  fun showFab()
}
