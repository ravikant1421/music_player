package com.example.mymusicplayer2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class RecentDbHelper extends SQLiteOpenHelper {
    public static ArrayList<MusicFiles> musicFilesArrayList;
    public static final int VERSION=1;
    public static final String DATABASE_NAME = "RecentSongsDB";
    public static final String TABLE_NAME = "recentsongs";
    public RecentDbHelper(Context context) {
        super(context,DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //CREATE TABLE recent_songs ( title TEXT, album TEXT, duration TEXT, artist TEXT , path TEXT, timeline TIMESTAMP );
        db.execSQL("CREATE TABLE "+ TABLE_NAME +" ( title TEXT, album TEXT, duration TEXT, artist TEXT , path TEXT, timeline TIMESTAMP );");
    }

    public ArrayList<MusicFiles> fetchRecentSongs(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("Select * from "+ TABLE_NAME+" ORDER BY timeline DESC",null);
        musicFilesArrayList=new ArrayList<>();
        while(cursor.moveToNext()){
            String title = cursor.getString(0);
            String album = cursor.getString(1);
            String duration = cursor.getString(2);
            String artist = cursor.getString(3);
            String path = cursor.getString(4);
            musicFilesArrayList.add(new MusicFiles(title,album,duration,artist,path));
        }
        cursor.close();
        db.close();
        return musicFilesArrayList;
    }
    public void addRecentSong(MusicFiles musicFiles){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor1 = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE title = ?", new String[]{musicFiles.getTitle()});
        boolean songExists = cursor1.moveToFirst();
        if(songExists){
            db.delete(TABLE_NAME, "title = ?",new String[]{musicFiles.getTitle()});
        }
        cursor1.close();
        ContentValues cv = new ContentValues();
        cv.put("title",musicFiles.getTitle());
        cv.put("album",musicFiles.getAlbum());
        cv.put("duration",musicFiles.getDuration());
        cv.put("artist",musicFiles.getArtist());
        cv.put("path",musicFiles.getPath());
        cv.put("timeline",System.currentTimeMillis());
        db.insert(TABLE_NAME,null,cv);
        Cursor cursor2 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        int size = 0;
        if (cursor2.moveToFirst()) {
            size = cursor2.getInt(0);
        }
        cursor2.close();
        if(size > 10){
            deleteLestRecentSong();
        }
        db.close();
    }
    public void deleteLestRecentSong(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT MIN(timeline) FROM " + TABLE_NAME, null);
        long minTimeline = 0;
        if (cursor.moveToFirst()) {
            minTimeline = cursor.getLong(0);
        }
        db.delete(TABLE_NAME, "timeline = ?", new String[] { String.valueOf(minTimeline) });
        cursor.close();
        db.close();
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
