package com.gmail.jorgegilcavazos.ballislife.util

import com.gmail.jorgegilcavazos.ballislife.features.common.ThreadAdapter
import com.gmail.jorgegilcavazos.ballislife.features.model.CommentItem
import com.gmail.jorgegilcavazos.ballislife.features.model.CommentWrapper
import com.gmail.jorgegilcavazos.ballislife.features.model.ThreadItem
import net.dean.jraw.models.CommentNode
import java.util.*

class CommentsTraverser {
	companion object {
		fun flattenCommentTree(topLevelComments: List<CommentNode>): List<ThreadItem> {
			var items: MutableList<ThreadItem> = ArrayList()
			for (node in topLevelComments) {
				traverse(node, items)
			}
			return items
		}

		private fun traverse(root: CommentNode, items: MutableList<ThreadItem>) {
			items.add(ThreadItem(ThreadAdapter.TYPE_COMMENT, createCommentItem(root), root.depth))
			for (node in root.children) {
				traverse(node, items)
			}
			if (root.hasMoreComments()) {
				items.add(ThreadItem(ThreadAdapter.TYPE_LOAD_MORE, createCommentItem(root), root.depth + 1))
			}
		}

		private fun createCommentItem(root: CommentNode): CommentItem {
			val comment = root.comment
			return CommentItem(
					commentNode = root,
					commentWrapper = CommentWrapper(
							comment = comment,
							id = comment.id,
							saved = comment.isSaved,
							author = comment.author,
							score = comment.score,
							created = comment.created,
							body = comment.body,
							bodyHtml = comment.data("body_html"),
							authorFlair = comment.authorFlair,
							vote = comment.vote,
							edited = comment.hasBeenEdited()),
					depth = root.depth)
		}
	}
}
