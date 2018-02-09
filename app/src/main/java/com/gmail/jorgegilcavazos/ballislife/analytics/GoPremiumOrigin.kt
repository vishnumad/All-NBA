package com.gmail.jorgegilcavazos.ballislife.analytics

enum class GoPremiumOrigin(val originName: String) {
  NAVIGATION_DRAWER("navigation_drawer"),
  GAME_THREAD_STREAM("game_thread_stream"),
  GAME_THREAD_DELAY("game_thread_delay"),
  HIGHLIGHTS_SORTING("highlights_sorting_options"),
  HIGHLIGHTS_SORTING_EXPLORE_CARD("highlights_sorting_explore_card"),
  HIGHLIGHTS_FAVORITES_EXPLORE_CARD("highlights_favorites_explore_card"),
  HIDE_SCORES("hide_scores"),
  ALERT_TRIPLE_DOUBLE("alert_triple_double"),
  ALERT_QUADRUPLE_DOUBLE("alert_quadruple_double"),
  ALERT_5_X_5("alert_5x5"),
  SETTINGS_ABOUT("settings_about")
}