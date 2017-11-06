package com.gmail.jorgegilcavazos.ballislife.features.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.main.BaseActionBarActivity;
import com.gmail.jorgegilcavazos.ballislife.util.Constants;

public class SettingsActivity extends BaseActionBarActivity
        implements BillingProcessor.IBillingHandler {

    private BillingProcessor billingProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String billingLicense = getString(R.string.play_billing_license_key);
        billingProcessor = new BillingProcessor(this, billingLicense, this);
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

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails
            details) {
        if (productId.equals(Constants.PREMIUM_PRODUCT_ID)) {
            Toast.makeText(this, R.string.purchase_complete, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    public boolean isPremium() {
        return billingProcessor.isPurchased(Constants.PREMIUM_PRODUCT_ID)
                || localRepository.isUserWhitelisted();
    }

    public void purchasePremium() {
        billingProcessor.purchase(this, Constants.PREMIUM_PRODUCT_ID);
    }
}
