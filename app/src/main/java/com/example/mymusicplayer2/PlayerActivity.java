package com.example.mymusicplayer2;

import static com.example.mymusicplayer2.AlbumDetails.albumMusicFiles;
import static com.example.mymusicplayer2.ArtistDetails.artistMusicFiles;
import static com.example.mymusicplayer2.FolderDetails.folderMusicFiles;
import static com.example.mymusicplayer2.MainActivity.musicFiles;
import static com.example.mymusicplayer2.MainActivity.repeatBoolean;
import static com.example.mymusicplayer2.MainActivity.shuffleBoolean;

import static com.example.mymusicplayer2.MusicService.notification;
import static com.example.mymusicplayer2.NowPlayingFragmentBottom.bottomNextBtnFlag;
import static com.example.mymusicplayer2.NowPlayingFragmentBottom.bottomPrevBtnFlag;
import static com.example.mymusicplayer2.NowPlayingFragmentBottom.clickedSong;
import static com.example.mymusicplayer2.NowPlayingFragmentBottom.playBottomFlag;
import static com.example.mymusicplayer2.NowPlayingFragmentBottom.showLayoutFlag;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import android.media.MediaMetadataRetriever;


import android.net.Uri;
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


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;


public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {
    TextView songName,durationPlayed,durationTotal,songArtist,headerSongName;
    ImageView  coverArt,nextButton,backButton,repeatButton,shuffleButton,prevButton;
    FloatingActionButton playPauseButton;
    SeekBar seekBar;
    public static int position=-1;
    static Uri uri;
    private final Handler handler=new Handler();
    public Thread playThread,prevThread,nextThread;
    public static ArrayList<MusicFiles> songsList=new ArrayList<>();
    static MusicService musicService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setFullScreen();
            setContentView(R.layout.activity_player);
            getSupportActionBar().hide();
            int seekbarProgressBottomPlayer=getIntent().getIntExtra("seekbarProgress",-1);
            init();
            getIntentMethod();
            if(seekbarProgressBottomPlayer!=-1){
            seekBar.setProgress(seekbarProgressBottomPlayer);
            }
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (musicService != null && fromUser) {
                        musicService.seekTo(progress * 1000);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

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
            shuffleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (shuffleBoolean) {
                        shuffleBoolean = false;
                        shuffleButton.setImageResource(R.drawable.ic_shuffle_off);
                    } else {
                        shuffleBoolean = true;
                        shuffleButton.setImageResource(R.drawable.ic_shuffle_on);
                    }
                }
            });
            repeatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (repeatBoolean) {
                        repeatBoolean = false;
                        repeatButton.setImageResource(R.drawable.ic_repeat_off);
                    } else {
                        repeatBoolean = true;
                        repeatButton.setImageResource(R.drawable.ic_repeat_on);
                    }
                }
            });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    finish();
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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        if(!showLayoutFlag){
            showLayoutFlag=true;
        }
        Intent intent=new Intent(this,MusicService.class);
        /* bindService(intent, this, BIND_AUTO_CREATE) is a method call that binds the specified service to the current component
           (which is likely an activity, fragment, or service)
           BIND_AUTO_CREATE: This parameter specifies the binding options for the service. In this case,
           BIND_AUTO_CREATE indicates that the service should be created if it does not already exist
            bindService(intent,this,BIND_AUTO_CREATE); here this is the instance of ServiceConnection interface .
         */
            bindService(intent,this,BIND_AUTO_CREATE);
            playThreadBtn();
            nextThreadBtn();
            prevThreadBtn();

        super.onResume();
    }
    private void playThreadBtn() {
        playThread=new Thread(){
            @Override
            public void run() {
                super.run();
                playPauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }
    private void nextThreadBtn() {
            nextThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    nextButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            nextBtnClicked();
                        }
                    });
                }
            };
            nextThread.start();
           }
    private void prevThreadBtn() {

                prevThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        prevButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                prevBtnClicked();
                            }
                        });
                    }
                };
                prevThread.start();
            }
    @Override
    protected void onPause() {
        super.onPause();
        bottomNextBtnFlag=false;
        bottomPrevBtnFlag=false;
        if(musicService.isPlaying()){
            playBottomFlag=true;
        }
        else {
            playBottomFlag=false;
        }
        unbindService(this);
    }
    private int getRandom(int i) {
        Random random=new Random();
        return random.nextInt(i+1);
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
                musicService.showNotification(R.drawable.ic_pause);
//                playPauseButton.setImageResource(R.drawable.ic_pause);
                durationTotal.setText(formattedTime(musicService.getDuration() / 1000));
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
                durationTotal.setText(formattedTime(musicService.getDuration() / 1000));
                metaData(uri, songsList.get(position).getTitle(), songsList.get(position).getArtist());
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
                musicService.onCompleted();
                musicService.showNotification(R.drawable.ic_play);
//                playPauseButton.setImageResource(R.drawable.ic_play);
            }
        }
    }
    public void nextBtnClicked() {
        if(musicService!=null) {
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
                musicService.showNotification(R.drawable.ic_pause);
//                playPauseButton.setImageResource(R.drawable.ic_pause);
                durationTotal.setText(formattedTime(musicService.getDuration() / 1000));
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
                durationTotal.setText(formattedTime(musicService.getDuration() / 1000));
                metaData(uri, songsList.get(position).getTitle(), songsList.get(position).getArtist());
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
                musicService.onCompleted();
                musicService.showNotification(R.drawable.ic_play);

//                playPauseButton.setImageResource(R.drawable.ic_play);
            }
        }
    }

    public void playPauseBtnClicked() {
        if(musicService!=null) {
            if (musicService.isPlaying()) {
                playPauseButton.setImageResource(R.drawable.ic_play);
                musicService.showNotification(R.drawable.ic_play);
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
            } else {
                playPauseButton.setImageResource(R.drawable.ic_pause);
                musicService.showNotification(R.drawable.ic_pause);
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
            new NowPlayingFragmentBottom().setPlayPauseMiniPlayerIc(musicService,NowPlayingFragmentBottom.view);
        }
    }

    private void init(){
        headerSongName=findViewById(R.id.headerSongName);
        songName=findViewById(R.id.songName);
        durationPlayed=findViewById(R.id.durationPlayed);
        durationTotal=findViewById(R.id.durationTotal);
        coverArt=findViewById(R.id.coverArt);
        nextButton=findViewById(R.id.nextButton);
        backButton=findViewById(R.id.backButton);
        repeatButton=findViewById(R.id.repeatButton);
        shuffleButton=findViewById(R.id.shuffleButton);
        prevButton=findViewById(R.id.prevButton);
        playPauseButton=findViewById(R.id.playPauseButton);
        seekBar=findViewById(R.id.seekBar);
        songName=findViewById(R.id.songName);
        songArtist=findViewById(R.id.songArtist);
    }
    @NonNull
    private String formattedTime(int currentPosition)
    {
        String totalOut;
        String totalNew;
        String seconds =String.valueOf(currentPosition%60);
        String minutes =String.valueOf(currentPosition/60);
                      //String hours =String.valueOf(currentPosition/(60*60));
        totalOut=minutes+":"+seconds;
        totalNew=minutes+":"+"0"+seconds;
        if(seconds.length()==1)
            return totalNew;
        else
          return totalOut;

    }
    private void getIntentMethod(){
        position=getIntent().getIntExtra("position",-1);
        String fromFolderDetail=getIntent().getStringExtra("FromFolderDetailKey");
        String fromAlbumDetail=getIntent().getStringExtra("FromAlbumDetailKey");
        String searchedAllSongsFragment=getIntent().getStringExtra("searchedAllSongsFragmentKey");
        String fromArtistDetail=getIntent().getStringExtra("FromArtistDetailKey");
        String fromLayoutBottomSongName=getIntent().getStringExtra("FromLayoutBottomKey");
        String fromNotification=getIntent().getStringExtra("FromNotification");
        if(searchedAllSongsFragment!=null) {
            for (int i = 0; i < musicFiles.size(); i++) {
                String tempSong = musicFiles.get(i).getTitle();
                if (searchedAllSongsFragment.equals(tempSong)) {
                    position = i;
                    break;
                }
            }
        }
        if(fromLayoutBottomSongName!=null && fromLayoutBottomSongName.equals(clickedSong)){
            songsList=musicFiles;
            for(int i=0;i<musicFiles.size();i++){
                if(musicFiles.get(i).getTitle().equals(clickedSong)){
                    position=i;
                    break;
                }
            }
        }
        else if(fromNotification!=null && fromNotification.equals("FromNotificationValue")){
            String title=notification.extras.getString(NotificationCompat.EXTRA_TITLE);
            songsList=musicFiles;
            for(int i=0;i<musicFiles.size();i++){
                if(musicFiles.get(i).getTitle().equals(title)){
                    position=i;
                    break;
                }
            }
        }
        else if(fromFolderDetail!=null && fromFolderDetail.equals("FromFolderDetailValue")){
            songsList=folderMusicFiles;
        }
        else if(fromArtistDetail!=null && fromArtistDetail.equals("FromArtistDetailValue"))
        {
            songsList=artistMusicFiles;

        }
        else if(fromAlbumDetail!=null && fromAlbumDetail.equals("FromAlbumDetailValue"))
        {
           songsList=albumMusicFiles;

        }
        else{
        songsList=musicFiles;
        }
        if(songsList!=null)
        {
            playPauseButton.setImageResource(R.drawable.ic_pause);
            uri=Uri.parse(songsList.get(position).getPath());
        }

        if(shuffleBoolean){
            shuffleButton.setImageResource(R.drawable.ic_shuffle_on);
        }
        else
        {
            shuffleButton.setImageResource(R.drawable.ic_shuffle_off);
        }
        if(repeatBoolean){
            repeatButton.setImageResource(R.drawable.ic_repeat_on);
        }
        else
        {
            repeatButton.setImageResource(R.drawable.ic_repeat_off);
        }
        Intent intent=new Intent(this,MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);
    }
    private  void metaData(Uri uri,String title,String artist)
    {   if(bottomNextBtnFlag || bottomPrevBtnFlag){
        return;
        }
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        byte[] art=retriever.getEmbeddedPicture();
        songName.setText(title);
        songArtist.setText(artist);
        headerSongName.setText(title);
        headerSongName.setSelected(true);
        if(art!=null)
        {
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(art)
                    .into(coverArt);

        }
        else
        {
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(R.drawable.ic_music_player)
                    .into(coverArt);
        }

    }
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder=(MusicService.MyBinder) service;
        musicService=myBinder.getService();
        musicService.setCallBack(this);
        seekBar.setMax(musicService.getDuration()/1000);
        durationTotal.setText(formattedTime(musicService.getDuration()/1000));
        metaData(uri,songsList.get(position).getTitle(),songsList.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService=null;
    }

}