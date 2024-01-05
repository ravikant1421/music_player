package com.example.mymusicplayer2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.MyHolderFolder>{
    Context context;
    ArrayList<String> folders;
    View view;

    public FolderAdapter(Context context, ArrayList<String> folders) {
        this.context = context;
        this.folders = folders;
    }
    @NonNull
    @Override
    public MyHolderFolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view= LayoutInflater.from(context).inflate(R.layout.folder_item,parent,false);
        return new FolderAdapter.MyHolderFolder(view);
    }

    @Override
    public void onBindViewHolder( MyHolderFolder holder, int position) {
        holder.folderName.setText(folders.get(position));
        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(view.getContext(),FolderDetails.class);
            intent.putExtra("FolderNameKey",folders.get(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public static class MyHolderFolder extends RecyclerView.ViewHolder{
        TextView folderName;
        public MyHolderFolder(@NonNull View itemView) {
            super(itemView);
            folderName=itemView.findViewById(R.id.folderName);
        }
    }
}
