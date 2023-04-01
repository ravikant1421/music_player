package com.example.mymusicplayer2;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mymusicplayer2.MainActivity.ARTIST_TO_FRAG;
import static com.example.mymusicplayer2.MainActivity.PATH_TO_FRAG;
import static com.example.mymusicplayer2.MainActivity.SHOW_MINI_PLAYER;
import static com.example.mymusicplayer2.MainActivity.SONG_NAME_TO_FRAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageView skipNextBottom,skipPreviousBottom,bottomAlbumArt;
    TextView songNameMiniPlayer,artistNameMiniPlayer;
    FloatingActionButton playPauseMiniPlayerButton;
    View view;
    MusicService musicService;
    private boolean mBound = false;
    public static final String MUSIC_LAST_PLAYED="LAST_PLAYED";
    public static final String MUSIC_FILE="STORED_MUSIC";
    public static final String ARTIST_NAME="ARTIST NAME";
    public static final String SONG_NAME="SONG NAME";
    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);
        skipNextBottom=view.findViewById(R.id.skipNextBottom);
        skipPreviousBottom=view.findViewById(R.id.skipPreviousBottom);
        bottomAlbumArt=view.findViewById(R.id.bottomAlbumArt);
        songNameMiniPlayer =view.findViewById(R.id.songNameMiniPlayer);
        artistNameMiniPlayer=view.findViewById(R.id.artistNameMiniPlayer);
        playPauseMiniPlayerButton=view.findViewById(R.id.playPauseMiniPlayerButton);
        return view;
    }
    private byte[] getAlbumArt(String uri)
    {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        return retriever.getEmbeddedPicture();
    }

    @Override
    public void onResume() {
        super.onResume();
        skipNextBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicService!=null) {
                    musicService.nextBtnClicked();
                    if(getActivity()!=null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                        editor.putString(ARTIST_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.musicFiles.get(musicService.position).getTitle());
                        editor.apply();
                        SharedPreferences preferences=getActivity().getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
                        String path=preferences.getString(MUSIC_FILE,null);
                        String artist=preferences.getString(ARTIST_NAME,null);
                        String songName=preferences.getString(SONG_NAME,null);
                        if(path!=null)
                        {
                            SHOW_MINI_PLAYER=true;
                            PATH_TO_FRAG =path;
                            ARTIST_TO_FRAG=artist;
                            SONG_NAME_TO_FRAG= songName;
                        }
                        else {
                            SHOW_MINI_PLAYER=false;
                            PATH_TO_FRAG=null;
                            ARTIST_TO_FRAG=null;
                            SONG_NAME_TO_FRAG= null;
                        }


                        if(SHOW_MINI_PLAYER)
                        {
                            if (PATH_TO_FRAG!=null){
                                byte[] art=getAlbumArt(PATH_TO_FRAG);
                                if(art!=null) {
                                    Glide.with(getContext())
                                            .load(art)
                                            .into(bottomAlbumArt);
                                }
                                else {
                                    Glide.with(getContext())
                                            .load(R.drawable.ic_baseline_music_note_24)
                                            .into(bottomAlbumArt);
                                }
                                songNameMiniPlayer.setText(SONG_NAME_TO_FRAG);
                                artistNameMiniPlayer.setText(ARTIST_TO_FRAG);
                            }
                        }
                    }
                }
            }
        });
        playPauseMiniPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService!=null) {
                    musicService.playPauseBtn();
                    if(musicService.isPlaying()){
                        playPauseMiniPlayerButton.setImageResource(R.drawable.ic_pause);
                    }else {
                        playPauseMiniPlayerButton.setImageResource(R.drawable.ic_play);
                    }
                }

            }
        });
        skipPreviousBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService!=null) musicService.previousBtnClicked();

            }
        });
        if(SHOW_MINI_PLAYER)
        {
            if (PATH_TO_FRAG!=null){
                if(getContext()!=null) {
                    byte[] art = getAlbumArt(PATH_TO_FRAG);
                    if (art != null) {
                        Glide.with(getContext())
                                .load(art)
                                .into(bottomAlbumArt);
                    } else {
                        Glide.with(getContext())
                                .load(R.drawable.ic_baseline_music_note_24)
                                .into(bottomAlbumArt);
                    }
                    songNameMiniPlayer.setText(SONG_NAME_TO_FRAG);
                    artistNameMiniPlayer.setText(ARTIST_TO_FRAG);
                    Intent intent = new Intent(getContext(), MusicService.class);
                    if (getContext() != null) {
                        getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                    }
                }

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBound) {
            if (getContext()!=null){
                getContext().unbindService(this);
                mBound=false;
            }
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder=(MusicService.MyBinder) service;
        musicService=binder.getService();
        mBound=true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService=null;
        mBound=false;

    }
}