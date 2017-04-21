package com.gmail.jorgegilcavazos.ballislife.features.videoplayer;

public interface VideoPlayerView {

    void playStreamable(String videoUrl);

    void showCouldNotLoadVideoToast();

    void finishActivity();
}
