package com.example.mymusicplayer2;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.widget.TextView;


import java.util.ArrayList;


public class RecentSongsFragment extends Fragment {
    ArrayList<String> arrayList;
    ListView recentSongsListView;
    TextView noItemFoundTV;
    View  view;
    static ArrayAdapter<String> recentSongsAdapter;
    public RecentSongsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arrayList = new ArrayList<>();
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<MusicFiles> musicFiles = new RecentDbHelper(getContext()).fetchRecentSongs();
        arrayList.clear();
        for (int i = 0; i < musicFiles.size(); i++) {
            arrayList.add(musicFiles.get(i).getTitle());
        }
        if(arrayList.size()>=1) {
            recentSongsAdapter = new ArrayAdapter<>(requireContext(),R.layout.list_item_text,R.id.listItemTextview, arrayList);
            recentSongsListView.setAdapter(recentSongsAdapter);
            recentSongsListView.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent=new Intent(getContext(),PlayerActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("fromRecentList",true);
                startActivity(intent);
            });
        }
        else{
            recentSongsListView.setVisibility(View.INVISIBLE);
            noItemFoundTV.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recent_songs, container, false);
        recentSongsListView = view.findViewById(R.id.recentSongsListView);
        noItemFoundTV = view.findViewById(R.id.recent_song_no_item_tv);
        return view;
    }
}