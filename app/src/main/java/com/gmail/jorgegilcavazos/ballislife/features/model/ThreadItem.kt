package com.gmail.jorgegilcavazos.ballislife.features.model

class ThreadItem(
		val type: ThreadItemType,
		/**
		 * If type = COMMENT then it holds [CommentItem] for that comment.
		 * If type = LOAD_MORE_COMMENTS = then it holds the [CommentItem] of it's parent comment.
		 * else null.
		 */
		val commentItem: CommentItem?,
		val depth: Int,
		var loading: Boolean = false)