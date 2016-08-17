package com.example.jorgegil.closegamealert.View.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.jorgegil.closegamealert.R;


public class BoxScoreFragment extends Fragment {
    private static final String ESPN_BOX_SCORE_URL = "http://espn.go.com/nba/gamecast?version=mobile&gcSection=boxscore&gameId=";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_box_score, container, false);

        String gameId = getArguments().getString("gameId");

        WebView webView = (WebView) view.findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
        webView.loadUrl(ESPN_BOX_SCORE_URL + gameId);
        return view;
    }
}
