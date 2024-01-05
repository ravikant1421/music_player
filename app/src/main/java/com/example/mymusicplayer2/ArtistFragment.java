package com.example.mymusicplayer2;
import static com.example.mymusicplayer2.MainActivity.musicFiles;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ArtistFragment extends Fragment {
    ArrayList<String> allArtist;
    ListView allArtistListView;
    TextView noItemFoundTV;
    View view;
    static ArrayList<MusicFiles> artists=new ArrayList<>();
    static ArrayAdapter<String> allArtistAdapter;
    public ArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allArtist = new ArrayList<>();
        Set<String> duplicateArtists =new HashSet<>();
        for(MusicFiles singleFile : musicFiles){
            if( !duplicateArtists.contains(singleFile.getArtist()) ){
                artists.add(singleFile);
                allArtist.add(singleFile.getArtist());
                duplicateArtists.add(singleFile.getArtist());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_artist, container, false);
        if(artists.size()>=1) {
            allArtistListView = view.findViewById(R.id.allArtistListView);
            allArtistAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_item_text,R.id.listItemTextview, allArtist);
            allArtistListView.setAdapter(allArtistAdapter);
            allArtistListView.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent=new Intent(getContext(),ArtistDetails.class);
                intent.putExtra("artistName",allArtist.get(position));
                startActivity(intent);
            });
        }
        else {
            allArtistListView.setVisibility(View.INVISIBLE);
            noItemFoundTV.setVisibility(View.VISIBLE);
        }


        return view;
    }
}