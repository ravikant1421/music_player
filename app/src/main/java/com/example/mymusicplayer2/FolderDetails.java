package com.example.mymusicplayer2;

import static com.example.mymusicplayer2.MainActivity.musicFiles;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import java.io.File;
import java.util.ArrayList;

public class FolderDetails extends AppCompatActivity {
    String folderName;
    static ArrayList<MusicFiles> folderMusicFiles;
    static ArrayList<String> folderDetailArrayList;
    ArrayAdapter<String> folderDetailAdapter;
    ListView folderDetailListView;
    TextView noItemTV;
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
            if(folder!=null){
                folder=folder.substring(folder.lastIndexOf('/')+1);
                if(folderName.equals(folder)){
                    folderMusicFiles.add(musicFiles.get(i));
                    folderDetailArrayList.add(musicFiles.get(i).getTitle());
                }
            }
        }
        if(folderDetailArrayList.size() >= 1){
            folderDetailAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item_text,R.id.listItemTextview, folderDetailArrayList);
            folderDetailListView.setAdapter(folderDetailAdapter);
            folderDetailListView.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent=new Intent(getApplicationContext(),PlayerActivity.class);
                intent.putExtra("FromFolderDetailKey","FromFolderDetailValue");
                intent.putExtra("position",position);
                startActivity(intent);
            });
        }
        else{
            folderDetailListView.setVisibility(View.INVISIBLE);
            noItemTV.setVisibility(View.VISIBLE);
        }

    }
}