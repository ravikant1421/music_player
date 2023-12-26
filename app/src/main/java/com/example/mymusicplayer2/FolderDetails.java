package com.example.mymusicplayer2;

import static com.example.mymusicplayer2.MainActivity.musicFiles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.io.File;
import java.util.ArrayList;

public class FolderDetails extends AppCompatActivity {
    String folderName;
    static ArrayList<MusicFiles> folderMusicFiles;
    static ArrayList<String> folderDetailArrayList;
    ArrayAdapter<String> folderDetailAdapter;
    ListView folderDetailListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_details);
        folderName=getIntent().getStringExtra("FolderNameKey");
        folderDetailListView=findViewById(R.id.folderDetailListView);
        folderMusicFiles=new ArrayList<>();
        folderDetailArrayList=new ArrayList<>();
        for(int i=0;i<musicFiles.size();i++){
            File file=new File(musicFiles.get(i).getPath());
            String folder=file.getParent();
            folder=folder.substring(folder.lastIndexOf('/')+1);
            if(folderName.equals(folder)){
                folderMusicFiles.add(musicFiles.get(i));
                folderDetailArrayList.add(musicFiles.get(i).getTitle());
            }
        }
        folderDetailAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_itrm_text,R.id.listItemTextview, folderDetailArrayList);
        folderDetailListView.setAdapter(folderDetailAdapter);
        folderDetailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),PlayerActivity.class);
                intent.putExtra("FromFolderDetailKey","FromFolderDetailValue");
                intent.putExtra("position",position);
                startActivity(intent);
            }
        });

    }
}