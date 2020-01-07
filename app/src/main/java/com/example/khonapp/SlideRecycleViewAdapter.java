package com.example.khonapp;

import android.app.Activity;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

public class SlideRecycleViewAdapter extends RecyclerView.Adapter<SlideRecycleViewAdapter.ViewHolder> {

    private static final String TAG = "SlideRecycleView";
    private static final int Slide_size = 4;
    //vars
    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mImageURL = new ArrayList<>();
    private ArrayList<String> mDescription = new ArrayList<>();
    private Context mcontext;
    private Activity mActivity;
    Bundle bundle;
    View view;

    public SlideRecycleViewAdapter(Activity mActivity, Context mContext, ArrayList<String> mName, ArrayList<String> mImageURL, ArrayList<String> mDescription) {
        this.mName = mName;
        this.mImageURL = mImageURL;
        this.mDescription = mDescription;
        this.mcontext = mContext;
        this.mActivity = mActivity;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: TypeView : " + position + " mNameSize = " + mName.size());
        if (position == Slide_size) {
            return 2;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder:  called View Type : " + viewType);
        if (viewType == 2) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_lastitem, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");
        Log.d(TAG, "onBindViewHolder: ImgURL : " + mImageURL.get(position));
        //Set image
        if (position == getItemCount() - 1) {
            Glide.with(mcontext)
                    .load(R.drawable.ic_play_circle_outline_black_24dp)
                    .into(holder.img);
        } else {
            Glide.with(mcontext)
                    .asBitmap()
                    .load(mImageURL.get(position))
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
                    .into(holder.img);
        }

        holder.parentLayout.setOnClickListener(view -> {
            Log.d(TAG, "onClick: click on image : " + mName.get(position));
            Toast.makeText(mcontext, mName.get(position), Toast.LENGTH_SHORT).show();

            if (position == Slide_size) {
                AppCompatActivity news_activity = (AppCompatActivity) view.getContext();
                news_activity.getSupportActionBar().hide();
                news_activity.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                        .replace(R.id.fragment_container, new Full_NewListFragment(), "full_news").addToBackStack("full_news").commit();
            } else {
                bundle = new Bundle();
                bundle.putString("new_title", mName.get(position));
                bundle.putString("new_img", mImageURL.get(position));
                bundle.putString("news_des", mDescription.get(position));

                NewFragment newFragment = new NewFragment();
                newFragment.setArguments(bundle);

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                        .replace(R.id.fragment_container, newFragment, "news").addToBackStack("news").commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return Slide_size + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView parentLayout;
        ImageView img;
        TextView t_news;
        ProgressBar progressBar;
        //TextView text;

        private ViewHolder(View itemView) {
            super(itemView);
            parentLayout = itemView.findViewById(R.id.parentLayout);
            img = itemView.findViewById(R.id.image_view);
            progressBar = itemView.findViewById(R.id.progressBar);
            t_news = itemView.findViewById(R.id.text_news);
            //text = itemView.findViewById(R.id.text_view);
        }
    }
}