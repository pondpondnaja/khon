package com.example.khonapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class SlideRecycleViewAdapter extends RecyclerView.Adapter<SlideRecycleViewAdapter.ViewHolder>{

    private static final String TAG = "SlideRecycleView";

    //vars
    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mImageURL = new ArrayList<>();
    private Context mcontext;

    public SlideRecycleViewAdapter(Context mcontext, ArrayList<String> mName, ArrayList<String> mImageURL){
        this.mName = mName;
        this.mImageURL = mImageURL;
        this.mcontext = mcontext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder:  called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        Log.d(TAG, "onBindViewHolder: called");
        Glide.with(mcontext).asBitmap().load(mImageURL.get(position)).into(holder.img);

        holder.text.setText(mName.get(position));
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: click on image : " + mName.get(position));
                Toast.makeText(mcontext, mName.get(position),Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mcontext,NewsInfoActivity.class);
                intent.putExtra("new_title",mName.get(position));
                intent.putExtra("new_img",mImageURL.get(position));

                mcontext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageURL.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.image_view);
            text = itemView.findViewById(R.id.text_view);
        }
    }
}