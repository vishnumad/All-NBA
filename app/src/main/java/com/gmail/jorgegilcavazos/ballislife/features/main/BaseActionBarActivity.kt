package com.gmail.jorgegilcavazos.ballislife.features.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.gmail.jorgegilcavazos.ballislife.R
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalRepository
import com.gmail.jorgegilcavazos.ballislife.features.model.SwishTheme
import javax.inject.Inject

abstract class BaseActionBarActivity : AppCompatActivity() {
	@Inject lateinit var localRepository: LocalRepository

	override fun onCreate(savedInstanceState: Bundle?) {
		injectAppComponent()
		setAppTheme()
		super.onCreate(savedInstanceState)
	}

	abstract fun injectAppComponent()

	private fun setAppTheme() {
		when (localRepository.appTheme) {
			SwishTheme.LIGHT -> setTheme(R.style.AppTheme)
			SwishTheme.DARK -> setTheme(R.style.AppTheme_Dark)
			null -> setTheme(R.style.AppTheme)
		}
	}
}