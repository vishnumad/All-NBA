package com.gmail.jorgegilcavazos.ballislife.features.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Highlight(
    val id: String = "testId",
    val title: String = "testTitle",
    val thumbnail: String? = "testThumbnail",
    val hdThumbnail: String? = "testHdThumbnail",
    val url: String = "testUrl",
    val score: Int = 0,
    @SerializedName("created_utc") val createdUtc: Long = 0,
    @SerializedName("fav_time") val favTime: Date? = null)

fun Highlight.toMapWithNewDate(): Map<String, Any?> {
  return mapOf(
      "id" to this.id,
      "title" to this.title,
      "thumbnail" to this.thumbnail,
      "hdThumbnail" to this.hdThumbnail,
      "url" to this.url,
      "score" to this.score,
      "created_utc" to this.createdUtc,
      "fav_time" to Date())
}
