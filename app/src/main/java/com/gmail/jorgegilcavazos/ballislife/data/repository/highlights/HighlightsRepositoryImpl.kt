package com.gmail.jorgegilcavazos.ballislife.data.repository.highlights

import android.support.annotation.VisibleForTesting
import com.gmail.jorgegilcavazos.ballislife.data.service.HighlightsService
import com.gmail.jorgegilcavazos.ballislife.features.highlights.Sorting
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HighlightsRepositoryImpl @Inject constructor(
    private val highlightsService: HighlightsService) : HighlightsRepository {

  companion object {
    private val ORDER_KEY_CREATED = "\"created_utc\""
    private val ORDER_KEY_SCORE = "\"score\""
  }

  private val cachedHighlights: MutableList<Highlight> = ArrayList()
  private var sorting = Sorting.NEW
  private var itemsToLoad: Int = 0
  private var lastHighlightTimestamp = Long.MAX_VALUE
  private var lastHighlightScore = Int.MAX_VALUE

  override fun setItemsToLoad(itemsToLoad: Int) {
    this.itemsToLoad = itemsToLoad
  }

  override fun reset(sorting: Sorting) {
    this.sorting = sorting
    lastHighlightTimestamp = Long.MAX_VALUE
    lastHighlightScore = Int.MAX_VALUE
    cachedHighlights.clear()
  }

  override fun next(): Single<List<Highlight>> {
    if (lastHighlightScore < 0 || lastHighlightTimestamp < 0) {
      return Single.just(emptyList())
    }

    val source = when (sorting) {
      Sorting.NEW -> {
        highlightsService.getAllHighlights(
            ORDER_KEY_CREATED,
            0,
            lastHighlightTimestamp,
            itemsToLoad
        )
      }
      Sorting.TOP_DAY -> {
        highlightsService.getDailyHighlights(
            DateFormatUtil.getTodayForHighlights(),
            ORDER_KEY_SCORE,
            0,
            lastHighlightScore,
            itemsToLoad
        )
      }
      Sorting.TOP_WEEK -> {
        highlightsService.getWeeklyHighlights(
            DateFormatUtil.getWeekForHighlights(),
            ORDER_KEY_SCORE,
            0,
            lastHighlightScore,
            itemsToLoad
        )
      }
      Sorting.TOP_SEASON -> {
        highlightsService.getSeasonHighlights(
            ORDER_KEY_SCORE,
            0,
            lastHighlightScore,
            itemsToLoad
        )
      }
    }

    // Get highlights from 0 to PREV_MAX in batches of [itemsToLoad]. If this is the first fetch,
    // it will return the last [itemsToLoad] items from 0 to Int/Long.MAX. Then it will save the
    // lowest value - 1 as the new PREV_MAX, so that the next time this is called it fetches
    // from 0 to PREV_MAX.
    return source
        .flatMap { stringHighlightMap ->
          if (stringHighlightMap.isEmpty()) {
            Single.just(emptyList<Highlight>())
          } else {
            val sortedHighlights = when (sorting) {
              Sorting.NEW -> stringHighlightMap.values.sortedByDescending { it.createdUtc }
              Sorting.TOP_DAY,
              Sorting.TOP_WEEK,
              Sorting.TOP_SEASON -> stringHighlightMap.values.sortedByDescending { it.score }
            }

            // Set the new limit to the lowest value (-1 to avoid duplication) in this batch of
            // highlights so that the next page will fetch from 0 to that value.
            //
            // Note that with this solution it is possible that some highlights will be lost in
            // pagination because we're ending the next page on the lowest value of the last
            // page - 1.
            // Say we have 10 items with scores [20, 15, 15, 15, 12, 10, 9, 9, 5, 4] and paginating
            // in chunks of 3.
            // The first page would return 20, 15, 15 and set the value to 14,
            // The second page would then return 12, 10, and 9, skipping the third 15.
            when (sorting) {
              Sorting.NEW -> lastHighlightTimestamp = sortedHighlights.last().createdUtc - 1
              Sorting.TOP_DAY,
              Sorting.TOP_WEEK,
              Sorting.TOP_SEASON -> lastHighlightScore = sortedHighlights.last().score - 1
            }

            cachedHighlights.addAll(sortedHighlights)
            Single.just(sortedHighlights)
          }
        }
  }

  override fun getCachedHighlights(): List<Highlight> {
    return cachedHighlights
  }

  override fun getSorting(): Sorting {
    return sorting
  }

  @VisibleForTesting
  fun getLastHighlightTimestamp(): Long = lastHighlightTimestamp

  @VisibleForTesting
  fun getLastHighlightScore(): Int = lastHighlightScore
}
