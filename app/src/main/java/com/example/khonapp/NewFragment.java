package com.example.khonapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class NewFragment extends Fragment {
    private static final String TAG = "newAc";

    private Bundle bundle;
    private Toolbar toolbar;
    private ImageView news_img;
    private TextView news_title, news_detail;
    private ProgressBar progressBar2;
    private MainActivity activity;

    private String new_img,new_title,news_des = "";
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.activity_news,container,false);

        context = view.getContext();
        news_img     = view.findViewById(R.id.news_img);
        news_title   = view.findViewById(R.id.news_title);
        news_detail  = view.findViewById(R.id.news_detail);
        progressBar2 = view.findViewById(R.id.progressBar_news);

        activity = (MainActivity) getActivity();
        AppCompatActivity news_activity = (AppCompatActivity) view.getContext();

        bundle = getArguments();
        if(bundle != null){
            new_img   = bundle.getString("new_img");
            new_title = bundle.getString("new_title");
            news_des  = bundle.getString("news_des");

            activity.setToolbarTitle(new_title);
            news_activity.getSupportActionBar().show();
            new Handler().postDelayed(() -> activity.toolbar_text.setSelected(true), 1000);
        }

        setData();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity.toolbar_text.setSelected(false);
    }

    private void setData() {
        Glide.with(context)
                .asBitmap()
                .load(new_img)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource){
                        Toast.makeText(getContext(),"Can't load image.",Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar2.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(news_img);
        news_title.setText(new_title);
        news_detail.setText(news_des);
    }
}
