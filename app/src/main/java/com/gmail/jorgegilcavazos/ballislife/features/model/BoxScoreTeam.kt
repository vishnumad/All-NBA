package com.gmail.jorgegilcavazos.ballislife.features.model

import com.google.gson.annotations.SerializedName

class BoxScoreTeam(val pstsg: List<StatLine>,
									 @SerializedName("s") val score: Int)
