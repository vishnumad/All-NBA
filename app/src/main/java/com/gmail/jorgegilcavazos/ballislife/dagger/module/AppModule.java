package com.gmail.jorgegilcavazos.ballislife.dagger.module;

import android.app.Application;
import android.content.Context;

import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.google.firebase.analytics.FirebaseAnalytics;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return application;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return application;
    }

    @Provides
    @Singleton
    public FirebaseAnalytics provideFirebaseAnalytics() {
        return ((BallIsLifeApplication) application).getFirebaseAnalytics();
    }
}
