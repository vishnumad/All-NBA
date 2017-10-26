package com.gmail.jorgegilcavazos.ballislife.features.model

class CommentItem(
    val commentWrapper: CommentWrapper,
    var depth: Int,
    var childrenCollapsed: Boolean = false)