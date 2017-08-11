package com.gmail.jorgegilcavazos.ballislife.data.repository.submissions

import com.gmail.jorgegilcavazos.ballislife.features.model.wrapper.SubmissionWrapper

/**
 * Stores recently fetched submissions in memory.
 * @see SubmissionWrapper
 */
interface SubmissionRepository {
    fun getSubmission(id: String): SubmissionWrapper?

    fun saveSubmission(submissionWrapper: SubmissionWrapper)

    fun reset()
}