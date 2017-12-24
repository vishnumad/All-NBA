package com.gmail.jorgegilcavazos.ballislife.features.model

import com.gmail.jorgegilcavazos.ballislife.features.common.SwishCardViewHolder

/**
 * Type of content that is to be displayed in a [SwishCardViewHolder].
 */
enum class SwishCard(val key: String) {
  HIGHLIGHT_SORTING("highlight_sorting"),
  HIGHLIGHT_FAVORITES("highlights_favorites"),
  EMPTY_FAVORITE_HIGHLIGHTS("empty_favorite_highlights")
}