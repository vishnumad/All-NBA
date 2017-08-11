package com.gmail.jorgegilcavazos.ballislife.data.repository.submissions

import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.SubmissionWrapper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the submission repository interface. Stores custom submissions in a map keyed
 * by their id.
 * @see SubmissionRepository
 * @see SubmissionWrapper
 */
@Singleton
class SubmissionRepositoryImpl @Inject constructor() : SubmissionRepository {

    private val idToSubmissionMap = HashMap<String, SubmissionWrapper>()

    override fun getSubmission(id: String): SubmissionWrapper? = idToSubmissionMap[id]

    override fun saveSubmission(submissionWrapper: SubmissionWrapper) {
        idToSubmissionMap[submissionWrapper.id] = submissionWrapper
    }

    override fun reset() = idToSubmissionMap.clear()
}