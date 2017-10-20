package com.gmail.jorgegilcavazos.ballislife.data.actions.models

class VoteUIModel(val inProgress: Boolean, val success: Boolean, val notLoggedIn: Boolean) {

  companion object {
    fun inProgress(): VoteUIModel = VoteUIModel(
        inProgress = true,
        success = false,
        notLoggedIn = false)

    fun success(): VoteUIModel = VoteUIModel(
        inProgress = false,
        success = true,
        notLoggedIn = false)

    fun notLoggedIn(): VoteUIModel = VoteUIModel(
        inProgress = false,
        success = false,
        notLoggedIn = true)
  }
}