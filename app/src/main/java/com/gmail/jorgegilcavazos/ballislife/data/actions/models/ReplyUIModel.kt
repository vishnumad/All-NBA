package com.gmail.jorgegilcavazos.ballislife.data.actions.models

import com.gmail.jorgegilcavazos.ballislife.features.model.CommentItem

class ReplyUIModel(
    val inProgress: Boolean = false,
    val success: Boolean = false,
    val parentNotFound: Boolean = false,
    val notLoggedIn: Boolean = false,
    val commentItem: CommentItem? = null,
    val error: Throwable? = null) {

  companion object {
    fun inProgress(): ReplyUIModel = ReplyUIModel(inProgress = true)

    fun success(): ReplyUIModel = ReplyUIModel(success = true)

    fun success(commentItem: CommentItem): ReplyUIModel = ReplyUIModel(
        success = true,
        commentItem = commentItem)

    fun parentNotFound(): ReplyUIModel = ReplyUIModel(parentNotFound = true)

    fun notLoggedIn(): ReplyUIModel = ReplyUIModel(notLoggedIn = true)

    fun error(e: Throwable): ReplyUIModel = ReplyUIModel(error = e)
  }
}