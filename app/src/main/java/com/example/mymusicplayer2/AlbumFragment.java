package com.example.mymusicplayer2;


import static com.example.mymusicplayer2.MainActivity.musicFiles;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class AlbumFragment extends Fragment {
    static ArrayList<MusicFiles> albums = new ArrayList<>();
    RecyclerView recyclerView;
    AlbumAdapter albumAdapter;
    TextView noItemPresentTV;
    View view;

    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Set<String> duplicateAlbums = new HashSet<>();
        for (MusicFiles singleFile : musicFiles) {
            if (!duplicateAlbums.contains(singleFile.getAlbum())) {
                albums.add(singleFile);
                duplicateAlbums.add(singleFile.getAlbum());
            }
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_album, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        noItemPresentTV = view.findViewById(R.id.album_no_item_tv);
        if (albums.size() >= 1) {
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            albumAdapter = new AlbumAdapter(getContext(), albums);
            recyclerView.setAdapter(albumAdapter);
        }
        else{
            recyclerView.setVisibility(View.INVISIBLE);
            noItemPresentTV.setVisibility(View.VISIBLE);
        }
        return view;
    }
}