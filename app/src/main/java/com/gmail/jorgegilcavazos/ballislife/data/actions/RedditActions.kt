package com.gmail.jorgegilcavazos.ballislife.data.actions

import com.gmail.jorgegilcavazos.ballislife.data.actions.models.ReplyUIModel
import com.gmail.jorgegilcavazos.ballislife.data.actions.models.SaveUIModel
import com.gmail.jorgegilcavazos.ballislife.data.actions.models.VoteUIModel
import io.reactivex.Observable
import net.dean.jraw.models.Comment
import net.dean.jraw.models.VoteDirection

interface RedditActions {

  fun saveComment(comment: Comment): Observable<SaveUIModel>

  fun unsaveComment(comment: Comment): Observable<SaveUIModel>

  fun voteComment(comment: Comment, voteDirection: VoteDirection): Observable<VoteUIModel>

  fun replyToComment(parentFullname: String, response: String): Observable<ReplyUIModel>

  fun replyToSubmission(submissionId: String, response: String): Observable<ReplyUIModel>
}