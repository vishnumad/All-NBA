package com.gmail.jorgegilcavazos.ballislife.features.tour;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.features.login.LoginActivity;
import com.gmail.jorgegilcavazos.ballislife.features.main.BaseNoActionBarActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TourLoginActivity extends BaseNoActionBarActivity {

    @BindView(R.id.text_positive) TextView tvPositive;
    @BindView(R.id.text_negative) TextView tvNegative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_login);
        ButterKnife.bind(this);

        tvNegative.setOnClickListener(v -> finish());

        tvPositive.setOnClickListener(v -> {
            Intent intent = new Intent(TourLoginActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();
        });
    }

    @Override
    public void injectAppComponent() {
        BallIsLifeApplication.getAppComponent().inject(this);
    }
}
