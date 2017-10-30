package com.gmail.jorgegilcavazos.ballislife.data.repository.boxscore

import com.gmail.jorgegilcavazos.ballislife.features.boxscore.BoxScoreUIModel
import io.reactivex.Observable

interface BoxScoreRepository {
	fun boxScore(gameId: String, forceNetwork: Boolean): Observable<BoxScoreUIModel>
}