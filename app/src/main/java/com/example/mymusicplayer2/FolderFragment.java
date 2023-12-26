package com.example.mymusicplayer2;


import static com.example.mymusicplayer2.MainActivity.musicFiles;




import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;


public class FolderFragment extends Fragment {
    View view;
    ArrayList<String> folders;
    RecyclerView recyclerView;
    FolderAdapter folderAdapter;

    public FolderFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        folders=new ArrayList<>();
        super.onCreate(savedInstanceState);
        for(MusicFiles singleFile:musicFiles){
            File file=new File(singleFile.getPath());
            String folder=file.getParent();
            folder=folder.substring(folder.lastIndexOf('/')+1);
            if(!folders.contains(folder)){
                folders.add(folder);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_folder, container, false);
        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        if(folders.size()>=1)
        {
            folderAdapter =new FolderAdapter(getContext(),folders);
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(folderAdapter);
        }
        return view;
    }
}