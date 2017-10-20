package com.gmail.jorgegilcavazos.ballislife.data.actions.models

class SaveUIModel(val inProgress: Boolean, val success: Boolean, val notLoggedIn: Boolean) {

  companion object {
    fun inProgress(): SaveUIModel = SaveUIModel(
        inProgress = true,
        success = false,
        notLoggedIn = false)

    fun success(): SaveUIModel = SaveUIModel(
        inProgress = false,
        success = true,
        notLoggedIn = false)

    fun notLoggedIn(): SaveUIModel = SaveUIModel(
        inProgress = false,
        success = false,
        notLoggedIn = true)
  }
}