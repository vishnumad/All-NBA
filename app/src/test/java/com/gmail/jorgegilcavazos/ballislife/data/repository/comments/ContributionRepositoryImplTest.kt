package com.gmail.jorgegilcavazos.ballislife.data.repository.comments

import net.dean.jraw.models.Comment
import net.dean.jraw.models.Submission
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ContributionRepositoryImplTest {

  private lateinit var repository: ContributionRepositoryImpl

  @Before
  fun setup() {
    repository = ContributionRepositoryImpl()
  }

  @Test
  fun saveAndGetComment() {
    val mockComment1 = mock(Comment::class.java)
    `when`(mockComment1.fullName).thenReturn("fullname1")
    val mockComment2 = mock(Comment::class.java)
    `when`(mockComment2.fullName).thenReturn("fullname2")

    repository.saveComment(mockComment1)
    repository.saveComment(mockComment2)

    assertEquals(mockComment1, repository.getComment("fullname1"))
    assertEquals(mockComment2, repository.getComment("fullname2"))
  }

  @Test
  fun saveAndGetSubmission() {
    val mockSubmission1 = mock(Submission::class.java)
    `when`(mockSubmission1.id).thenReturn("id1")
    val mockSubmission2 = mock(Submission::class.java)
    `when`(mockSubmission2.id).thenReturn("id2")

    repository.saveSubmission(mockSubmission1)
    repository.saveSubmission(mockSubmission2)

    assertEquals(mockSubmission1, repository.getSubmission("id1"))
    assertEquals(mockSubmission2, repository.getSubmission("id2"))
  }
}