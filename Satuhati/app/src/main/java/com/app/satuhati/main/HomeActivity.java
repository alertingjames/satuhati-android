package com.app.satuhati.main;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.app.satuhati.R;
import com.app.satuhati.adapters.MusicListAdapter;
import com.app.satuhati.base.BaseActivity;
import com.app.satuhati.commons.Commons;
import com.app.satuhati.commons.ReqConst;
import com.app.satuhati.models.Music;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.cunoraz.gifview.library.GifView;
import com.google.android.gms.maps.model.LatLng;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

public class HomeActivity extends BaseActivity{

    public AVLoadingIndicatorView progressBar;
    ListView list;

    ImageView searchButton, cancelButton;
    public LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView title;

    EditText inputBox;
    FrameLayout nameBox, musicLayout;
    TextView cancelBoxButton, sendButton;
    View boxBackground;

    File audioFile = null;
    private MediaPlayer myPlayer;
    private Handler mHandler = new Handler();
    SeekBar seekBar;
    TextView sendAudio;
    ImageView play;
    public static final int RECORD_AUDIO = 0;
    boolean playFlag=false;
    GifView gifView;

    LocationManager locationManager;

    ArrayList<Music> musicArrayList = new ArrayList<>();
    MusicListAdapter adapter = new MusicListAdapter(this);

    public ProgressBar gProg = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        list = (ListView) findViewById(R.id.list);

        setupUI(findViewById(R.id.activity), this);

        title = (TextView)findViewById(R.id.title);
        title.setText(getString(R.string.musics));

        searchBar = (LinearLayout)findViewById(R.id.search_bar);
        searchButton = (ImageView)findViewById(R.id.searchButton);
        cancelButton = (ImageView)findViewById(R.id.cancelButton);

        ui_edtsearch = (EditText)findViewById(R.id.edt_search);
        ui_edtsearch.setFocusable(true);
        ui_edtsearch.requestFocus();

        ui_edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = ui_edtsearch.getText().toString().trim().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        isLocationEnabled();

        initTabs(0);

        getMusics();

        inputBox = (EditText)findViewById(R.id.inputBox);
        nameBox = (FrameLayout)findViewById(R.id.nameBox);
        cancelBoxButton = (TextView)findViewById(R.id.cancel_button);
        sendButton = (TextView)findViewById(R.id.send_button);
        boxBackground = (View)findViewById(R.id.layout);
        musicLayout = (FrameLayout)findViewById(R.id.musicLayout);

        gifView = (GifView) findViewById(R.id.gif);
        gifView.play();

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
                    uploadMusic(audioFile);
                    if(myPlayer != null && myPlayer.isPlaying()){
                        myPlayer.stop();
                        myPlayer.release();
                        myPlayer = null;
                        playFlag = false;
                        play.setImageResource(R.drawable.audioplayicon);
                        seekBar.setVisibility(View.INVISIBLE);
                        gifView.setVisibility(View.GONE);
                    }
                }
            }
        });

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
                        if(audioFile == null){
                            return;
                        }
                        playFlag = true;
                        try {
                            myPlayer = new MediaPlayer();
                            myPlayer.setDataSource(audioFile.getAbsolutePath());
                            myPlayer.prepare();
                            myPlayer.start();
                            seekBar.setVisibility(View.VISIBLE);
                            seekBar.setMax(myPlayer.getDuration());
                            gifView.setVisibility(View.VISIBLE);

//Make sure you update Seekbar on UI thread
                            HomeActivity.this.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if(myPlayer != null){
                                        int mCurrentPosition = myPlayer.getCurrentPosition();
                                        seekBar.setProgress(mCurrentPosition);
                                        if(mCurrentPosition >= seekBar.getMax()){
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
                    }
                }
            });

            sendAudio=(TextView)findViewById(R.id.sendAudio);
            sendAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("FILExtension===>", FilenameUtils.getExtension(audioFile.getName()));
                    if(audioFile != null && FilenameUtils.getExtension(audioFile.getName()).length()>0) {
                        play.setEnabled(true);
                        seekBar.setVisibility(View.INVISIBLE);
                        musicLayout.setVisibility(View.GONE);
                        nameBox.setVisibility(View.VISIBLE);
                        boxBackground.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

    }

    public void closeMusicLayout(View view){
        musicLayout.setVisibility(View.GONE);
    }

    private void initTabs(int pos){

        AHBottomNavigation bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

// Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.musics, R.drawable.ic_music, R.color.colorPrimary);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.weather, R.drawable.ic_weather, R.color.colorPrimary);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.news, R.drawable.ic_news, R.color.colorPrimary);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.profile, R.drawable.account, R.color.colorPrimary);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.contact, R.drawable.contactus, R.color.colorPrimary);

        bottomNavigation.setAccentColor(getColor(R.color.colorPrimary));
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
//        bottomNavigation.setBehaviorTranslationEnabled(true);

// Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);
        bottomNavigation.addItem(item5);
        bottomNavigation.setCurrentItem(pos);

        // Set listeners
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                // Do something cool here...
                switch (position){
                    case 0:
                        onResume();
                        break;
                    case 1:
                        Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);
                        break;
                    case 2:
                        intent = new Intent(getApplicationContext(), NewsActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);
                        break;
                    case 3:
                        intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);
                        break;
                    case 4:
                        intent = new Intent(getApplicationContext(), ContactUsActivity.class);
                        startActivity(intent);
                        finish();
                        overridePendingTransition(0,0);
                        break;
                    default:

                }

                return true;
            }
        });

    }

    public void search(View view){
        cancelButton.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        searchBar.setVisibility(View.VISIBLE);
        title.setVisibility(View.GONE);
    }

    public void cancelSearch(View view){
        cancelButton.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.GONE);
        title.setVisibility(View.VISIBLE);
        ui_edtsearch.setText("");
    }

    public void addAudio(View view){
        showAlertDialogCapture();
    }

    @Override
    public void onResume() {
        super.onResume();

        checkForLocationPermission();

    }

    private void getMusics() {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "allmusics")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("MUSICRESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                musicArrayList.clear();
                                JSONArray dataArr = response.getJSONArray("data");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    Music music = new Music();
                                    music.setId(object.getInt("id"));
                                    music.setUser_id(object.getInt("member_id"));
                                    music.setUser_name(object.getString("member_name"));
                                    music.setName(object.getString("name"));
                                    music.setTime(object.getString("time"));
                                    music.setUrl(object.getString("url"));
                                    music.setLikes(Integer.parseInt(object.getString("likes")));
                                    music.setLiked(object.getString("liked").equals("yes"));

                                    musicArrayList.add(music);
                                }

                                if(musicArrayList.isEmpty())((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                                else ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.GONE);

                                adapter.setDatas(musicArrayList);
                                list.setAdapter(adapter);

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
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public ImageView likeButton;
    public TextView likesBox;
    public int musicId;

    public void likeMusic(String mId){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "likemusic")
                .addBodyParameter("music_id", mId)
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                if(likeButton != null){
                                    likeButton.setImageResource(R.drawable.ic_liked);
                                    for(Music music: musicArrayList){
                                        if(music.getId() == musicId){
                                            music.setLiked(true);
                                            music.setLikes(music.getLikes() + 1);
                                            likesBox.setText(String.valueOf(music.getLikes()));
                                        }
                                    }
                                }
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
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
//                        toInit();
                    }
                });
    }

    public void unLikeMusic(String mId){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "unlikemusic")
                .addBodyParameter("music_id", mId)
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                if(likeButton != null){
                                    likeButton.setImageResource(R.drawable.ic_like);
                                    for(Music music: musicArrayList){
                                        if(music.getId() == musicId){
                                            music.setLiked(false);
                                            if(music.getLikes() > 0){
                                                music.setLikes(music.getLikes() - 1);
                                                likesBox.setText(getString(R.string.likes) + ": " + String.valueOf(music.getLikes()));
                                            }
                                        }
                                    }
                                }
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
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
//                        toInit();
                    }
                });
    }

    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 7001;

    private void checkForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);

        } else {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location == null) location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null) location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (location != null) {
                try {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                    Commons.thisUser.setLatLng(latLng);
                    registerMemberLocation(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void registerMemberLocation(String lat, String lon) {
        AndroidNetworking.upload(ReqConst.SERVER_URL + "reglocation")
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.get_idx()))
                .addMultipartParameter("latitude", lat)
                .addMultipartParameter("longitude", lon)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("LOCRESPONSE!!!", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {

                    }
                });
    }


    private void isLocationEnabled() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    dialog.cancel();
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        } else {
            Log.d("Info+++", "Location enabled");
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                Commons.thisUser.setLatLng(latLng);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        Log.d("INFO+++", "GPS Provider enabled");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("INFO+++", "GPS Provider disabled");
                    }
                });
            }
            else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                Commons.thisUser.setLatLng(latLng);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        Log.d("INFO+++", "NETWORK Provider enabled");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("INFO+++", "NETWORK Provider disabled");
                    }
                });
            }
            else if(locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
                Log.d("Info+++", "Passive Location Provider enabled");
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                Commons.thisUser.setLatLng(latLng);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        Log.d("INFO+++", "PASSIVE Provider enabled");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("INFO+++", "PASSIVE Provider disabled");
                    }
                });
            }
        }
    }

    private void showAlertDialogCapture(){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_pick, null);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        dialog.show();

        TextView pickButton = (TextView)view.findViewById(R.id.pickButton);
        TextView recordButton = (TextView)view.findViewById(R.id.recordButton);
        TextView cancelButton = (TextView)view.findViewById(R.id.cancelButton);

        pickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent videoIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(videoIntent, getString(R.string.select_audio)), 1);
                dialog.dismiss();
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RecordMusicActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // The absolute width of the available display size in pixels.
        int displayWidth = displayMetrics.widthPixels;
        // The absolute height of the available display size in pixels.
        int displayHeight = displayMetrics.heightPixels;

        // Initialize a new window manager layout parameters
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        // Copy the alert dialog window attributes to new layout parameter instance
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        // Set alert dialog width equal to screen width 80%
        int dialogWindowWidth = (int) (displayWidth - displayWidth * 0.15);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                //the selected audio.
                Uri uri = data.getData();
                try {
                    String uriString = uri.toString();
                    File myFile = new File(uriString);
                    //    String path = myFile.getAbsolutePath();
                    String displayName = null;
                    String path2 = getAudioPath(uri);
                    File f = new File(path2);
                    long fileSizeInBytes = f.length();
                    long fileSizeInKB = fileSizeInBytes / 1024;
                    long fileSizeInMB = fileSizeInKB / 1024;
                    if (fileSizeInMB > 25) {
                        showToast("sorry file size is large");
                    } else {
                        audioFile = new File(path2);
                        musicLayout.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    //handle exception
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getAudioPath(Uri uri) {
        String[] data = {MediaStore.Audio.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void uploadMusic(File file) {
        ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "addmusic")
                .addMultipartFile("file", file)
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
                                ((LinearLayout)findViewById(R.id.progressLayout)).setVisibility(View.GONE);
                            }
                        });
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                showToast(getString(R.string.upload_success));
                                getMusics();
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

    public void deleteMusic(String musicId) {
        showAlertDialogForQuestion(getString(R.string.warning), getString(R.string.sure_delete), HomeActivity.this, null, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                progressBar.setVisibility(View.VISIBLE);
                AndroidNetworking.post(ReqConst.SERVER_URL + "delmusic")
                        .addBodyParameter("music_id", musicId)
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                Log.d("RESPONSE!!!", response.toString());
                                progressBar.setVisibility(View.GONE);
                                try {
                                    String result = response.getString("result_code");
                                    if(result.equals("0")){
                                        showToast(getString(R.string.deleted));
                                        getMusics();
                                    }else if(result.equals("1")){
                                        showToast(getString(R.string.no_exist));
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
                                progressBar.setVisibility(View.GONE);
                                showToast(error.getErrorDetail());
                            }
                        });
                return null;
            }
        });

    }


}













































