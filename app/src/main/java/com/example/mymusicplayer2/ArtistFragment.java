package com.example.mymusicplayer2;

import static com.example.mymusicplayer2.MainActivity.artists;
import static com.example.mymusicplayer2.MainActivity.musicFiles;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ArtistFragment extends Fragment {
    ArrayList<String> arrayList;
    ListView allArtistListView;
    View view;
    static ArrayAdapter<String> allArtistAdapter;
    public ArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_artist, container, false);
        if(artists.size()>=1) {
            allArtistListView = view.findViewById(R.id.allArtistListView);
            arrayList = new ArrayList<>();
            for (int i = 0; i < artists.size(); i++) {
                arrayList.add(artists.get(i).getArtist());
            }
            allArtistAdapter = new ArrayAdapter<>(getContext(), R.layout.list_itrm_text,R.id.listItemTextview, arrayList);
            allArtistListView.setAdapter(allArtistAdapter);
        }
        allArtistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getContext(),ArtistDetails.class);
                intent.putExtra("artistName",arrayList.get(position));
                startActivity(intent);
            }
        });

        return view;
    }
}