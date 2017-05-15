package com.gmail.jorgegilcavazos.ballislife.features.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import jonathanfinerty.once.Once;

public class BallIsLifeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Once.initialise(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
