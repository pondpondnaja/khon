package com.example.khonapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class NewsInfoActivity extends AppCompatActivity {
    private static final String TAG = "news";

    Toolbar toolbar;
    ImageView news_img;
    TextView news_title,news_detail;
    ProgressBar progressBar2;

    private String new_img,new_title,news_des = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        //toolbar      = findViewById(R.id.toolbar_news);
        news_img     = findViewById(R.id.news_img);
        news_title   = findViewById(R.id.news_title);
        news_detail  = findViewById(R.id.news_detail);
        progressBar2 = findViewById(R.id.progressBar_news);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("News");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        if (savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                new_img    = null;
                news_title = null;
                news_des   = null;
            }else{
                new_img   = extras.getString("new_img");
                new_title = extras.getString("new_title");
                news_des  = extras.getString("news_des");
                Log.d(TAG, "onCreate: News image : " + new_img);
            }
        }else{
            new_img   = (String) savedInstanceState.getSerializable("new_img");
            new_title = (String) savedInstanceState.getSerializable("new_title");
            news_des  = (String) savedInstanceState.getSerializable("new_des");
        }

        //Glide.with(getBaseContext()).load(new_img).into(news_img);
        Glide.with(getBaseContext())
                .asBitmap()
                .load(new_img)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        Toast.makeText(getBaseContext(),"Can't load image.",Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void finish(){
        super.finish();
        Log.d(TAG, "finish: Finish called");
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }
}
