package com.gmail.jorgegilcavazos.ballislife.util

import com.gmail.jorgegilcavazos.ballislife.R

class TeamUtils {
  companion object {
    fun getTeamLogo(abbreviation: String): Int {
      return when(abbreviation) {
        "ATL" -> R.drawable.atl
        "BKN" -> R.drawable.bkn
        "BOS" -> R.drawable.bos
        "CHA" -> R.drawable.cha
        "CHI" -> R.drawable.chi
        "CLE" -> R.drawable.cle
        "DAL" -> R.drawable.dal
        "DEN" -> R.drawable.den
        "DET" -> R.drawable.det
        "GS" -> R.drawable.gsw
        "HOU" -> R.drawable.hou
        "IND" -> R.drawable.ind
        "LAC" -> R.drawable.lac
        "LAL" -> R.drawable.lal
        "MEM" -> R.drawable.mem
        "MIA" -> R.drawable.mia
        "MIL" -> R.drawable.mil
        "MIN" -> R.drawable.min
        "NO" -> R.drawable.nop
        "NY" -> R.drawable.nyk
        "OKC" -> R.drawable.okc
        "ORL" -> R.drawable.orl
        "PHI" -> R.drawable.phi
        "PHX" -> R.drawable.phx
        "POR" -> R.drawable.por
        "SAC" -> R.drawable.sac
        "SA" -> R.drawable.sas
        "TOR" -> R.drawable.tor
        "UTAH" -> R.drawable.uta
        "WSH" -> R.drawable.was
        else -> R.drawable.rnbasnoo
      }
    }
  }
}