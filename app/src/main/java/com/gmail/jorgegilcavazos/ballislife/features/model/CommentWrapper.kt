package com.gmail.jorgegilcavazos.ballislife.features.model

import net.dean.jraw.models.Comment
import net.dean.jraw.models.Flair
import java.io.Serializable
import java.util.*

class CommentWrapper(
    val comment: Comment?,
    val id: String,
    val parentFullname: String,
    val saved: Boolean,
    val author: String,
    val score: Int,
    val created: Date,
    val body: String,
    val bodyHtml: String,
    val authorFlair: Flair?) : Serializable