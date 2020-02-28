package com.example.khonapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class Full_NewViewAdapter extends RecyclerView.Adapter<Full_NewViewAdapter.ViewHolder> {
    private static final String TAG = "NewAC";
    private ArrayList<String> mName        = new ArrayList<>();
    private ArrayList<String> mImageURL    = new ArrayList<>();
    private ArrayList<String> mDescription = new ArrayList<>();
    private Context mcontext;
    Bundle bundle;

    public Full_NewViewAdapter(ArrayList<String> mName, ArrayList<String> mImageURL, ArrayList<String> mDescription, Context mcontext) {
        this.mName = mName;
        this.mImageURL = mImageURL;
        this.mDescription = mDescription;
        this.mcontext = mcontext;
    }

    @Override
    public Full_NewViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_list_full,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.new_title.setText(mName.get(position).trim().replace("&nbsp;", "").replace("<br>", ""));
        holder.new_title.setSelected(true);

        String URL_Builder = "http://192.168.1.43:5000/static/images/news/" + mImageURL.get(position);
        Glide.with(mcontext)
                .asBitmap()
                .load(URL_Builder)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(mcontext, "Can't load image.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onLoadFailed: Message : " + e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.news_full_img);

        holder.new_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: click on image : " + mName.get(position));
                Toast.makeText(mcontext, mName.get(position),Toast.LENGTH_SHORT).show();

                bundle = new Bundle();
                bundle.putString("new_title",mName.get(position));
                bundle.putString("new_img",mImageURL.get(position));
                bundle.putString("news_des",mDescription.get(position));

                NewFragment newFragment = new NewFragment();
                newFragment.setArguments(bundle);

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_right,R.anim.slide_in_right,R.anim.slide_out_right)
                        .add(R.id.fragment_container, newFragment, "news_full_item").addToBackStack("news_full_item").commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView new_title;
        CardView new_parent;
        ImageView news_full_img;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            new_title = itemView.findViewById(R.id.new_title_full);
            new_parent = itemView.findViewById(R.id.new_parent);
            progressBar = itemView.findViewById(R.id.progressBar);
            news_full_img = itemView.findViewById(R.id.news_img_fullList);
        }
    }
}
