package com.example.mymusicplayer2;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mymusicplayer2.MainActivity.musicFiles;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class AllSongsFragment extends Fragment {
    ArrayList<String> arrayList;
    ListView allSongsListView;
    View view;
    static ArrayAdapter<String> allSongsAdapter;
    public static String mySortPreferences="SortOrder";
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
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // onQueryTextSubmit() Called when the user submits the query
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

           // onQueryTextChange() Called when the query text is changed by the user.
            @Override
            public boolean onQueryTextChange(String newText) {
                ListView searchedSongsListView=view.findViewById(R.id.allSongsListView);
                ArrayList arrayList=new ArrayList<>();
                String userInput=newText.toLowerCase();
                for(MusicFiles song:musicFiles)
                {
                    if(song.getTitle().toLowerCase().contains(userInput))
                    {
                        arrayList.add(song.getTitle());
                    }
                }
                ArrayAdapter allSongsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, arrayList);
                searchedSongsListView.setAdapter(allSongsAdapter);
                searchedSongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(getContext(),PlayerActivity.class);
                        intent.putExtra("searchedAllSongsFragmentKey",arrayList.get(position).toString());
                        startActivity(intent);
                    }
                });

                return false;
            }
        });


        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // setHasOptionsMenu() Report that this fragment would like to participate in populating the options menu by receiving a call to
        // onCreateOptionsMenu(Menu, MenuInflater) and related methods.
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        SharedPreferences.Editor editor=this.getActivity().getSharedPreferences(mySortPreferences,MODE_PRIVATE).edit();
        switch (item.getItemId())
        {
            case R.id.byName:
                editor.putString("sorting","sortByName");
                editor.apply();
                this.getActivity().recreate();
                break;
            case R.id.byDate:
                editor.putString("sorting","sortByDate");
                editor.apply();
                this.getActivity().recreate();
                break;
            case R.id.bySize:
                editor.putString("sorting","sortBySize");
                editor.apply();
                //recreate() Cause this Activity to be recreated with a new instance.
                this.getActivity().recreate();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       view=inflater.inflate(R.layout.fragment_all_songs, container, false);
       if(musicFiles.size()>=1) {
           allSongsListView = view.findViewById(R.id.allSongsListView);
           arrayList = new ArrayList<>();
           for (int i = 0; i < musicFiles.size(); i++) {
               arrayList.add(musicFiles.get(i).getTitle());
           }
           allSongsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, arrayList);
           allSongsListView.setAdapter(allSongsAdapter);
       }
       allSongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               Intent intent=new Intent(getContext(),PlayerActivity.class);
               intent.putExtra("position",position);
               startActivity(intent);
           }
       });

        return view;

    }

}