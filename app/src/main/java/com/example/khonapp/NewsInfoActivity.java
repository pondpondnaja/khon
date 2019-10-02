package com.example.khonapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class NewsInfoActivity extends AppCompatActivity {
    private static final String TAG = "news";

    Toolbar toolbar;
    ImageView news_img;
    TextView news_title,news_detail;

    private String new_img,new_title = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        toolbar     = findViewById(R.id.toolbar_news);
        news_img    = findViewById(R.id.news_img);
        news_title  = findViewById(R.id.news_title);
        news_detail = findViewById(R.id.news_detail);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("News");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                new_img    = null;
                news_title = null;
            }else{
                new_img = extras.getString("new_img");
                new_title = extras.getString("new_title");
                Log.d(TAG, "onCreate: News image : " + new_img);
            }
        }else{
            new_img = (String) savedInstanceState.getSerializable("new_img");
            new_title = (String) savedInstanceState.getSerializable("new_title");
        }
        Glide.with(getBaseContext()).load(new_img).into(news_img);
        news_title.setText(new_title);
    }
}
