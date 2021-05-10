package com.app.satuhati.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.satuhati.R;
import com.app.satuhati.base.BaseActivity;
import com.cunoraz.gifview.library.GifView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;

public class MusicPlayActivity extends BaseActivity {

    private MediaPlayer myPlayer = null;
    private String outputFile = "";
    private Handler mHandler = new Handler();

    SeekBar seekBar;
    ProgressBar progressBar;
    ImageView play;
    ImageButton audioRestartButton;
    public static final int RECORD_AUDIO = 0;
    GifView gifView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_music_play);

        String title = getIntent().getStringExtra("title");
        String audioUrl = getIntent().getStringExtra("audioUrl");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        ((TextView)findViewById(R.id.title)).setText(title);

        gifView = (GifView) findViewById(R.id.gif);
        gifView.play();
        outputFile = audioUrl;
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        play = (ImageView) findViewById(R.id.playButton);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);

        } else {
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(myPlayer != null && !myPlayer.isPlaying()){
                        try {
                            myPlayer.start();
                            seekBar.setVisibility(View.VISIBLE);
                            seekBar.setMax(myPlayer.getDuration());
                            gifView.setVisibility(View.VISIBLE);

//Make sure you update Seekbar on UI thread
                            MusicPlayActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(myPlayer != null){
                                        int mCurrentPosition = myPlayer.getCurrentPosition();
                                        seekBar.setProgress(mCurrentPosition);
                                        if(mCurrentPosition>=seekBar.getMax()){
                                            play.setImageResource(R.drawable.audioplayicon);
                                            seekBar.setVisibility(View.INVISIBLE);
                                            gifView.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    mHandler.postDelayed(this, 1000);
                                }
                            });

                            showToast(getString(R.string.resuming_music));

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        play.setImageResource(R.drawable.audiostopicon);
                    }else {
                        gifView.setVisibility(View.INVISIBLE);
                        try {
                            if (myPlayer != null) {
                                myPlayer.pause();
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        play.setImageResource(R.drawable.audioplayicon);
                    }
                }
            });

            audioRestartButton = (ImageButton)findViewById(R.id.startButton);
            audioRestartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(myPlayer.isPlaying()) myPlayer.stop();
                    myPlayer.release();
                    myPlayer = null;
                    progressBar.setVisibility(View.VISIBLE);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            initPlay();
                        }
                    });
                }
            });

            progressBar.setVisibility(View.VISIBLE);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    initPlay();
                }
            });
        }

    }

    private void initPlay(){
        if(myPlayer == null){
            try {
                myPlayer = new MediaPlayer();
                myPlayer.setDataSource(outputFile);
                myPlayer.prepare();
                myPlayer.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seekBar.setVisibility(View.VISIBLE);
                        seekBar.setMax(myPlayer.getDuration());
                        gifView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        play.setImageResource(R.drawable.audiostopicon);
                    }
                });

//Make sure you update Seekbar on UI thread
                MusicPlayActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            int mCurrentPosition = myPlayer.getCurrentPosition();
                            Log.d("Player Pos!!!", String.valueOf(mCurrentPosition) + " / " + String.valueOf(seekBar.getMax()));
                            seekBar.setProgress(mCurrentPosition);
                            if(mCurrentPosition >= seekBar.getMax() - 50){
                                play.setImageResource(R.drawable.audioplayicon);
                                seekBar.setVisibility(View.INVISIBLE);
                                gifView.setVisibility(View.INVISIBLE);
                            }
                            mHandler.postDelayed(this, 1000);
                        }catch (NullPointerException e){
                            e.fillInStackTrace();
                        }
                    }
                });

                showToast(getString(R.string.playing_music));

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(myPlayer != null && myPlayer.isPlaying()) myPlayer.stop();
        myPlayer.release();
        myPlayer = null;
    }
}

















