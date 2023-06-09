package com.example.mymusicplayer2;

import static com.example.mymusicplayer2.MainActivity.musicFiles;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {
    String albumName;
    static ArrayList<MusicFiles> albumMusicFiles;
    static ArrayList<String> albumDetailArrayList;
    ArrayAdapter<String> albumDetailAdapter;
    ListView albumDetailListView;
    ImageView albumPhoto;
    // onOptionsMenuClosed() is a method that is called when the options menu of an activity is closed.
    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        albumPhoto=findViewById(R.id.albumPhoto);
        albumMusicFiles=new ArrayList<>();
        albumDetailListView=findViewById(R.id.albumDetailListView);
        albumName=getIntent().getStringExtra("albumName");
        albumDetailArrayList=new ArrayList<>();
        boolean flagAlbumPhoto=true;
        for(int i=0;i<musicFiles.size();i++)
        {
            if(albumName.equals(musicFiles.get(i).getAlbum()))
            {  if(flagAlbumPhoto)
               {   Uri uri= Uri.parse(musicFiles.get(i).getPath());
                   MediaMetadataRetriever retriever=new MediaMetadataRetriever();
                   retriever.setDataSource(uri.toString());
                   byte[] art=retriever.getEmbeddedPicture();
                   try {
                       retriever.release();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   flagAlbumPhoto=false;
                   if(art!=null)
                   {
                       Glide.with(this)
                               .asBitmap()
                               .load(art)
                               .into(albumPhoto);
                   }
                   else
                   {
                       Glide.with(this)
                               .asBitmap()
                               .load(R.drawable.ic_baseline_music_note_24)
                               .into(albumPhoto);
                   }

               }
               albumMusicFiles.add(musicFiles.get(i));
            }
        }
        for(int i=0;i<albumMusicFiles.size();i++)
        {
            albumDetailArrayList.add(albumMusicFiles.get(i).getTitle());
        }
        albumDetailAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, albumDetailArrayList);
        albumDetailListView.setAdapter(albumDetailAdapter);
        albumDetailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),PlayerActivity.class);
                intent.putExtra("FromAlbumDetailKey","FromAlbumDetailValue");
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }
}