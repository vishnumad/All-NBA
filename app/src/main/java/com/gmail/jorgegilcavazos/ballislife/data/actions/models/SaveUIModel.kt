package com.gmail.jorgegilcavazos.ballislife.data.actions.models

class SaveUIModel(val inProgress: Boolean = false,
									val success: Boolean = false,
									val notLoggedIn: Boolean = false,
									val error: Throwable? = null) {

	companion object {
		fun inProgress(): SaveUIModel = SaveUIModel(inProgress = true)

		fun success(): SaveUIModel = SaveUIModel(success = true)

		fun notLoggedIn(): SaveUIModel = SaveUIModel(notLoggedIn = true)

		fun error(e: Throwable): SaveUIModel = SaveUIModel(error = e)
	}
}