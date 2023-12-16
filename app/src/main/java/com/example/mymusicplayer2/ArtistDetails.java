package com.example.mymusicplayer2;

import static com.example.mymusicplayer2.MainActivity.musicFiles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ArtistDetails extends AppCompatActivity {
    String artistName;
    static ArrayList<MusicFiles> artistMusicFiles;
    static ArrayList<String> artistDetailArrayList;
    ArrayAdapter<String> artistDetailAdapter;
    ListView artistDetailListView;
    TextView artistNameTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_details);
        artistMusicFiles=new ArrayList<>();
        artistDetailListView=findViewById(R.id.artistDetailListView);
        artistNameTop=findViewById(R.id.artistNameTop);
        artistName=getIntent().getStringExtra("artistName");
        artistDetailArrayList=new ArrayList<>();
        for(int i=0;i<musicFiles.size();i++){
            if(artistName.equals(musicFiles.get(i).getArtist())){
                artistMusicFiles.add(musicFiles.get(i));
            }
        }
        for(int i=0;i<artistMusicFiles.size();i++)
        {
            artistDetailArrayList.add(artistMusicFiles.get(i).getTitle());
        }
        if(!artistMusicFiles.isEmpty()) {
            artistNameTop.setText(artistMusicFiles.get(0).getArtist());
        }
        artistDetailAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_itrm_text,R.id.listItemTextview, artistDetailArrayList);
        artistDetailListView.setAdapter(artistDetailAdapter);
        artistDetailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),PlayerActivity.class);
                intent.putExtra("FromArtistDetailKey","FromArtistDetailValue");
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });
    }
}