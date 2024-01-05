package com.example.mymusicplayer2;

import static com.example.mymusicplayer2.ApplicationClass.ACTION_NEXT;
import static com.example.mymusicplayer2.ApplicationClass.ACTION_PLAY;
import static com.example.mymusicplayer2.ApplicationClass.ACTION_PREVIOUS;
import static com.example.mymusicplayer2.ApplicationClass.CHANNEL_ID_2;


import androidx.palette.graphics.Palette;

import static com.example.mymusicplayer2.MainActivity.isBottomFragShown;
import static com.example.mymusicplayer2.PlayerActivity.songsList;

import android.app.Notification;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;

import android.widget.RemoteViews;


import androidx.annotation.Nullable;

import androidx.core.app.NotificationCompat;


import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    //called when service will started
    /*  IBinder is an interface that is used to facilitate communication between processes in Android.
        It is a fundamental building block for implementing Android's interprocess communication (IPC) mechanism.

        Binder is an essential part of the Android system that provides the underlying infrastructure for interprocess communication(IPC),
        which is a critical aspect of building complex Android applications.

         MyBinder is a custom class that extends the Binder class and provides a way for clients to interact with a local Service running in the same process.
     */
    public static Notification notification;
    public static int dominantColor = 0xFFFFFFFF;
    IBinder binder = new MyBinder();
    MediaPlayer mediaPlayer;
    public static ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    public static int position = -1;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
    ActionPlaying actionPlaying;

    /* onStartCommand(Intent intent, int flags, int startId) is a method that is used in Android to start or re-start a Service.
       It is called by the system when a client sends a request to start the Service, using the startService(Intent) method.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int myPosition = intent.getIntExtra("servicePosition", -1);
            String actionName = intent.getStringExtra("ActionName");
            if (myPosition != -1) {
                playMedia(myPosition);
            }
            if (actionName != null) {
                switch (actionName) {
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
        }
        return START_STICKY;
    }
    private void playMedia(int startPosition) {
        musicFiles = songsList;
        position = startPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null) {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        } else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }

    @Override
    public void onCreate() {
        /* MediaSessionCompat is a class from the Android Support Library that provides a way to interact with media playback from outside of the media playback service or activity,
           such as through notifications, lock screens, or hardware media buttons. */
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    void start() {
        mediaPlayer.start();
    }

    boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    void stop() {
        mediaPlayer.stop();
    }

    void release() {
        mediaPlayer.release();
    }

    int getDuration() {
        return mediaPlayer.getDuration();
    }

    void seekTo(int position) {  // Seeks to specified time position.
        mediaPlayer.seekTo(position);
    }

    int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    void createMediaPlayer(int positionInner) {
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.apply();
        /* getBaseContext(): This is a method call that retrieves the base context of the application.
           The base context is the context from which all other contexts in the application are derived.
         */
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    void pause() {
        mediaPlayer.pause();
    }

    void onCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
        }
        createMediaPlayer(position);
        mediaPlayer.start();
        onCompleted();
        if(isBottomFragShown){
            if(mediaPlayer.isPlaying()){
                new NowPlayingFragmentBottom().changeMetaDataFragBottom(R.drawable.ic_pause);
            }
            else{
                new NowPlayingFragmentBottom().changeMetaDataFragBottom(R.drawable.ic_play);
            }
        }
    }

    void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    void showNotification() {
        // When the PendingIntent is triggered, the system broadcasts the intent that was passed to getBroadcast()
        int playPauseIc =R.drawable.ic_pause;
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("FromNotification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent
                .getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE);
        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent
                .getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent
                .getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE);
        byte[] picture;
        try {
            picture = getAlbumArt(musicFiles.get(position).getPath());
        }
        catch (Exception e){
            picture=null;
        }
        Bitmap thumb;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_player);
        }
        RemoteViews customNotificationView = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);
        customNotificationView.setImageViewResource(R.id.notification_btnPlayPause, playPauseIc);
        customNotificationView.setImageViewBitmap(R.id.notification_image, thumb);
        customNotificationView.setTextViewText(R.id.notification_song_name, musicFiles.get(position).getTitle());
        customNotificationView.setTextViewText(R.id.notification_artist_name, musicFiles.get(position).getArtist());
        customNotificationView.setImageViewBitmap(R.id.notification_gradient, getDominantColor(thumb));

        customNotificationView.setOnClickPendingIntent(R.id.notification_rl_layout,pendingIntent);
        customNotificationView.setOnClickPendingIntent(R.id.notification_btnPrevious, prevPending);
        customNotificationView.setOnClickPendingIntent(R.id.notification_btnPlayPause, pausePending);
        customNotificationView.setOnClickPendingIntent(R.id.notification_btnNext, nextPending);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseIc)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .setCustomContentView(customNotificationView)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSilent(true)
                .build();
        startForeground(1,notification);
    }
    void removeNotification() {
        stopForeground(true);
    }
    public Bitmap getDominantColor(Bitmap bm){
        Palette.from(bm).generate(palette -> {
            // Access the dominant color
            if (palette != null && palette.getDominantSwatch() != null) {
                dominantColor = palette.getDominantSwatch().getRgb();
            }
        });
        Bitmap bitmap = Bitmap.createBitmap(200, 60, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(dominantColor);
        return bitmap;
    }
    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art;
        art = retriever.getEmbeddedPicture();
        retriever.release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            retriever.close();
        }
        return art;
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
        if(actionPlaying!=null){
            actionPlaying.playPauseBtnClicked();
        }
    }
}
