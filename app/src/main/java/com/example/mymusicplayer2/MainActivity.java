package com.example.mymusicplayer2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.provider.MediaStore;
import android.view.View;
import android.widget.ProgressBar;


import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;


import java.util.ArrayList;
import java.util.List;



/** @noinspection deprecation*/
public class MainActivity extends AppCompatActivity {
    public static  boolean SHOW_MINI_PLAYER = false;
    public static ArrayList<MusicFiles> musicFiles;
    ProgressBar progressBar ;
     TabLayout tabLayout;
     ViewPager viewPager;
     public static boolean shuffleBoolean=false;
     public static boolean  repeatBoolean=false;
    public static final String MUSIC_LAST_PLAYED="LAST_PLAYED";
    public static final String MUSIC_FILE="STORED_MUSIC";
    public static final String ARTIST_NAME="ARTIST NAME";
    public static final String SONG_NAME="SONG NAME";
    public static String PATH_TO_FRAG=null;
    public static String ARTIST_TO_FRAG=null;
    public static String SONG_NAME_TO_FRAG=null;
    public static boolean isBottomFragShown =false;
    public static boolean isPlayerActivityShown = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager=findViewById(R.id.viewPager);
        tabLayout=findViewById(R.id.tabLayout);
        progressBar=findViewById(R.id.progress_bar);
        musicFiles=new ArrayList<>();
        runTimePermission();
     }
    public void runTimePermission() {
        Dexter.withContext(getApplicationContext())
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        new InitTask().execute();
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public  ArrayList<MusicFiles> getAllAudio(Context context) {
        ArrayList<MusicFiles> tempAudioList =new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection={
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };
        /*
        * getContentResolver() is used to get ContentResolver
        * ContentResolver is use to fetch,interact or modify data provided by ContentProvider and we use query method to do these operation on it.
        * ContentProvider is use to provide data from external application (similar table in sql).
        * query parameters
        * uri :- uri of content provider here it is MediaStore.Audio.Media and most of are found in android.provider package.
        * projection:- particular set of columns you want query.
        * order :- it takes order of sorting.
        * */
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);
        if(cursor!=null)
        {
            while (cursor.moveToNext())
            {
                String title=cursor.getString(0);
                String album=cursor.getString(1);
                String duration=cursor.getString(2);
                String artist=cursor.getString(3);
                String path=cursor.getString(4);
                MusicFiles musicFiles=new MusicFiles(title,album,duration,artist,path);
                tempAudioList.add(musicFiles);
            }
            cursor.close();
        }
        return tempAudioList;
    }

    /** @noinspection deprecation*/
    public static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> fragments;
        private final ArrayList<String> titles;
        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments=new ArrayList<>();
            this.titles=new ArrayList<>();
        }

        void  addFragments(Fragment fragment,String title)
        {
            fragments.add(fragment);
            titles.add(title);
        }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }


    private void initViewPager() {
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new AllSongsFragment(),"All Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(),"Albums");
        viewPagerAdapter.addFragments(new FolderFragment(),"Folder");
        viewPagerAdapter.addFragments(new ArtistFragment(),"Artist");
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(viewPagerAdapter.getCount());
        tabLayout.setupWithViewPager(viewPager);
     }

    @Override
    protected void onResume() {
        super.onResume();
        isPlayerActivityShown = false;
        SharedPreferences preferences=getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
        String path=preferences.getString(MUSIC_FILE,null);
        String artist=preferences.getString(ARTIST_NAME,null);
        String songName=preferences.getString(SONG_NAME,null);
        if(path!=null) {
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
    }
    @SuppressLint("StaticFieldLeak")
    class InitTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progressBar.setVisibility(View.INVISIBLE);
            initViewPager();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            musicFiles = getAllAudio(getApplicationContext());
            return null;
        }
    }
}