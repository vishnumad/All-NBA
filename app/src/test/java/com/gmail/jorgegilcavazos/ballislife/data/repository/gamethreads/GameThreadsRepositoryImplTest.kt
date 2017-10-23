package com.gmail.jorgegilcavazos.ballislife.data.repository.gamethreads

import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication
import com.gmail.jorgegilcavazos.ballislife.data.repository.submissions.SubmissionRepository
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditGameThreadsService
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadSummary
import com.gmail.jorgegilcavazos.ballislife.features.model.GameThreadType
import com.gmail.jorgegilcavazos.ballislife.features.model.SubmissionWrapper
import com.gmail.jorgegilcavazos.ballislife.util.DateFormatUtil
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.TrampolineSchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import net.dean.jraw.models.CommentSort
import net.dean.jraw.models.Submission
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations

class GameThreadsRepositoryImplTest {

  companion object {
    val SUBMISSION_ID = "submissionId1"
    val SUBMISSION_TITLE = "Game Thread: Spurs @ Warriors"
    val SUBMISSION_ID_2 = "submissionId2"
    val SUBMISSION_TITLE_2 = "Game Thread: Warriors @ Spurs"
  }

  @Mock private lateinit var mockRedditGameThreadsService: RedditGameThreadsService
  @Mock private lateinit var mockSubmissionRepository: SubmissionRepository
  @Mock private lateinit var mockRedditAuthentication: RedditAuthentication

  private lateinit var gameThreadsRepository: GameThreadsRepositoryImpl

  @Before
  fun setUp() {
    MockitoAnnotations.initMocks(this)

    `when`(mockRedditAuthentication.authenticate()).thenReturn(Completable.complete())

    gameThreadsRepository = GameThreadsRepositoryImpl(
        mockRedditGameThreadsService,
        mockSubmissionRepository,
        mockRedditAuthentication,
        TrampolineSchedulerProvider())
  }

  @Test
  fun gameThreadsFoundMatching() {
    val mockSubmission = prepareMockSubmission(SUBMISSION_ID, SUBMISSION_TITLE)
    `when`(mockSubmission.selftext).thenReturn("Non empty")
    val wrapper = SubmissionWrapper(mockSubmission)
    `when`(mockSubmissionRepository.getSubmission(SUBMISSION_ID, CommentSort.NEW, true))
        .thenReturn(Single.just(wrapper))
    `when`(
        mockRedditGameThreadsService.fetchGameThreads(
            "\"created_utc\"",
            DateFormatUtil.addHoursToTime(0, -2),
            DateFormatUtil.addHoursToTime(0, 5)))
        .thenReturn(
            Single.just(
                mapOf(
                    "9813hd2" to GameThreadSummary(
                        SUBMISSION_ID,
                        SUBMISSION_TITLE,
                        0))))

    val testObserver = gameThreadsRepository.gameThreads(
        "SAS",
        "GSW",
        0L,
        GameThreadType.LIVE).test()

    testObserver.assertValueCount(2)
    testObserver.assertValueAt(0, { it.inProgress })
    testObserver.assertValueAt(1, { it.found && it.submission == mockSubmission })
  }

  @Test
  fun gameThreadsFoundMultipleMatchingReturnFirst() {
    val mockSubmission1 = prepareMockSubmission(SUBMISSION_ID, SUBMISSION_TITLE)
    `when`(mockSubmission1.selftext).thenReturn("Non empty")
    val mockSubmission2 = prepareMockSubmission(SUBMISSION_ID_2, SUBMISSION_TITLE_2)
    `when`(mockSubmission2.selftext).thenReturn("Non empty")

    val wrapper1 = SubmissionWrapper(mockSubmission1)
    val wrapper2 = SubmissionWrapper(mockSubmission2)

    `when`(mockSubmissionRepository.getSubmission(SUBMISSION_ID, CommentSort.NEW, true))
        .thenReturn(Single.just(wrapper1))
    `when`(mockSubmissionRepository.getSubmission(SUBMISSION_ID_2, CommentSort.NEW, true))
        .thenReturn(Single.just(wrapper2))
    `when`(
        mockRedditGameThreadsService.fetchGameThreads(
            "\"created_utc\"",
            DateFormatUtil.addHoursToTime(0, -2),
            DateFormatUtil.addHoursToTime(0, 5)))
        .thenReturn(
            Single.just(
                mapOf(
                    "2393d83" to GameThreadSummary(
                        SUBMISSION_ID_2,
                        SUBMISSION_TITLE_2,
                        0
                    ),
                    "9813hd2" to GameThreadSummary(
                        SUBMISSION_ID,
                        SUBMISSION_TITLE,
                        0)
                )))

    val testObserver = gameThreadsRepository.gameThreads(
        "SAS",
        "GSW",
        0L,
        GameThreadType.LIVE).test()

    testObserver.assertValueCount(2)
    testObserver.assertValueAt(0, { it.inProgress })
    testObserver.assertValueAt(1, { it.found && it.submission == mockSubmission2 })
  }

  @Test
  fun gameThreadsFoundMultipleMatchingFilterNonRemoved() {
    val mockSubmission1 = prepareMockSubmission(SUBMISSION_ID, SUBMISSION_TITLE)
    `when`(mockSubmission1.selftext).thenReturn("Non empty")
    val mockSubmission2 = prepareMockSubmission(SUBMISSION_ID_2, SUBMISSION_TITLE_2)
    `when`(mockSubmission2.selftext).thenReturn("[removed]")

    val wrapper1 = SubmissionWrapper(mockSubmission1)
    val wrapper2 = SubmissionWrapper(mockSubmission2)

    `when`(mockSubmissionRepository.getSubmission(SUBMISSION_ID, CommentSort.NEW, true))
        .thenReturn(Single.just(wrapper1))
    `when`(mockSubmissionRepository.getSubmission(SUBMISSION_ID_2, CommentSort.NEW, true))
        .thenReturn(Single.just(wrapper2))
    `when`(
        mockRedditGameThreadsService.fetchGameThreads(
            "\"created_utc\"",
            DateFormatUtil.addHoursToTime(0, -2),
            DateFormatUtil.addHoursToTime(0, 5)))
        .thenReturn(
            Single.just(
                mapOf(
                    "2393d83" to GameThreadSummary(
                        SUBMISSION_ID_2,
                        SUBMISSION_TITLE_2,
                        0
                    ),
                    "9813hd2" to GameThreadSummary(
                        SUBMISSION_ID,
                        SUBMISSION_TITLE,
                        0)
                )))

    val testObserver = gameThreadsRepository.gameThreads(
        "SAS",
        "GSW",
        0L,
        GameThreadType.LIVE).test()

    testObserver.assertValueCount(2)
    testObserver.assertValueAt(0, { it.inProgress })
    testObserver.assertValueAt(1, { it.found && it.submission == mockSubmission1 })
  }

  @Test
  fun gameThreadsFoundMultipleMatchingFilterNonDeleted() {
    val mockSubmission1 = prepareMockSubmission(SUBMISSION_ID, SUBMISSION_TITLE)
    `when`(mockSubmission1.selftext).thenReturn("Non empty")
    val mockSubmission2 = prepareMockSubmission(SUBMISSION_ID_2, SUBMISSION_TITLE_2)
    `when`(mockSubmission2.selftext).thenReturn("[deleted]")

    val wrapper1 = SubmissionWrapper(mockSubmission1)
    val wrapper2 = SubmissionWrapper(mockSubmission2)

    `when`(mockSubmissionRepository.getSubmission(SUBMISSION_ID, CommentSort.NEW, true))
        .thenReturn(Single.just(wrapper1))
    `when`(mockSubmissionRepository.getSubmission(SUBMISSION_ID_2, CommentSort.NEW, true))
        .thenReturn(Single.just(wrapper2))
    `when`(
        mockRedditGameThreadsService.fetchGameThreads(
            "\"created_utc\"",
            DateFormatUtil.addHoursToTime(0, -2),
            DateFormatUtil.addHoursToTime(0, 5)))
        .thenReturn(
            Single.just(
                mapOf(
                    "2393d83" to GameThreadSummary(
                        SUBMISSION_ID_2,
                        SUBMISSION_TITLE_2,
                        0
                    ),
                    "9813hd2" to GameThreadSummary(
                        SUBMISSION_ID,
                        SUBMISSION_TITLE,
                        0)
                )))

    val testObserver = gameThreadsRepository.gameThreads(
        "SAS",
        "GSW",
        0L,
        GameThreadType.LIVE).test()

    testObserver.assertValueCount(2)
    testObserver.assertValueAt(0, { it.inProgress })
    testObserver.assertValueAt(1, { it.found && it.submission == mockSubmission1 })
  }

  @Test
  fun gameThreadsFoundMultipleMatchingButAllRemovedOrDeleted() {
    val mockSubmission1 = prepareMockSubmission(SUBMISSION_ID, SUBMISSION_TITLE)
    `when`(mockSubmission1.selftext).thenReturn("[removed]")
    val mockSubmission2 = prepareMockSubmission(SUBMISSION_ID_2, SUBMISSION_TITLE_2)
    `when`(mockSubmission2.selftext).thenReturn("[deleted]")

    val wrapper1 = SubmissionWrapper(mockSubmission1)
    val wrapper2 = SubmissionWrapper(mockSubmission2)

    `when`(mockSubmissionRepository.getSubmission(SUBMISSION_ID, CommentSort.NEW, true))
        .thenReturn(Single.just(wrapper1))
    `when`(mockSubmissionRepository.getSubmission(SUBMISSION_ID_2, CommentSort.NEW, true))
        .thenReturn(Single.just(wrapper2))
    `when`(
        mockRedditGameThreadsService.fetchGameThreads(
            "\"created_utc\"",
            DateFormatUtil.addHoursToTime(0, -2),
            DateFormatUtil.addHoursToTime(0, 5)))
        .thenReturn(
            Single.just(
                mapOf(
                    "2393d83" to GameThreadSummary(
                        SUBMISSION_ID_2,
                        SUBMISSION_TITLE_2,
                        0
                    ),
                    "9813hd2" to GameThreadSummary(
                        SUBMISSION_ID,
                        SUBMISSION_TITLE,
                        0)
                )))

    val testObserver = gameThreadsRepository.gameThreads(
        "SAS",
        "GSW",
        0L,
        GameThreadType.LIVE).test()

    testObserver.assertValueCount(2)
    testObserver.assertValueAt(0, { it.inProgress })
    testObserver.assertValueAt(1, { it.notFound })
  }

  @Test
  fun gameThreadsFilterTitle() {
    val goodSubmissionId = "id5"
    val goodSubmissionTitle = "POST GAME THREAD: Spurs @ Warriors"
    val mockSubmission1 = prepareMockSubmission(SUBMISSION_ID, SUBMISSION_TITLE)
    `when`(mockSubmission1.selftext).thenReturn("Non empty")
    val wrapper = SubmissionWrapper(mockSubmission1)

    `when`(mockSubmissionRepository.getSubmission(goodSubmissionId, CommentSort.TOP, true))
        .thenReturn(Single.just(wrapper))
    `when`(
        mockRedditGameThreadsService.fetchGameThreads(
            "\"created_utc\"",
            DateFormatUtil.addHoursToTime(0, -2),
            DateFormatUtil.addHoursToTime(0, 5)))
        .thenReturn(
            Single.just(
                mapOf(
                    "2393d83" to GameThreadSummary(
                        "id0",
                        "Bad title",
                        0
                    ),
                    "f344fss" to GameThreadSummary(
                        "id1",
                        "Game Thread: Celtics @ Nets",
                        0
                    ),
                    "f2f34ff" to GameThreadSummary(
                        "id2",
                        "Game Thread: Spurs @ Cavaliers",
                        0
                    ),
                    "967854f" to GameThreadSummary(
                        "id3",
                        "POST GAME THREAD: Lakers @ Magic",
                        0
                    ),
                    "2ggg8A3" to GameThreadSummary(
                        "id4",
                        "GAME THREAD: Spurs @ Warriors",
                        0
                    ),
                    "334v3v3" to GameThreadSummary(
                        goodSubmissionId,
                        goodSubmissionTitle,
                        0
                    )
                )))

    val testObserver = gameThreadsRepository.gameThreads(
        "SAS",
        "GSW",
        0L,
        GameThreadType.POST).test()

    testObserver.assertValueCount(2)
    testObserver.assertValueAt(0, { it.inProgress })
    testObserver.assertValueAt(1, { it.found && it.submission == mockSubmission1 })
  }

  @Test
  fun gameThreadsNotFound() {
    `when`(
        mockRedditGameThreadsService.fetchGameThreads(
            "\"created_utc\"",
            DateFormatUtil.addHoursToTime(0, -2),
            DateFormatUtil.addHoursToTime(0, 5)))
        .thenReturn(Single.just(emptyMap<String, GameThreadSummary>()))

    val testObserver = gameThreadsRepository.gameThreads(
        "SAS",
        "GSW",
        0L,
        GameThreadType.LIVE).test()

    testObserver.assertValueCount(2)
    testObserver.assertValueAt(0, { it.inProgress })
    testObserver.assertValueAt(1, { it.notFound })
  }

  private fun prepareMockSubmission(id: String, title: String): Submission {
    val mockSubmission = mock(Submission::class.java)
    `when`(mockSubmission.id).thenReturn(id)
    `when`(mockSubmission.title).thenReturn(title)
    `when`(mockSubmission.author).thenReturn("")
    return mockSubmission
  }
}