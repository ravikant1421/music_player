package com.example.mymusicplayer2;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;


import java.io.IOException;
import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyHolderAlbum> {
    private Context context;
    private ArrayList<MusicFiles> musicFiles;
    View view;
    public AlbumAdapter(){
    }

    public AlbumAdapter(Context context, ArrayList<MusicFiles> musicFiles) {
        this.context = context;
        this.musicFiles = musicFiles;
    }

    @NonNull
    @Override
    public MyHolderAlbum onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view= LayoutInflater.from(context).inflate(R.layout.album_item,parent,false);
        return new MyHolderAlbum(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolderAlbum holder, int position) {
        holder.albumName.setText(musicFiles.get(position).getAlbum());
        byte[]  image = new byte[0];
        try {
            image = getAlbumArt(musicFiles.get(position).getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(image!=null)
        {
            Glide.with(context)
                    .asBitmap()
                    .load(image)
                    .into(holder.albumImage);
        }
        else
        {
            Glide.with(context)
                    .asBitmap()
                    .load(R.drawable.ic_music_player)
                    .into(holder.albumImage);
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(context,AlbumDetails.class);
            intent.putExtra("albumName",musicFiles.get(position).getAlbum());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return musicFiles.size();
    }

    public class MyHolderAlbum extends RecyclerView.ViewHolder {
        ImageView albumImage;
        TextView albumName;
        public MyHolderAlbum(@NonNull View itemView) {
            super(itemView);
            albumImage=itemView.findViewById(R.id.albumImage);
            albumName=itemView.findViewById(R.id.albumName);

        }
    }
    public byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
