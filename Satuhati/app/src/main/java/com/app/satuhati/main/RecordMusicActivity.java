package com.app.satuhati.main;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.app.satuhati.R;
import com.app.satuhati.base.BaseActivity;
import com.app.satuhati.commons.Commons;
import com.app.satuhati.commons.ReqConst;
import com.cunoraz.gifview.library.GifView;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class RecordMusicActivity extends BaseActivity {

    boolean recordFlag=false;
    boolean playFlag=false;

    private MediaRecorder myAudioRecorder=null;
    private MediaPlayer myPlayer;
    private String outputFile = "";
    private Handler mHandler = new Handler();

    SeekBar seekBar;
    ProgressBar progressBar;
    AVLoadingIndicatorView mainProgressBar;
    TextView sendAudio;
    ImageView record, play;
    public static final int RECORD_AUDIO = 0;

    GifView gifView;
    EditText inputBox;
    FrameLayout nameBox;
    TextView cancelBoxButton, sendButton;
    View boxBackground;
    File audioFile = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record_music);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        mainProgressBar = (AVLoadingIndicatorView) findViewById(R.id.loading_bar);
        inputBox = (EditText)findViewById(R.id.inputBox);
        nameBox = (FrameLayout)findViewById(R.id.nameBox);
        cancelBoxButton = (TextView)findViewById(R.id.cancel_button);
        sendButton = (TextView)findViewById(R.id.send_button);
        boxBackground = (View)findViewById(R.id.layout);

        inputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(inputBox.getText().toString().trim().length() > 0) sendButton.setVisibility(View.VISIBLE);
                else sendButton.setVisibility(View.GONE);
            }
        });

        cancelBoxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameBox.setVisibility(View.GONE);
                boxBackground.setVisibility(View.GONE);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputBox.getText().length() > 0){
                    nameBox.setVisibility(View.GONE);
                    boxBackground.setVisibility(View.GONE);
                    uploadMusic();
                }
            }
        });

        gifView = (GifView) findViewById(R.id.gif);
        gifView.play();

        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO);

        } else {
            seekBar = (SeekBar)findViewById(R.id.seekBar);

            play = (ImageView) findViewById(R.id.play);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!playFlag){
                        if(outputFile.length() == 0){
                            showToast(getString(R.string.record_music));
                            return;
                        }
                        playFlag = true;
                        try {
                            myPlayer = new MediaPlayer();
                            myPlayer.setDataSource(outputFile);
                            myPlayer.prepare();
                            myPlayer.start();
                            seekBar.setVisibility(View.VISIBLE);
                            seekBar.setMax(myPlayer.getDuration());
                            gifView.setVisibility(View.VISIBLE);

//Make sure you update Seekbar on UI thread
                            RecordMusicActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if(myPlayer != null){
                                        int mCurrentPosition = myPlayer.getCurrentPosition();
                                        seekBar.setProgress(mCurrentPosition);
                                        if(mCurrentPosition >= seekBar.getMax()){
                                            record.setEnabled(true);
                                            playFlag = false;
                                            play.setImageResource(R.drawable.audioplayicon);
                                            seekBar.setVisibility(View.INVISIBLE);
                                            gifView.setVisibility(View.GONE);
                                        }
                                    }
                                    mHandler.postDelayed(this, 1000);
                                }
                            });

                            showToast(getString(R.string.playing_music));

                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        play.setImageResource(R.drawable.audiostopicon);
                        //        play.setTextSize(11.00f);
                        record.setEnabled(false);
                    }else {
                        playFlag=false;
                        gifView.setVisibility(View.GONE);
                        try {
                            if (myPlayer != null) {
                                myPlayer.stop();
                                myPlayer.release();
                                myPlayer = null;
                                showToast(getString(R.string.stoped_playing));
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        play.setImageResource(R.drawable.audioplayicon);
                        //        play.setTextSize(11.00f);
                        record.setEnabled(true);
                    }
                }
            });
            record=(ImageView) findViewById(R.id.record);
            record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(!recordFlag){
                        recordFlag=true;
                        progressBar.setVisibility(View.VISIBLE);
                        try {
                            //        initAudio();
                            outputFile = Environment.getExternalStorageDirectory().
                                    getAbsolutePath() + "/" + getString(R.string.mymusic) + new Date().getTime()+".3gp";
                            myAudioRecorder = new MediaRecorder();
                            myAudioRecorder.reset();
                            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                            myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                            myAudioRecorder.setOutputFile(outputFile);
                            seekBar.setVisibility(View.INVISIBLE);
                            myAudioRecorder.prepare();
                            myAudioRecorder.start();
                        } catch (IllegalStateException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        record.setImageResource(R.drawable.audiorecordstopicon);
                        //        record.setTextSize(11.00f);
                        play.setEnabled(false);
                        showToast(getString(R.string.recording_started));
                    }else{
                        recordFlag=false;
                        progressBar.setVisibility(View.INVISIBLE);
                        myAudioRecorder.stop();
                        myAudioRecorder.release();
                        myAudioRecorder = null;

                        record.setImageResource(R.drawable.audiorecordicon);
                        //        record.setTextSize(11.00f);
                        play.setEnabled(true);
                        showToast(getString(R.string.music_recorded));
                    }

                }
            });
            sendAudio=(TextView)findViewById(R.id.sendAudio);
            sendAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    audioFile = new File(outputFile);
                    Log.d("FILExtension===>", FilenameUtils.getExtension(audioFile.getName()));

                    if(audioFile != null && FilenameUtils.getExtension(audioFile.getName()).length()>0) {
                        //                  showUploadAlert(Uri.fromFile(f));
                        record.setImageResource(R.drawable.audiorecordicon);
                        //        record.setTextSize(11.00f);
                        play.setEnabled(true);
                        record.setEnabled(true);
                        seekBar.setVisibility(View.INVISIBLE);
                        recordFlag=false;
                        nameBox.setVisibility(View.VISIBLE);
                        boxBackground.setVisibility(View.VISIBLE);
                    }
                    else showToast(getString(R.string.record_music));
                }
            });
        }
    }

    public void uploadMusic() {
        ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "addmusic")
                .addMultipartFile("file", audioFile)
                .addMultipartParameter("name", inputBox.getText().toString().trim())
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        String uploaded = String.valueOf((int)bytesUploaded*100/totalBytes);
                        Log.d("UPLOADED!!!", uploaded);
                        ((TextView)findViewById(R.id.progressText)).setText(uploaded);
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                progressBar.setVisibility(View.GONE);
                                ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.GONE);
                            }
                        });
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                showToast(getString(R.string.upload_success));
                                finish();

                            }else {
                                showToast(getString(R.string.server_issue));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                progressBar.setVisibility(View.GONE);
                                ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.GONE);
                            }
                        });
                        showToast(error.getErrorDetail());
                    }
                });
    }

}






















