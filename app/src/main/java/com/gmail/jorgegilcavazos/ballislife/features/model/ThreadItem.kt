package com.gmail.jorgegilcavazos.ballislife.features.model

class ThreadItem(
    val type: Int,
    val commentItem: CommentItem?,
    val depth: Int,
    var hidden: Boolean = false)