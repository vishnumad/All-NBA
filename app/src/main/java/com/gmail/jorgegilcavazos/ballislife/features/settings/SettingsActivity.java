package com.gmail.jorgegilcavazos.ballislife.features.settings;

import android.os.Bundle;
import android.view.MenuItem;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.main.BaseActionBarActivity;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;

public class SettingsActivity extends BaseActionBarActivity {

    private BillingProcessor billingProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        billingProcessor = ((BallIsLifeApplication) getApplication()).getBillingProcessor();
    }

    @Override
    public void injectAppComponent() {
        BallIsLifeApplication.getAppComponent().inject(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isPremium() {
        return billingProcessor.isPurchased(Constants.PREMIUM_PRODUCT_ID)
                || localRepository.isUserWhitelisted();
    }
}
