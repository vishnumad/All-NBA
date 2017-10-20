package com.gmail.jorgegilcavazos.ballislife.data.repository.gamethreads

import com.gmail.jorgegilcavazos.ballislife.features.gamethread.GameThreadsUIModel
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadType
import io.reactivex.Observable

interface GameThreadsRepository {
  fun gameThreads(home: String, visitor: String, gameTimeUtc: Long, type: GameThreadType)
      : Observable<GameThreadsUIModel>
}