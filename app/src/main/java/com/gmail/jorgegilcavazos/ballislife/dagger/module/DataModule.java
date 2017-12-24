package com.gmail.jorgegilcavazos.ballislife.dagger.module;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gmail.jorgegilcavazos.ballislife.BuildConfig;
import com.gmail.jorgegilcavazos.ballislife.data.local.LocalSharedPreferences;
import com.gmail.jorgegilcavazos.ballislife.data.service.HighlightsService;
import com.gmail.jorgegilcavazos.ballislife.data.service.NbaGamesService;
import com.gmail.jorgegilcavazos.ballislife.data.service.NbaService;
import com.gmail.jorgegilcavazos.ballislife.data.service.RedditGameThreadsService;
import com.gmail.jorgegilcavazos.ballislife.util.NetworkUtils;
import com.gmail.jorgegilcavazos.ballislife.util.NetworkUtilsImpl;
import com.google.gson.Gson;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;
import static com.gmail.jorgegilcavazos.ballislife.data.reddit.RedditAuthenticationImpl
        .REDDIT_AUTH_PREFS;

@Module
public class DataModule {

    String swishBaseUrl;
    String nbaBaseUrl;

    public DataModule(String swishBaseUrl, String nbaBaseUrl) {
        this.swishBaseUrl = swishBaseUrl;
        this.nbaBaseUrl = nbaBaseUrl;
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY :
                HttpLoggingInterceptor.Level.NONE);

        return new OkHttpClient.Builder().addInterceptor(interceptor).build();
    }

    @Provides
    @Singleton
    @Named("SwishBackend")
    Retrofit provideRetrofitForSwish(Gson gson, OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder().client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(swishBaseUrl)
                .build();

        return retrofit;
    }

    @Provides
    @Singleton
    @Named("NbaBackend")
    Retrofit provideRetrofitForNba(Gson gson, OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder().client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(nbaBaseUrl)
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

    @Provides
    @Singleton
    HighlightsService provideHighlightsService(@Named("SwishBackend") Retrofit retrofit) {
        return retrofit.create(HighlightsService.class);
    }

    @Provides
    @Singleton
    NbaGamesService provideNbaGamesService(@Named("SwishBackend") Retrofit retrofit) {
        return retrofit.create(NbaGamesService.class);
    }

    @Provides
    @Singleton
    RedditGameThreadsService provideRedditGameThreadsService(@Named("SwishBackend") Retrofit retrofit) {
        return retrofit.create(RedditGameThreadsService.class);
    }

    @Provides
    @Singleton
    NbaService provideNbaService(@Named("NbaBackend") Retrofit retrofit) {
        return retrofit.create(NbaService.class);
    }

    @Provides
    CompositeDisposable provideCompositeDisposables() {
        return new CompositeDisposable();
    }

    @Provides
    NetworkUtils provideNetworkUtils() {
        return new NetworkUtilsImpl();
    }
}
