package com.gmail.jorgegilcavazos.ballislife.features.model

/**
 * A player's statline.
 */
data class StatLine(
    var pts: Int,
    var min: Int,
    var ast: Int,
    var reb: Int,
    var dreb: Int,
    var oreb: Int,
    var blk: Int,
    var blka: Int,
    var stl: Int,
    var tov: Int,
    var pf: Int,
    var fga: Int,
    var fgm: Int,
    var tpa: Int,
    var tpm: Int,
    var fta: Int,
    var ftm: Int,
    var pm: Int,
    var fn: String?,
    var ln: String?
)