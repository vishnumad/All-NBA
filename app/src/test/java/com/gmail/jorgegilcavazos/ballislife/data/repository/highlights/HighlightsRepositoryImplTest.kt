package com.gmail.jorgegilcavazos.ballislife.data.repository.highlights

import com.gmail.jorgegilcavazos.ballislife.data.service.HighlightsService
import com.gmail.jorgegilcavazos.ballislife.features.highlights.home.Sorting
import com.gmail.jorgegilcavazos.ballislife.features.model.Highlight
import io.reactivex.Single
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class HighlightsRepositoryImplTest {

  @Mock private lateinit var highlightsService: HighlightsService

  private lateinit var highlightsRepository: HighlightsRepositoryImpl

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)
    highlightsRepository = HighlightsRepositoryImpl(highlightsService)
  }

  @Test
  fun loadPagesWithNewSorting() {
    val hl1 = createHighlightWithTimestamp(9)
    val hl2 = createHighlightWithTimestamp(7)
    val hl3 = createHighlightWithTimestamp(8)
    val hl4 = createHighlightWithTimestamp(4)
    val hl5 = createHighlightWithTimestamp(6)
    val hl6 = createHighlightWithTimestamp(5)
    highlightsRepository.setItemsToLoad(3)
    `when`(highlightsService.getAllHighlights(Companion.ORDER_KEY_CREATED, 0, Long.MAX_VALUE, 3))
        .thenReturn(Single.just(mapOf("C" to hl1, "A" to hl2, "B" to hl3)))
    `when`(highlightsService.getAllHighlights(Companion.ORDER_KEY_CREATED, 0, 6, 3))
        .thenReturn(Single.just(mapOf("D" to hl4, "E" to hl5, "F" to hl6)))

    val page1 = highlightsRepository.next().test()
    page1.assertComplete()
    page1.assertValueAt(0, {it == listOf(hl1, hl3, hl2) })
    Assert.assertEquals(6, highlightsRepository.getLastHighlightTimestamp())

    val page2 = highlightsRepository.next().test()
    page2.assertComplete()
    page2.assertValueAt(0, {it == listOf(hl5, hl6, hl4) })
    Assert.assertEquals(3, highlightsRepository.getLastHighlightTimestamp())

    Assert.assertEquals(highlightsRepository.cachedHighlights, listOf(hl1, hl3, hl2, hl5, hl6, hl4))
  }

  @Test
  fun loadPagesWithDailyTopSorting() {
    val hl1 = createHighlightWithScore(500)
    val hl2 = createHighlightWithScore(31)
    val hl3 = createHighlightWithScore(45)
    val hl4 = createHighlightWithScore(2)
    val hl5 = createHighlightWithScore(444)
    val hl6 = createHighlightWithScore(1090)
    highlightsRepository.setItemsToLoad(3)
    highlightsRepository.reset(Sorting.TOP_SEASON)
    `when`(highlightsService.getSeasonHighlights(Companion.ORDER_KEY_SCORE, 0, Int.MAX_VALUE, 3))
        .thenReturn(Single.just(mapOf("C" to hl1, "A" to hl6, "B" to hl5)))
    `when`(highlightsService.getSeasonHighlights(Companion.ORDER_KEY_SCORE, 0, 443, 3))
        .thenReturn(Single.just(mapOf("D" to hl4, "E" to hl3, "F" to hl2)))

    val page1 = highlightsRepository.next().test()
    page1.assertComplete()
    page1.assertValueAt(0, {it == listOf(hl6, hl1, hl5) })
    Assert.assertEquals(443, highlightsRepository.getLastHighlightScore())

    val page2 = highlightsRepository.next().test()
    page2.assertComplete()
    page2.assertValueAt(0, {it == listOf(hl3, hl2, hl4) })
    Assert.assertEquals(1, highlightsRepository.getLastHighlightScore())

    Assert.assertEquals(highlightsRepository.cachedHighlights, listOf(hl6, hl1, hl5, hl3, hl2, hl4))
  }

  private fun createHighlightWithTimestamp(createdUtc: Long): Highlight {
    return Highlight(
        id = "ID",
        title = "Title",
        thumbnail = "",
        hdThumbnail = "",
        url = "",
        score = 0,
        createdUtc = createdUtc
    )
  }

  private fun createHighlightWithScore(score: Int): Highlight {
    return Highlight(
        id = "ID",
        title = "Title",
        thumbnail = "",
        hdThumbnail = "",
        url = "",
        score = score,
        createdUtc = 0
    )
  }

  companion object {
    private val ORDER_KEY_CREATED = "\"created_utc\""
    private val ORDER_KEY_SCORE = "\"score\""
  }

}