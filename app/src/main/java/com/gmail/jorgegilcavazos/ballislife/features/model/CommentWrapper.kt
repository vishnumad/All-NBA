package com.gmail.jorgegilcavazos.ballislife.features.model

import net.dean.jraw.models.Comment
import net.dean.jraw.models.Flair
import net.dean.jraw.models.VoteDirection
import java.io.Serializable
import java.util.*

class CommentWrapper(
    val comment: Comment?,
    val id: String = "",
    val saved: Boolean = false,
    val author: String = "",
    val score: Int = 0,
    val created: Date = Calendar.getInstance().time,
    val body: String = "",
    val bodyHtml: String = "",
    val authorFlair: Flair? = null,
    val vote: VoteDirection = VoteDirection.UPVOTE,
    val edited: Boolean = false) : Serializable