package com.gmail.jorgegilcavazos.ballislife.features.boxscore

import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreResponse

class BoxScoreUIModel(
		val inProgress: Boolean = false,
		val success: Boolean = false,
		val notAvailable: Boolean = false,
		val boxScore: BoxScoreResponse? = null) {

	companion object {
		fun inProgress() = BoxScoreUIModel(inProgress = true)

		fun success(boxScoreResponse: BoxScoreResponse) = BoxScoreUIModel(success = true,
				boxScore = boxScoreResponse)

		fun notAvailable() = BoxScoreUIModel(notAvailable = true)
	}
}