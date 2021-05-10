package com.app.satuhati.adapters;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.app.satuhati.R;
import com.app.satuhati.commons.Commons;
import com.app.satuhati.main.HomeActivity;
import com.app.satuhati.main.MusicPlayActivity;
import com.app.satuhati.models.Music;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

public class MusicListAdapter extends BaseAdapter {

    private HomeActivity _context;
    private ArrayList<Music> _datas = new ArrayList<>();
    private ArrayList<Music> _alldatas = new ArrayList<>();

    public MusicListAdapter(HomeActivity context){

        super();
        this._context = context;
    }

    public void setDatas(ArrayList<Music> datas) {

        _alldatas = datas;
        _datas.clear();
        _datas.addAll(_alldatas);
    }

    @Override
    public int getCount(){
        return _datas.size();
    }

    @Override
    public Object getItem(int position){
        return _datas.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        CustomHolder holder;

        if (convertView == null) {
            holder = new CustomHolder();

            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.item_music, parent, false);

            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.username = (TextView) convertView.findViewById(R.id.user);
            holder.dateTime = (TextView) convertView.findViewById(R.id.datetime);
            holder.audio = (ImageView) convertView.findViewById(R.id.audio);
            holder.likeButton = (ImageView) convertView.findViewById(R.id.likeButton);
            holder.likesBox = (TextView) convertView.findViewById(R.id.likes);
            holder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);

            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        final Music entity = (Music) _datas.get(position);

        holder.title.setText(entity.getName());

        String[] monthes={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

        Calendar c = Calendar.getInstance();
        //Set time in milliseconds
        c.setTimeInMillis(Long.parseLong(entity.getTime()));
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMin = c.get(Calendar.MINUTE);
        if(mDay<10)
            holder.dateTime.setText(monthes[mMonth] + " 0" + mDay + ", " + mYear + " " + mHour + ":" + mMin);
        else
            holder.dateTime.setText(monthes[mMonth] + " " + mDay + ", " + mYear + " " + mHour + ":" + mMin);
        holder.username.setText(entity.getUser_name());

        if(entity.getUser_id() == Commons.thisUser.get_idx())holder.username.setText(_context.getString(R.string.me));
        if(entity.isLiked())holder.likeButton.setImageResource(R.drawable.ic_liked);
        else holder.likeButton.setImageResource(R.drawable.ic_like);
        holder.likesBox.setText(_context.getString(R.string.likes) + ": " + String.valueOf(entity.getLikes()));

        if(entity.getUser_id() == Commons.thisUser.get_idx()){
            holder.likeButton.setImageResource(R.drawable.ic_trash);
        }

        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(entity.getUser_id() == Commons.thisUser.get_idx()){
                    _context.deleteMusic(String.valueOf(entity.getId()));
                }else {
                    if(entity.isLiked())_context.unLikeMusic(String.valueOf(entity.getId()));
                    else _context.likeMusic(String.valueOf(entity.getId()));
                    _context.likeButton = holder.likeButton;
                    _context.likesBox = holder.likesBox;
                    _context.musicId = entity.getId();
                }
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_context.gProg != null)_context.gProg.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);
                _context.gProg = holder.progressBar;
                Intent intent = new Intent(_context, MusicPlayActivity.class);
                intent.putExtra("title", entity.getName());
                intent.putExtra("audioUrl", entity.getUrl());
                _context.startActivity(intent);
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(entity.getUrl()));
//                _context.startActivity(browserIntent);
            }
        });

        return convertView;
    }

    public void filter(String charText){

        charText = charText.toLowerCase();
        _datas.clear();

        if(charText.length() == 0){
            _datas.addAll(_alldatas);
        }else {

            for (Music music : _alldatas){

                if (music instanceof Music) {

                    String value = music.getName().toLowerCase();
                    if (value.contains(charText)) {
                        _datas.add(music);
                    }
                    else {
                        value = music.getUser_name().toLowerCase();
                        if (value.contains(charText)) {
                            _datas.add(music);
                        }
                        else {
                            String timeStamp = music.getTime();
                            String[] monthes={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

                            Calendar c = Calendar.getInstance();
                            //Set time in milliseconds
                            c.setTimeInMillis(Long.parseLong(timeStamp));
                            int mYear = c.get(Calendar.YEAR);
                            int mMonth = c.get(Calendar.MONTH);
                            int mDay = c.get(Calendar.DAY_OF_MONTH);
                            int mHour = c.get(Calendar.HOUR_OF_DAY);
                            int mMin = c.get(Calendar.MINUTE);
                            if(mDay<10)
                                value = monthes[mMonth] + " 0" + mDay + ", " + mYear + " " + mHour + ":" + mMin;
                            else
                                value = monthes[mMonth] + " " + mDay + ", " + mYear + " " + mHour + ":" + mMin;
                            Log.d("DATETIME===>", value);
                            if (value.contains(charText) || value.startsWith(charText)) {
                                _datas.add(music);
                            }
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    class CustomHolder {
        ImageView audio, likeButton;
        TextView title, dateTime, username, likesBox;
        ProgressBar progressBar;
    }
}



