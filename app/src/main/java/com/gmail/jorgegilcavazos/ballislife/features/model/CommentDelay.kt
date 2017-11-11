package com.gmail.jorgegilcavazos.ballislife.features.model

enum class CommentDelay(val seconds: Int) {
  NONE(0),
  FIVE(5),
  TEN(10),
  TWENTY(20),
  THIRTY(30),
  MINUTE(60),
  TWO_MINUTES(120),
  FIVE_MINUTES(300)
}