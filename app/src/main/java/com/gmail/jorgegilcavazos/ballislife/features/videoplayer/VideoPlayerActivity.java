package com.gmail.jorgegilcavazos.ballislife.features.videoplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.service.StreamableService;
import com.gmail.jorgegilcavazos.ballislife.features.application.BallIsLifeApplication;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.BaseSchedulerProvider;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoPlayerActivity extends AppCompatActivity implements VideoPlayerView,
        EasyVideoCallback {
    public static final String SHORTCODE = "videoUrl";
    private static final String TAG = "VideoPlayerActivity";

    @Inject
    BaseSchedulerProvider schedulerProvider;

    @BindView(R.id.player) EasyVideoPlayer videoPlayer;

    private VideoPlayerPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BallIsLifeApplication.getAppComponent().inject(this);
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);

        videoPlayer.setCallback(this);
        videoPlayer.setAutoPlay(true);
        videoPlayer.setLoop(true);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.streamable.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        StreamableService streamableService = retrofit.create(StreamableService.class);

        presenter = new VideoPlayerPresenter(streamableService, schedulerProvider);
        presenter.attachView(this);

        String shortcode = getIntent().getStringExtra(SHORTCODE);
        presenter.loadStreamable(shortcode);

        //Strange bug with Easy Video player messed up aspect ratio if you changed orientation
        //before the video started playing
        //https://github.com/afollestad/easy-video-player/issues/25
        if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onDestroy() {
        presenter.stop();
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.pause();
    }

    @Override
    public void playStreamable(String videoUrl) {
        videoPlayer.setSource(Uri.parse("http:" + videoUrl));
    }

    @Override
    public void showCouldNotLoadVideoToast() {
        Toast.makeText(this, R.string.cannot_play_video, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {
        //If you don't use a timer the aspect ratio still misbehaves
        //https://github.com/afollestad/easy-video-player/issues/25
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                VideoPlayerActivity.this.runOnUiThread(() -> setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR));
            }
        };
        timer.schedule(task, 400);
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {

    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {

    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {

    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
        presenter.onErrorPlayingVideo(e);
    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {

    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {

    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {

    }
}
