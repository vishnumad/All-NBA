package com.gmail.jorgegilcavazos.ballislife.features.model

import com.google.gson.annotations.SerializedName

data class BoxScoreResponse(@SerializedName("g") val game: BoxScoreValues)