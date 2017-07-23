package com.gmail.jorgegilcavazos.ballislife.dagger.module;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gmail.jorgegilcavazos.ballislife.data.local.LocalSharedPreferences;
import com.google.gson.Gson;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;
import static com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthenticationImpl
        .REDDIT_AUTH_PREFS;

@Module
public class DataModule {

    String baseUrl;

    public DataModule(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Gson gson) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();

        return retrofit;
    }

    @Provides
    @Singleton
    @Named("localSharedPreferences")
    SharedPreferences provideLocalSharedPreferences(Application app) {
        return app.getSharedPreferences(LocalSharedPreferences.LOCAL_APP_PREFS, MODE_PRIVATE);
    }

    @Provides
    @Singleton
    @Named("redditSharedPreferences")
    SharedPreferences provideRedditSharedPreferences(Application app) {
        return app.getSharedPreferences(REDDIT_AUTH_PREFS, MODE_PRIVATE);
    }

    @Provides
    @Singleton
    @Named("defaultSharedPreferences")
    SharedPreferences provideDefaultSharedPreferences(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }
}
