package com.example.mymusicplayer2;

import static com.example.mymusicplayer2.AlbumDetails.albumMusicFiles;
import static com.example.mymusicplayer2.ArtistDetails.artistMusicFiles;
import static com.example.mymusicplayer2.FolderDetails.folderMusicFiles;
import static com.example.mymusicplayer2.MainActivity.isBottomFragShown;
import static com.example.mymusicplayer2.MainActivity.isPlayerActivityShown;
import static com.example.mymusicplayer2.MainActivity.musicFiles;
import static com.example.mymusicplayer2.MainActivity.repeatBoolean;
import static com.example.mymusicplayer2.MainActivity.shuffleBoolean;

import static com.example.mymusicplayer2.NowPlayingFragmentBottom.showLayoutFlag;
import static com.example.mymusicplayer2.RecentDbHelper.musicFilesArrayList;

import android.content.ComponentName;

import android.content.Intent;
import android.content.ServiceConnection;

import android.media.MediaMetadataRetriever;


import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Random;


public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {
    TextView songName, durationPlayed, durationTotal, songArtist, headerSongName;
    ImageView coverArt, nextButton, backButton, repeatButton, shuffleButton, prevButton;
    FloatingActionButton playPauseButton;
    SeekBar seekBar;

    public static int position = -1;
    public boolean isPlayingBeforeSeeking;
    static Uri uri;
    private final Handler handler = new Handler();
    public Thread playThread, prevThread, nextThread;
    public static ArrayList<MusicFiles> songsList = new ArrayList<>();
    public static MusicService musicService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        init();
        getIntentMethod();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (musicService != null) {
                    isPlayingBeforeSeeking = musicService.isPlaying();
                    musicService.pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicService != null) {
                    if (isPlayingBeforeSeeking) {
                        musicService.start();
                    }
                }
            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            /*  runOnUiThread(new Runnable()) is a method call that executes the specified Runnable object on the UI thread of the current activity.
                Runnable is an interface that represents a task that can be executed in a separate thread.
                It defines a single method run() that contains the code to be executed in the separate thread.
             */
            @Override
            public void run() {
                if (musicService != null) {
                    int currentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                    durationPlayed.setText(formattedTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });
        shuffleButton.setOnClickListener(v -> {
            if (shuffleBoolean) {
                shuffleBoolean = false;
                shuffleButton.setImageResource(R.drawable.ic_shuffle_off);
            } else {
                shuffleBoolean = true;
                shuffleButton.setImageResource(R.drawable.ic_shuffle_on);
            }
        });
        repeatButton.setOnClickListener(v -> {
            if (repeatBoolean) {
                repeatBoolean = false;
                repeatButton.setImageResource(R.drawable.ic_repeat_off);
            } else {
                repeatBoolean = true;
                repeatButton.setImageResource(R.drawable.ic_repeat_on);
            }
        });

    }

    private void setFullScreen() {
        /*requestWindowFeature() is used to ask the system to include or exclude some of windows features (toolbar, actionbar and so on) here window is current ui
          FEATURE_NO_TITLE it is used to set fullscreen
         */

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /*
        The second parameter is also WindowManager.LayoutParams.FLAG_FULLSCREEN, which means that we want to set all bits of the flag specified in the first parameter.
         */
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPlayerActivityShown = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPlayerActivityShown = true;
        if (!showLayoutFlag) {
            showLayoutFlag = true;
        }

        Intent intent = new Intent(this, MusicService.class);
        /* bindService(intent, this, BIND_AUTO_CREATE) is a method call that binds the specified service to the current component
           (which is likely an activity, fragment, or service)
           BIND_AUTO_CREATE: This parameter specifies the binding options for the service. In this case,
           BIND_AUTO_CREATE indicates that the service should be created if it does not already exist
            bindService(intent,this,BIND_AUTO_CREATE); here this is the instance of ServiceConnection interface .
         */

        boolean isFromBottomFrag = getIntent().getBooleanExtra("FromBottomFrag", false);
        boolean isFromNotification = getIntent().getBooleanExtra("FromNotification", false);
        if (!isFromBottomFrag || !isFromNotification) {
            bindService(intent, this, BIND_AUTO_CREATE);
        }
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                playPauseButton.setOnClickListener(v -> playPauseBtnClicked());
            }
        };
        playThread.start();
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                nextButton.setOnClickListener(v -> nextBtnClicked());
            }
        };
        nextThread.start();
    }

    private void prevThreadBtn() {

        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                prevButton.setOnClickListener(v -> prevBtnClicked());
            }
        };
        prevThread.start();
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    public void prevBtnClicked() {
        if (musicService != null) {
            if (musicService.isPlaying()) {
                musicService.stop();
                musicService.release();
                if (shuffleBoolean && !repeatBoolean) {
                    position = getRandom(songsList.size() - 1);
                }
                //else both true means repeat  is there
                else if (!shuffleBoolean && !repeatBoolean) {
                    position = ((position - 1) < 0 ? (songsList.size() - 1) : position - 1);
                }
                uri = Uri.parse(songsList.get(position).getPath());
                musicService.createMediaPlayer(position);
                metaData(uri, songsList.get(position).getTitle(), songsList.get(position).getArtist());
                seekBar.setMax(musicService.getDuration() / 1000);
                durationTotal.setText(formattedTime(musicService.getDuration() / 1000));
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int currentPosition = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(currentPosition);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
                musicService.onCompleted();
                musicService.showNotification();
                musicService.start();
            } else {
                musicService.stop();
                musicService.release();
                if (shuffleBoolean && !repeatBoolean) {
                    position = getRandom(songsList.size() - 1);
                }
                //else both true means repeat  is there
                else if (!shuffleBoolean && !repeatBoolean) {
                    position = ((position - 1) < 0 ? (songsList.size() - 1) : position - 1);
                }
                uri = Uri.parse(songsList.get(position).getPath());
                musicService.createMediaPlayer(position);
                metaData(uri, songsList.get(position).getTitle(), songsList.get(position).getArtist());
                seekBar.setMax(musicService.getDuration() / 1000);
                durationTotal.setText(formattedTime(musicService.getDuration() / 1000));
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int currentPosition = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(currentPosition);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
                musicService.onCompleted();
            }
            if (isBottomFragShown) {
                if (musicService.isPlaying()) {
                    new NowPlayingFragmentBottom().changeMetaDataFragBottom(R.drawable.ic_pause);
                } else {
                    new NowPlayingFragmentBottom().changeMetaDataFragBottom(R.drawable.ic_play);
                }
            }

        }
    }

    public void nextBtnClicked() {
        if (musicService != null) {
            if (musicService.isPlaying()) {
                musicService.stop();
                musicService.release();
                if (shuffleBoolean && !repeatBoolean) {
                    position = getRandom(songsList.size() - 1);
                }
                //else both true means repeat  is there
                else if (!shuffleBoolean && !repeatBoolean) {
                    position = (position + 1) % songsList.size();
                }
                uri = Uri.parse(songsList.get(position).getPath());
                musicService.createMediaPlayer(position);
                metaData(uri, songsList.get(position).getTitle(), songsList.get(position).getArtist());
                seekBar.setMax(musicService.getDuration() / 1000);
                durationTotal.setText(formattedTime(musicService.getDuration() / 1000));
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int currentPosition = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(currentPosition);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
                musicService.onCompleted();
                musicService.showNotification();
                musicService.start();
            } else {
                musicService.stop();
                musicService.release();
                if (shuffleBoolean && !repeatBoolean) {
                    position = getRandom(songsList.size() - 1);
                }
                //else both true means repeat  is there
                else if (!shuffleBoolean && !repeatBoolean) {
                    position = (position + 1) % songsList.size();
                }
                uri = Uri.parse(songsList.get(position).getPath());
                musicService.createMediaPlayer(position);
                metaData(uri, songsList.get(position).getTitle(), songsList.get(position).getArtist());
                seekBar.setMax(musicService.getDuration() / 1000);
                durationTotal.setText(formattedTime(musicService.getDuration() / 1000));
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int currentPosition = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(currentPosition);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
                musicService.onCompleted();
            }
            if (isBottomFragShown) {
                if (musicService.isPlaying()) {
                    new NowPlayingFragmentBottom().changeMetaDataFragBottom(R.drawable.ic_pause);
                } else {
                    new NowPlayingFragmentBottom().changeMetaDataFragBottom(R.drawable.ic_play);
                }
            }

        }
    }

    public void playPauseBtnClicked() {
        if (musicService != null) {
            if (musicService.isPlaying()) {
                playPauseButton.setImageResource(R.drawable.ic_play);
                musicService.pause();
                seekBar.setMax(musicService.getDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int currentPosition = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(currentPosition);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
                musicService.removeNotification();
            } else {
                playPauseButton.setImageResource(R.drawable.ic_pause);
                musicService.showNotification();
                musicService.start();
                seekBar.setMax(musicService.getDuration() / 1000);
                PlayerActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int currentPosition = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(currentPosition);
                        }
                        handler.postDelayed(this, 1000);
                    }
                });
            }
            if (isBottomFragShown) {
                if (musicService.isPlaying()) {
                    new NowPlayingFragmentBottom().changeMetaDataFragBottom(R.drawable.ic_pause);
                } else {
                    new NowPlayingFragmentBottom().changeMetaDataFragBottom(R.drawable.ic_play);
                }
            }
        }
    }

    private void init() {
        headerSongName = findViewById(R.id.headerSongName);
        songName = findViewById(R.id.songName);
        durationPlayed = findViewById(R.id.durationPlayed);
        durationTotal = findViewById(R.id.durationTotal);
        coverArt = findViewById(R.id.coverArt);
        nextButton = findViewById(R.id.nextButton);
        backButton = findViewById(R.id.backButton);
        repeatButton = findViewById(R.id.repeatButton);
        shuffleButton = findViewById(R.id.shuffleButton);
        prevButton = findViewById(R.id.prevButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        seekBar = findViewById(R.id.seekBar);
        songName = findViewById(R.id.songName);
        songArtist = findViewById(R.id.songArtist);
    }

    private String formattedTime(int currentPosition) {
        String totalOut;
        String totalNew;
        String seconds = String.valueOf(currentPosition % 60);
        String minutes = String.valueOf(currentPosition / 60);
        //String hours =String.valueOf(currentPosition/(60*60));
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1)
            return totalNew;
        else
            return totalOut;

    }

    private void getIntentMethod() {
        Intent intent = new Intent(this, MusicService.class);
        position = getIntent().getIntExtra("position", -1);
        String fromFolderDetail = getIntent().getStringExtra("FromFolderDetailKey");
        String fromAlbumDetail = getIntent().getStringExtra("FromAlbumDetailKey");
        String searchedAllSongsFragment = getIntent().getStringExtra("searchedAllSongsFragmentSong");
        String fromArtistDetail = getIntent().getStringExtra("FromArtistDetailKey");
        boolean isFromBottomFrag = getIntent().getBooleanExtra("FromBottomFrag", false);
        boolean isFromNotification = getIntent().getBooleanExtra("FromNotification", false);
        boolean isFromRecentList = getIntent().getBooleanExtra("fromRecentList", false);
        Intent actionIntent = getIntent();
        if (actionIntent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            MusicFiles musicFiles1;
            uri = actionIntent.getData();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(this, uri);
                String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                musicFiles1 = new MusicFiles(title, album, duration, artist, uri.getPath());
                retriever.release();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    retriever.close();
                }
                songsList.add(musicFiles1);
                position = 0;
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Something went Wrong", Toast.LENGTH_SHORT).show();
            }
        } else if (searchedAllSongsFragment != null) {
            for (int i = 0; i < musicFiles.size(); i++) {
                String tempSong = musicFiles.get(i).getTitle();
                if (searchedAllSongsFragment.equals(tempSong)) {
                    position = i;
                    break;
                }
            }
        } else if (isFromBottomFrag || isFromNotification) {
            songsList = MusicService.musicFiles;
            position = MusicService.position;
            seekBar.setMax(musicService.getDuration() / 1000);
            durationTotal.setText(formattedTime(musicService.getDuration() / 1000));
            metaData(uri, songsList.get(position).getTitle(), songsList.get(position).getArtist());
            if (musicService.isPlaying()) {
                playPauseButton.setImageResource(R.drawable.ic_pause);
                musicService.showNotification();
            } else {
                playPauseButton.setImageResource(R.drawable.ic_play);
            }
            if (isFromNotification) {
                if (isPlayerActivityShown) {
                    finish();
                }
            }
            return;
        } else if (fromFolderDetail != null && fromFolderDetail.equals("FromFolderDetailValue")) {
            songsList = folderMusicFiles;
        } else if (fromArtistDetail != null && fromArtistDetail.equals("FromArtistDetailValue")) {
            songsList = artistMusicFiles;

        } else if (fromAlbumDetail != null && fromAlbumDetail.equals("FromAlbumDetailValue")) {
            songsList = albumMusicFiles;

        } else if (isFromRecentList) {
            songsList = musicFilesArrayList;

        } else {
            songsList = musicFiles;
        }
        if (songsList != null) {
            playPauseButton.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(songsList.get(position).getPath());
        }

        if (shuffleBoolean) {
            shuffleButton.setImageResource(R.drawable.ic_shuffle_on);
        } else {
            shuffleButton.setImageResource(R.drawable.ic_shuffle_off);
        }
        if (repeatBoolean) {
            repeatButton.setImageResource(R.drawable.ic_repeat_on);
        } else {
            repeatButton.setImageResource(R.drawable.ic_repeat_off);
        }
        intent.putExtra("servicePosition", position);
        startService(intent);
    }

    private void metaData(Uri uri, String title, String artist) {
        byte[] art;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri.toString());
            art = retriever.getEmbeddedPicture();
            retriever.release();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                retriever.close();
            }
        } catch (Exception e) {
            art = null;
        }
        songName.setText(title);
        songArtist.setText(artist);
        headerSongName.setText(title);
        headerSongName.setSelected(true);
        if (art != null) {
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(art)
                    .into(coverArt);

        } else {
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(R.drawable.ic_music_player)
                    .into(coverArt);
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        boolean isFromBottomFrag = getIntent().getBooleanExtra("FromBottomFrag", false);
        boolean isFromNotification = getIntent().getBooleanExtra("FromNotification", false);
        if (isFromBottomFrag || isFromNotification) {
            return;
        }
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        if (musicService != null) {
            musicService.setCallBack(this);
            seekBar.setMax(musicService.getDuration() / 1000);
            durationTotal.setText(formattedTime(musicService.getDuration() / 1000));
            metaData(uri, songsList.get(position).getTitle(), songsList.get(position).getArtist());
            musicService.onCompleted();
            musicService.showNotification();
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }
}