package com.gmail.jorgegilcavazos.ballislife.features.tour;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.features.login.LoginActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TourLoginActivity extends AppCompatActivity {

    @BindView(R.id.text_positive) TextView tvPositive;
    @BindView(R.id.text_negative) TextView tvNegative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_login);
        ButterKnife.bind(this);

        tvNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TourLoginActivity.this, LoginActivity.class);
                startActivity(intent);

                finish();
            }
        });
    }
}
