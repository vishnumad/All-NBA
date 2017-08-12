package com.gmail.jorgegilcavazos.ballislife.data.repository.submissions

import com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthentication
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditService
import com.gmail.jorgegilcavazos.ballislife.features.model.SubmissionWrapper
import com.google.common.base.Optional
import io.reactivex.Single
import net.dean.jraw.models.CommentSort
import net.dean.jraw.models.Submission
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the [SubmissionRepository] interface. Stores [SubmissionWrapper]s in a map
 * keyed by their id.
 * TODO: should there be a limit to how many submissions are cached?
 */
@Singleton
class SubmissionRepositoryImpl
@Inject constructor(
    val redditAuthentication: RedditAuthentication,
    val redditService: RedditService) : SubmissionRepository {
  private val idToSubmissionMap = HashMap<String, SubmissionWrapper>()

  override fun getSubmission(
      id: String,
      sort: CommentSort,
      forceReload: Boolean): Single<SubmissionWrapper> {
    // Use submission in cache if available and has the right comment sorting.
    if (!forceReload && idToSubmissionMap[id] != null && idToSubmissionMap[id]?.sort == sort) {
      return Single.just(idToSubmissionMap[id])
    }
    return redditService.getSubmission(redditAuthentication.redditClient, id, sort)
        .flatMap { s: Submission ->
          val wrapper = SubmissionWrapper(s)
          wrapper.sort = sort
          idToSubmissionMap[wrapper.id] = wrapper
          Single.just(wrapper)
        }
  }

  override fun getCachedSubmission(id: String): Optional<Submission> {
    val submission = idToSubmissionMap[id]?.submission
    return Optional.of(submission)
  }

  override fun saveSubmission(submissionWrapper: SubmissionWrapper) {
    idToSubmissionMap[submissionWrapper.id] = submissionWrapper
  }

  override fun reset() = idToSubmissionMap.clear()
}