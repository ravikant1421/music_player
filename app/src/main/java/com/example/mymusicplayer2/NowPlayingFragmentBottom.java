package com.example.mymusicplayer2;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mymusicplayer2.MainActivity.ARTIST_TO_FRAG;
import static com.example.mymusicplayer2.MainActivity.PATH_TO_FRAG;
import static com.example.mymusicplayer2.MainActivity.SHOW_MINI_PLAYER;
import static com.example.mymusicplayer2.MainActivity.SONG_NAME_TO_FRAG;
import static com.example.mymusicplayer2.MainActivity.isBottomFragShown;

import android.content.Intent;
import android.content.SharedPreferences;

import android.media.MediaMetadataRetriever;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;


public class NowPlayingFragmentBottom extends Fragment {
    public ImageView skipNextBottom, skipPreviousBottom, bottomAlbumArt;
    public TextView songNameMiniPlayer, artistNameMiniPlayer;

    FloatingActionButton playPauseMiniPlayerButton;

    FrameLayout bottomLayout;
    static View view;
    MusicService musicService;

    public static boolean showLayoutFlag = false;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";

    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_now_playing_bottom, container, false);
        skipNextBottom = view.findViewById(R.id.skipNextBottom);
        skipPreviousBottom = view.findViewById(R.id.skipPreviousBottom);
        songNameMiniPlayer = view.findViewById(R.id.songNameMiniPlayer);
        artistNameMiniPlayer = view.findViewById(R.id.artistNameMiniPlayer);
        playPauseMiniPlayerButton = view.findViewById(R.id.playPauseMiniPlayerButton);
        bottomAlbumArt = view.findViewById(R.id.bottomAlbumArt);
        bottomLayout = view.findViewById(R.id.bottomLayout);
        return view;
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        byte[] art;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        art=retriever.getEmbeddedPicture();
        retriever.release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            retriever.close();
        }
        return art;
    }
    @Override
    public void onResume() {
        super.onResume();
        isBottomFragShown = true;
        musicService = PlayerActivity.musicService;
        if (musicService != null && musicService.isPlaying()) {
            playPauseMiniPlayerButton.setImageResource(R.drawable.ic_pause);
        } else {
            playPauseMiniPlayerButton.setImageResource(R.drawable.ic_play);
        }
        bottomLayout.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), PlayerActivity.class);
            intent.putExtra("FromBottomFrag",true);
            startActivity(intent);
        });
        if (showLayoutFlag) {
            bottomLayout.setVisibility(View.VISIBLE);
        }
        skipNextBottom.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.nextBtnClicked();
                if(musicService.isPlaying()){
                    changeMetaDataFragBottom(R.drawable.ic_pause);
                }
                else{
                    changeMetaDataFragBottom(R.drawable.ic_play);
                }
            }
        });
        playPauseMiniPlayerButton.setOnClickListener(v -> {
            if(musicService !=null){
                musicService.playPauseBtn();
                if (musicService.isPlaying()) {
                    playPauseMiniPlayerButton.setImageResource(R.drawable.ic_pause);
                } else {
                    playPauseMiniPlayerButton.setImageResource(R.drawable.ic_play);
                }
            }
        });
        skipPreviousBottom.setOnClickListener(v -> {
            if (musicService != null) {
                musicService.previousBtnClicked();
                if(musicService.isPlaying()){
                    changeMetaDataFragBottom(R.drawable.ic_pause);
                }
                else{
                    changeMetaDataFragBottom(R.drawable.ic_play);
                }
            }
        });
        if (SHOW_MINI_PLAYER) {
            if (PATH_TO_FRAG != null) {
                if (getContext() != null) {
                    byte[] art ;
                    try {
                        art=getAlbumArt(PATH_TO_FRAG);
                    }
                    catch (Exception e){
                        art=null;
                    }
                    if (art != null) {
                        Glide.with(getContext())
                                .load(art)
                                .into(bottomAlbumArt);
                    } else {
                        Glide.with(getContext())
                                .load(R.drawable.ic_music_player)
                                .into(bottomAlbumArt);
                    }
                    songNameMiniPlayer.setText(SONG_NAME_TO_FRAG);
                    artistNameMiniPlayer.setText(ARTIST_TO_FRAG);
                }

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isBottomFragShown = false;
    }

    public void changeMetaDataFragBottom(int playPauseIc) {
        if (view.getContext()!= null) {
            SharedPreferences preferences=view.getContext().getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
            String path=preferences.getString(MUSIC_FILE,null);
            String artistName=preferences.getString(ARTIST_NAME,"");
            String songName=preferences.getString(SONG_NAME,"");
            byte[] art;
            try {
                art=getAlbumArt(path);
            }
            catch (Exception e){
                art=null;
            }
            if (art != null) {
                Glide.with(view.getContext())
                        .asBitmap()
                        .load(art)
                        .into((ImageView) view.findViewById(R.id.bottomAlbumArt));
            } else {
                Glide.with(view.getContext())
                        .asBitmap()
                        .load(R.drawable.ic_music_player)
                        .into((ImageView) view.findViewById(R.id.bottomAlbumArt));
            }
            songNameMiniPlayer = view.findViewById(R.id.songNameMiniPlayer);
            artistNameMiniPlayer = view.findViewById(R.id.artistNameMiniPlayer);
            playPauseMiniPlayerButton = view.findViewById(R.id.playPauseMiniPlayerButton);
            songNameMiniPlayer.setText(songName);
            artistNameMiniPlayer.setText(artistName);
            playPauseMiniPlayerButton.setImageResource(playPauseIc);

        }
    }
}