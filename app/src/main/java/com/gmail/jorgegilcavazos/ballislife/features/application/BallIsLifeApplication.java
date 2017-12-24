package com.gmail.jorgegilcavazos.ballislife.features.application;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.gmail.jorgegilcavazos.ballislife.BuildConfig;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.dagger.component.AppComponent;
import com.gmail.jorgegilcavazos.ballislife.dagger.component.DaggerAppComponent;
import com.gmail.jorgegilcavazos.ballislife.dagger.module.AppModule;
import com.gmail.jorgegilcavazos.ballislife.dagger.module.DataModule;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.leakcanary.LeakCanary;

import jonathanfinerty.once.Once;
import timber.log.Timber;

public class BallIsLifeApplication extends Application implements BillingProcessor.IBillingHandler {

    private static Context context;
    private static AppComponent appComponent;

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    public static Context getAppContext() {
        return context;
    }

    private BillingProcessor billingProcessor;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        Once.initialise(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .dataModule(new DataModule("https://nba-app-ca681.firebaseio.com/",
                        "http://data.nba.com/"))
                .build();

        String billingLicense = getString(R.string.play_billing_license_key);
        billingProcessor = new BillingProcessor(this, billingLicense, this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        if (productId.equals(Constants.PREMIUM_PRODUCT_ID)) {
            Toast.makeText(this, R.string.purchase_complete, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        FirebaseCrash.report(error);
    }

    @Override
    public void onBillingInitialized() {

    }

    public BillingProcessor getBillingProcessor() {
        return billingProcessor;
    }
}
