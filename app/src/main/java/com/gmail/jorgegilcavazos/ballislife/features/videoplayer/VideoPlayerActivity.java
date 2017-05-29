package com.gmail.jorgegilcavazos.ballislife.features.videoplayer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.gmail.jorgegilcavazos.ballislife.R;
import com.gmail.jorgegilcavazos.ballislife.data.API.StreamableService;
import com.gmail.jorgegilcavazos.ballislife.util.schedulers.SchedulerProvider;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VideoPlayerActivity extends AppCompatActivity implements VideoPlayerView ,
        EasyVideoCallback {
    private static final String TAG = "VideoPlayerActivity";

    public static final String SHORTCODE = "videoUrl";

    @BindView(R.id.player) EasyVideoPlayer videoPlayer;

    private VideoPlayerPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        presenter = new VideoPlayerPresenter(streamableService, SchedulerProvider.getInstance());
        presenter.attachView(this);

        String shortcode = getIntent().getStringExtra(SHORTCODE);
        presenter.loadStreamable(shortcode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.stop();
        presenter.detachView();
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
