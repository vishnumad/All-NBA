package com.gmail.jorgegilcavazos.ballislife.features.model

import com.google.gson.annotations.SerializedName

data class Broadcaster(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("home_visistor") val homeVisitor: String,
    val scope: String)