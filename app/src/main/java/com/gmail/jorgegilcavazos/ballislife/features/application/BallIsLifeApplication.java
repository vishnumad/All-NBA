package com.gmail.jorgegilcavazos.ballislife.features.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.gmail.jorgegilcavazos.ballislife.dagger.component.AppComponent;
import com.gmail.jorgegilcavazos.ballislife.dagger.component.DaggerAppComponent;
import com.gmail.jorgegilcavazos.ballislife.dagger.module.AppModule;
import com.gmail.jorgegilcavazos.ballislife.dagger.module.DataModule;

import jonathanfinerty.once.Once;

public class BallIsLifeApplication extends Application {

    private static AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        Once.initialise(this);
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .dataModule(new DataModule("https://nba-app-ca681.firebaseio.com/"))
                .build();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static AppComponent getAppComponent() {
        return appComponent;
    }
}
