package com.gmail.jorgegilcavazos.ballislife.data.actions.models

class VoteUIModel(val inProgress: Boolean = false,
									val success: Boolean = false,
									val notLoggedIn: Boolean = false,
									val error: Throwable? = null) {

	companion object {
		fun inProgress(): VoteUIModel = VoteUIModel(inProgress = true)

		fun success(): VoteUIModel = VoteUIModel(success = true)

		fun notLoggedIn(): VoteUIModel = VoteUIModel(notLoggedIn = true)

		fun error(e: Throwable): VoteUIModel = VoteUIModel(error = e)
	}
}