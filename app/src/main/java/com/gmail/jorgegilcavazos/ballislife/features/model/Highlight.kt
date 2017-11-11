package com.gmail.jorgegilcavazos.ballislife.features.model

import com.google.gson.annotations.SerializedName

data class Highlight(
    val id: String,
    val title: String,
    val thumbnail: String,
    val hdThumbnail: String,
    val url: String,
    val score: Int = 0,
    @SerializedName("created_utc") val createdUtc: Long)
