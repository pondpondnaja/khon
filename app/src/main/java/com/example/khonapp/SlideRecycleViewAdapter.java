package com.example.khonapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class SlideRecycleViewAdapter extends RecyclerView.Adapter<SlideRecycleViewAdapter.ViewHolder>{

    private static final String TAG = "SlideRecycleView";

    //vars
    private ArrayList<String> mName        = new ArrayList<>();
    private ArrayList<String> mImageURL    = new ArrayList<>();
    private ArrayList<String> mDescription = new ArrayList<>();
    private Context mcontext;
    private Activity mActivity;

    public SlideRecycleViewAdapter(Activity mActivity, Context mcontext, ArrayList<String> mName, ArrayList<String> mImageURL, ArrayList<String> mDescription){
        this.mName        = mName;
        this.mImageURL    = mImageURL;
        this.mDescription = mDescription;
        this.mcontext     = mcontext;
        this.mActivity    = mActivity;
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

        Glide.with(mcontext)
                .asBitmap()
                .load(mImageURL.get(position))
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(mcontext,"Can't load image.",Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.img);

        //holder.text.setText(mName.get(position));
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: click on image : " + mName.get(position));
                Toast.makeText(mcontext, mName.get(position),Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(mcontext,NewsInfoActivity.class);
                intent.putExtra("new_title",mName.get(position));
                intent.putExtra("new_img",mImageURL.get(position));
                intent.putExtra("news_des",mDescription.get(position));
                mcontext.startActivity(intent);
                mActivity.overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageURL.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        ProgressBar progressBar;
        //TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.image_view);
            progressBar = itemView.findViewById(R.id.progressBar);
            //text = itemView.findViewById(R.id.text_view);
        }
    }
}