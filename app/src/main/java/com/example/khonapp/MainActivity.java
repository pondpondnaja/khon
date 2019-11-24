package com.example.khonapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "mainAc";
    //private static final String URL = "http://192.168.64.2/3D/news.php";
    private static final String URL   = "http://mungmee.ddns.net/3D/news.php";

    private DrawerLayout drawer;
    private Toast backToast;
    boolean doubleBackToExitPressedOnce = false;
    boolean isRunning                   = false;

    FragmentManager fragmentManager;
    NavigationView navigationView;
    Toolbar toolbar;
    RecyclerView recyclerView,recyclerView_new;
    Runnable runtoLeft;
    Handler handler;

    //Recycle vars.
    private ArrayList<String> mName        = new ArrayList<>();
    private ArrayList<String> mImageURL    = new ArrayList<>();
    private ArrayList<String> mDescription = new ArrayList<>();
    //Layout
    LinearLayoutManager layoutManager,layoutManager_new;
    SlideRecycleViewAdapter adapter;
    Full_NewViewAdapter adapter_new;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: MainScreen");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        toolbar         = findViewById(R.id.toolbar);
        drawer          = findViewById(R.id.drawer_layout);
        navigationView  = findViewById(R.id.navigationView);

        navigationView.setNavigationItemSelectedListener(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawer, toolbar,
                R.string.navigation_draw_open, R.string.navigation_draw_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null){
            navigationView.setCheckedItem(R.id.home_section);
        }
        //initImageBitmap();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: AutoScroll Enable");
        initImageBitmap();
        super.onStart();
    }

    @Override
    protected void onResume(){
        if(fragmentManager.getBackStackEntryCount() == 0){
            Log.d(TAG, "onResume: RecycleView AutoScroll Resume BackStack = "+fragmentManager.getBackStackEntryCount());
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: RecycleView AutoScroll Pause and Remove Callback");
        handler.removeCallbacks(runtoLeft);
        isRunning = false;
        mName.clear();
        mImageURL.clear();
        mDescription.clear();
        recyclerView.clearFocus();
        recyclerView.clearOnScrollListeners();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: recycle destroy");
        handler.removeCallbacks(runtoLeft);
        recyclerView.clearFocus();
        recyclerView.stopScroll();
        recyclerView.clearOnScrollListeners();
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()) {

            case R.id.home_section:
                toolbar.setTitle(getResources().getString(R.string.app_name));
                int backStackEntry = getSupportFragmentManager().getBackStackEntryCount();
                if (backStackEntry > 0) {
                    for (int i = 0; i < backStackEntry; i++) {
                        fragmentManager.popBackStackImmediate();
                        onResume();
                    }
                }
                Log.d(TAG, "onNavigationItemSelected: call on resume : backstack "+fragmentManager.getBackStackEntryCount());
                break;

            case R.id.pic_detect:
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_right,R.anim.slide_in_right,R.anim.slide_out_right)
                        .replace(R.id.fragment_container, new CameraFragment(),"camera").addToBackStack("pic_detect").commit();
                onPause();
                getSupportActionBar().hide();
                break;

            case R.id.ar_model:
                toolbar.setTitle(menuItem.getTitle().toString());
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_right,R.anim.slide_in_right,R.anim.slide_out_right)
                        .replace(R.id.fragment_container, new ARFragment(),"ar_model").addToBackStack("ar").commit();
                Log.d(TAG, "onNavigationItemSelected: Back stack AR = "+getSupportFragmentManager().getBackStackEntryCount());
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ARFragment(),"ar_model").addToBackStack(null).commit();
                onPause();
                break;

            case R.id.message:
                Intent emailIntent = new Intent(android.content.Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:giggabome@gmail.com?subject=" + "Provide support");
                emailIntent.setData(data);
                startActivity(emailIntent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public void initImageBitmap(){

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>(){
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "onResponse: JSON respond : "+response);
                        for (int i = 0; i < response.length(); i++) {                    // Parsing json
                            try {
                                JSONObject obj     = response.getJSONObject(i);
                                String title       = obj.getString("title");
                                String description = obj.getString("description");
                                String image_url   = obj.getString("img_url");
                                Log.d(TAG, "onResponse: Title : "+title+" Image url : "+image_url);

                                mName.add(title);
                                mDescription.add(description);
                                mImageURL.add(image_url);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        initRecycleView();
                        scrollable();
                        autoScrolltoLeft();
                    }
                },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: Error appear");
                Toast.makeText(MainActivity.this,"Please check your internet connection or go to contact us.",Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    private void initRecycleView() {
        Log.d(TAG, "initRecycleView: init RecycleView");

        layoutManager_new = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView_new  = findViewById(R.id.recycleview_new_full);
        recyclerView_new.setLayoutManager(layoutManager_new);
        adapter_new       = new Full_NewViewAdapter(mName,mImageURL,mDescription,this);
        recyclerView_new.setHasFixedSize(false);
        recyclerView_new.setAdapter(adapter_new);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView  = findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(layoutManager);
        adapter       = new SlideRecycleViewAdapter(this,this, mName, mImageURL,mDescription);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
        scrollable();
    }

    public void scrollable() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int last = layoutManager.findLastCompletelyVisibleItemPosition();
                int finalSize = adapter.getItemCount() - 1;
                if (last == finalSize){
                    layoutManager.scrollToPosition(0);
                }
            }
        });
    }

    public void autoScrolltoLeft() {
        handler   = new Handler();
        runtoLeft = new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                recyclerView.scrollBy(2, 0);
                handler.postDelayed(this, 0);
            }
        };
        handler.postDelayed(runtoLeft, 0);
    }

    @Override
    public void onBackPressed(){

        getSupportActionBar().show();

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            Log.d(TAG, "onBackPressed: Backstack 1 = "+fragmentManager.getBackStackEntryCount());

        }else if(fragmentManager.getBackStackEntryCount() > 0){
            fragmentManager.popBackStackImmediate();
        }else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            backToast = Toast.makeText(this,"BACK again to exit.", Toast.LENGTH_SHORT);
            backToast.show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }

            }, 2000);
        } else {
            backToast.cancel();
            super.onBackPressed();
            return;
        }

        if(fragmentManager.getBackStackEntryCount() == 0){
            Log.d(TAG, "onBackPressed: Backstack = " + fragmentManager.getBackStackEntryCount());

            toolbar.setTitle(getResources().getString(R.string.app_name));
            getSupportActionBar().show();
            Log.d(TAG, "onBackPressed: Runable status: "+isRunning);
            if(!isRunning) {
                scrollable();
                autoScrolltoLeft();
                Log.d(TAG, "onBackPressed: Resume!!");
            }
        }
    }
}
