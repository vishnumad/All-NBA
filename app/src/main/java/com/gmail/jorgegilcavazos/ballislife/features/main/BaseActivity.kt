package com.gmail.jorgegilcavazos.ballislife.features.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository
import com.gmail.jorgegilcavazos.ballislife.features.model.SwishTheme
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

	@Inject lateinit var localRepository: LocalRepository

	override fun onCreate(savedInstanceState: Bundle?) {
		injectAppComponent()
		setAppTheme()
		super.onCreate(savedInstanceState)
	}

	abstract fun injectAppComponent()

	fun setToolbarPopupTheme(toolbar: Toolbar) {
		if (localRepository.appTheme === SwishTheme.DARK) {
			toolbar.popupTheme = R.style.AppTheme_PopupOverlay_Dark
		} else {
			toolbar.popupTheme = R.style.AppTheme_PopupOverlay_Light
		}
	}

	private fun setAppTheme() {
		when (localRepository.appTheme) {
			SwishTheme.LIGHT -> setTheme(R.style.AppTheme_NoActionBar)
			SwishTheme.DARK -> setTheme(R.style.AppTheme_Dark_NoActionBar)
			null -> setTheme(R.style.AppTheme_NoActionBar)
		}
	}
}
