package com.example.mymusicplayer2;

import static com.example.mymusicplayer2.MainActivity.musicFiles;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;


public class AllSongsFragment extends Fragment {
    ArrayList<String> arrayList;
    ListView allSongsListView;
    ProgressBar progressBar;
    TextView noItemFoundTV;
    View view;
    static ArrayAdapter<String> allSongsAdapter;
    public AllSongsFragment() {
        // Required empty public constructor
    }
     //creating option menu
     // onCreateOptionsMenu() when the user opens the menu for the first time
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        //inflater allows you to create a View from a resource layout file so that you do not need to create everything programmatically
        inflater.inflate(R.menu.all_songs_fragment_menu,menu);
        MenuItem item=menu.findItem(R.id.searchOption);

        //getting action defined in all_songs_fragment_menu menu
        SearchView searchView=(SearchView) item.getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // onQueryTextSubmit() Called when the user submits the query
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

           // onQueryTextChange() Called when the query text is changed by the user.
            @Override
            public boolean onQueryTextChange(String newText) {
                progressBar.setVisibility(View.VISIBLE);
                noItemFoundTV.setVisibility(View.INVISIBLE);
                ListView searchedSongsListView=view.findViewById(R.id.allSongsListView);
                searchedSongsListView.setVisibility(View.VISIBLE);
                String userInput=newText.toLowerCase();
                List<String> arrayList= musicFiles.stream().map(MusicFiles::getTitle).filter(title -> title.toLowerCase().contains(userInput)).collect(Collectors.toList());
                ArrayAdapter<String> allSongsAdapter = new ArrayAdapter<>(requireContext(),R.layout.list_item_text,R.id.listItemTextview, arrayList);
                searchedSongsListView.setAdapter(allSongsAdapter);
                progressBar.setVisibility(View.INVISIBLE);

                if(arrayList.size()>=1){
                    searchedSongsListView.setOnItemClickListener((parent, view, position, id) -> {
                        Intent intent=new Intent(getContext(),PlayerActivity.class);
                        intent.putExtra("searchedAllSongsFragmentSong", arrayList.get(position));
                        startActivity(intent);
                    });
                }
                else{
                    searchedSongsListView.setVisibility(View.INVISIBLE);
                    noItemFoundTV.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // setHasOptionsMenu() Report that this fragment would like to participate in populating the options menu by receiving a call to
        // onCreateOptionsMenu(Menu, MenuInflater) and related methods.
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        arrayList = new ArrayList<>();
        for (int i = 0; i < musicFiles.size(); i++) {
            arrayList.add(musicFiles.get(i).getTitle());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

       view=inflater.inflate(R.layout.fragment_all_songs, container, false);
        allSongsListView = view.findViewById(R.id.allSongsListView);
        progressBar = view.findViewById(R.id.progress_bar);
        noItemFoundTV= view.findViewById(R.id.all_song_no_item_tv);
         if(musicFiles.size()>=1) {
             allSongsAdapter = new ArrayAdapter<>(requireContext(),R.layout.list_item_text,R.id.listItemTextview, arrayList);
             allSongsListView.setAdapter(allSongsAdapter);
             allSongsListView.setOnItemClickListener((parent, view, position, id) -> {
                 Intent intent=new Intent(getContext(),PlayerActivity.class);
                 intent.putExtra("position",position);
                 startActivity(intent);
             });
        }
         else{
             allSongsListView.setVisibility(View.INVISIBLE);
             noItemFoundTV.setVisibility(View.VISIBLE);
         }
        return view;
    }

}