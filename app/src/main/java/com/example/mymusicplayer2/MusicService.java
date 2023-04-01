package com.example.mymusicplayer2;

import static com.example.mymusicplayer2.ApplicationClass.ACTION_NEXT;
import static com.example.mymusicplayer2.ApplicationClass.ACTION_PLAY;
import static com.example.mymusicplayer2.ApplicationClass.ACTION_PREVIOUS;
import static com.example.mymusicplayer2.ApplicationClass.CHANNEL_ID_2;
import static com.example.mymusicplayer2.MainActivity.musicFiles;
import static com.example.mymusicplayer2.PlayerActivity.songsList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    //called when service will started
    /*  IBinder is an interface that is used to facilitate communication between processes in Android.
        It is a fundamental building block for implementing Android's interprocess communication (IPC) mechanism.

        Binder is an essential part of the Android system that provides the underlying infrastructure for interprocess communication(IPC),
        which is a critical aspect of building complex Android applications.

         MyBinder is a custom class that extends the Binder class and provides a way for clients to interact with a local Service running in the same process.
     */
    IBinder binder=new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles=new ArrayList<>();
    Uri uri;
    int position=-1;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED="LAST_PLAYED";
    public static final String MUSIC_FILE="STORED_MUSIC";
    public static final String ARTIST_NAME="ARTIST NAME";
    public static final String SONG_NAME="SONG NAME";
    ActionPlaying actionPlaying;
    /* onStartCommand(Intent intent, int flags, int startId) is a method that is used in Android to start or re-start a Service.
       It is called by the system when a client sends a request to start the Service, using the startService(Intent) method.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition=intent.getIntExtra("servicePosition",-1);
        String actionName=intent.getStringExtra("ActionName");
        if(myPosition!=-1)
        {
            playMedia(myPosition);
        }
        if(actionName!=null)
        {
            switch (actionName)
            {
                case "playPause":
                   playPauseBtn();
                    break;

                case "next":
                    nextBtnClicked();
                    break;

                case "previous":
                   previousBtnClicked();
                    break;
            }
        }
       //for making it not stop service
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles=songsList;
        position=startPosition;
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if(musicFiles!=null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }
        else
        {
             createMediaPlayer(position);
             mediaPlayer.start();
        }
    }

    @Override
    public void onCreate() {
        /* MediaSessionCompat is a class from the Android Support Library that provides a way to interact with media playback from outside of the media playback service or activity,
           such as through notifications, lock screens, or hardware media buttons. */

        mediaSessionCompat=new MediaSessionCompat(getBaseContext(),"My Audio");
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder{
        MusicService getService(){
            return MusicService.this;
        }
    }
    void start()
    {
        mediaPlayer.start();
    }
    boolean isPlaying()
    {
        return mediaPlayer.isPlaying();
    }
    void stop()
    {
        mediaPlayer.stop();
    }
    void release()
    {
        mediaPlayer.release();
    }
    int getDuration()
    {
        return  mediaPlayer.getDuration();
    }
    void seekTo(int position)
    {  // Seeks to specified time position.
        mediaPlayer.seekTo(position);
    }
    int getCurrentPosition()
    {
        // Gets the current playback position.
        return mediaPlayer.getCurrentPosition();
    }
    void createMediaPlayer(int positionInner)
    {   position = positionInner;
        uri =Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor=getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE,uri.toString());
        editor.putString(ARTIST_NAME,musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME,musicFiles.get(position).getTitle());
        editor.apply();
        /* getBaseContext(): This is a method call that retrieves the base context of the application.
           The base context is the context from which all other contexts in the application are derived.
         */
        mediaPlayer=MediaPlayer.create(getBaseContext(),uri);
    }
    void pause()
    {
        mediaPlayer.pause();
    }
    void onCompleted()
    {
        mediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        if(actionPlaying!=null)
        {
            actionPlaying.nextBtnClicked();
        }
        createMediaPlayer(position);
        mediaPlayer.start();
        onCompleted();
    }
 void setCallBack(ActionPlaying actionPlaying)
 {
     this.actionPlaying=actionPlaying;
 }
    void showNotification(int playPauseBtn)

    {   // When the PendingIntent is triggered, the system broadcasts the intent that was passed to getBroadcast()

        Intent intent=new Intent(this,NotificationReceiver.class);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE);

        Intent prevIntent=new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending=PendingIntent
                .getBroadcast(this,0,prevIntent,PendingIntent.FLAG_IMMUTABLE);

        Intent pauseIntent=new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending=PendingIntent
                .getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent=new Intent(this,NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending=PendingIntent
                .getBroadcast(this,0,nextIntent,PendingIntent.FLAG_IMMUTABLE);
        byte[] picture;
        picture=getAlbumArt(musicFiles.get(position).getPath());
        Bitmap thumb;
        if(picture!=null)
        {
            thumb= BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        else
        {
            thumb=BitmapFactory.decodeResource(getResources(),R.drawable.ic_baseline_music_note_24);
        }
        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_skip_previou,"Previous",prevPending)
                .addAction(R.drawable.ic_skip_next,"next",nextPending)
                .addAction(playPauseBtn,"Pause",pausePending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .build();
        startForeground(1,notification);
    }
    private byte[] getAlbumArt(String uri)
    {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        return retriever.getEmbeddedPicture();
    }
    void nextBtnClicked(){
        if(actionPlaying!=null)
        {
            actionPlaying.nextBtnClicked();
        }
    }
    void previousBtnClicked(){
        if(actionPlaying!=null)
        {
            actionPlaying.prevBtnClicked();
        }
    }
    void playPauseBtn(){
        if(actionPlaying!=null)
        {
            actionPlaying.playPauseBtnClicked();
        }
    }
}
