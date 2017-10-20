package com.gmail.jorgegilcavazos.ballislife.data.actions.models

class ReplyUIModel(
    val inProgress: Boolean,
    val success: Boolean,
    val parentNotFound: Boolean,
    val notLoggedIn: Boolean) {

  companion object {
    fun inProgress(): ReplyUIModel = ReplyUIModel(
        inProgress = true,
        success = false,
        parentNotFound = false,
        notLoggedIn = false)

    fun success(): ReplyUIModel = ReplyUIModel(
        inProgress = false,
        success = true,
        parentNotFound = false,
        notLoggedIn = false)

    fun parentNotFound(): ReplyUIModel = ReplyUIModel(
        inProgress = false,
        success = false,
        parentNotFound = true,
        notLoggedIn = false)

    fun notLoggedIn(): ReplyUIModel = ReplyUIModel(
        inProgress = false,
        success = false,
        parentNotFound = false,
        notLoggedIn = true)
  }
}