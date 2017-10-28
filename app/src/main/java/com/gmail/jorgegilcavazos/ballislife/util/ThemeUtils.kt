package com.gmail.jorgegilcavazos.ballislife.util

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.features.model.SwishTheme

class ThemeUtils {

	companion object {

		fun getTextColor(context: Context, theme: SwishTheme): Int {
			val attrs = intArrayOf(android.R.attr.textColorPrimary)
			val typedArray: TypedArray
			typedArray = if (theme === SwishTheme.DARK) {
				context.obtainStyledAttributes(R.style.AppTheme_Dark, attrs)
			} else {
				context.obtainStyledAttributes(R.style.AppTheme, attrs)
			}
			val textColor = typedArray.getColor(0, Color.BLACK)
			typedArray.recycle()
			return textColor
		}

	}
}