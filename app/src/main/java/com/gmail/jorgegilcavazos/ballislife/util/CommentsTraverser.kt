package com.gmail.jorgegilcavazos.ballislife.util

import com.gmail.jorgegilcavazos.ballislife.features.common.ThreadAdapter
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
      items.add(ThreadItem(ThreadAdapter.TYPE_COMMENT, root, root.depth))
      for (node in root.children) {
        traverse(node, items)
      }
      if (root.hasMoreComments()) {
        items.add(ThreadItem(ThreadAdapter.TYPE_LOAD_MORE, null, root.depth + 1))
      }
    }
  }
}
