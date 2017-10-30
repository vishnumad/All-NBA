package com.gmail.jorgegilcavazos.ballislife.features.boxscore

import com.gmail.jorgegilcavazos.ballislife.features.model.BoxScoreValues

class BoxScoreUIModel(
		val inProgress: Boolean = false,
		val success: Boolean = false,
		val notAvailable: Boolean = false,
		val boxScoreValues: BoxScoreValues? = null) {

	companion object {
		fun inProgress() = BoxScoreUIModel(inProgress = true)

		fun success(boxScoreValues: BoxScoreValues) = BoxScoreUIModel(success = true,
				boxScoreValues = boxScoreValues)

		fun notAvailable() = BoxScoreUIModel(notAvailable = true)
	}
}