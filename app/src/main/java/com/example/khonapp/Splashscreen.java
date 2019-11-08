package com.example.khonapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class Splashscreen extends AppCompatActivity {

    Handler handler;
    Runnable runnable;
    long delay_time;
    long time = 2000L;

    RelativeLayout sp_txt;
    Animation textmove_in,textmove_out;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        sp_txt = findViewById(R.id.splash_text);
        textmove_in = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        textmove_out = AnimationUtils.loadAnimation(this,R.anim.text_fade_out);

        handler = new Handler();

        runnable = new Runnable() {
            public void run() {

                Intent intent = new Intent(Splashscreen.this, MainActivity.class);
                sp_txt.startAnimation(textmove_out);

                textmove_out.setAnimationListener(new Animation.AnimationListener(){
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation){
                        sp_txt.setVisibility(View.GONE);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in,R.anim.slide_out_left);
                        finish();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        };
    }

    public void onResume() {
        super.onResume();
        sp_txt.startAnimation(textmove_in);
        delay_time = time;
        handler.postDelayed(runnable, delay_time);
        time = System.currentTimeMillis();
    }

    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
        time = delay_time - (System.currentTimeMillis() - time);
    }
}
