package com.gmail.jorgegilcavazos.ballislife.features.model

import net.dean.jraw.models.CommentNode

class CommentItem(
		val commentNode: CommentNode? = null,
		val commentWrapper: CommentWrapper,
		var depth: Int,
		var childrenCollapsed: Boolean = false)