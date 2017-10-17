package com.gmail.jorgegilcavazos.ballislife.features.model

import com.google.gson.annotations.SerializedName

data class GameThreadSummary(val id: String,
                             val title: String,
                             @SerializedName("created_utc") val createdUtc: Long)
