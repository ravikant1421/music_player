package com.example.mymusicplayer2;


import static com.example.mymusicplayer2.MainActivity.paths;



import android.os.Bundle;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_folder, container, false);
        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        folders=new ArrayList<>();
        for(MusicFiles a:paths){
            File file=new File(a.getPath());
            String folder=file.getParent();
            folder=folder.substring(folder.lastIndexOf('/')+1);
            if(!folders.contains(folder)){
                folders.add(folder);
            }
        }
//        Toast.makeText(getContext(),folders.size()+"",Toast.LENGTH_SHORT).show();
        if(folders.size()>=1)
        {
            folderAdapter =new FolderAdapter(getContext(),folders);
            recyclerView.setAdapter(folderAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        }

        return view;
    }
}