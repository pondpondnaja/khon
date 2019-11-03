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

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "mainAc";

    private DrawerLayout drawer;
    private Toast backToast;
    boolean doubleBackToExitPressedOnce = false;
    boolean isRunning = false;

    FragmentManager fragmentManager;
    NavigationView navigationView;
    Toolbar toolbar;
    RecyclerView recyclerView;
    Runnable runtoLeft;
    Handler handler;

    //Recycle vars.
    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mImageURL = new ArrayList<>();
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: MainScreen");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
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
            recyclerView.getFocusable();
            layoutManager.scrollToPosition(0);
            scrollable();
            autoScrolltoLeft();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: RecycleView AutoScroll Pause and Remove Callback");
        handler.removeCallbacks(runtoLeft);
        isRunning = false;
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
                toolbar.setTitle("KHON Application");
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
            toolbar.setTitle("KHON Application");
            getSupportActionBar().show();
            Log.d(TAG, "onBackPressed: Runable status: "+isRunning);
            if(!isRunning) {
                onResume();
                Log.d(TAG, "onBackPressed: Resume!!");
            }
        }
    }

    public void initImageBitmap() {
        Log.d(TAG, "initImageBitmap: preparing Bitmap");

        mImageURL.add("http://www.finearts.go.th/promotion/images/266/Kathin2562/XL1A7916.jpg");
        mName.add("กรมศิลปากรกำหนดถวายผ้าพระกฐินพระราชทาน ประจำปี ๒๕๖๒ ณ วัดทรงศิลา จังหวัดชัยภูมิ");

        mImageURL.add("http://www.finearts.go.th/promotion/images/266/62-10-08_65thNationalExhibition/XL1A8685.jpg");
        mName.add("พิธีเปิดการแสดงศิลปกรรมแห่งชาติ ครั้งที่ ๖๕ ประจำปี ๒๕๖๒");

        mImageURL.add("http://www.finearts.go.th/promotion/images/266/62-10-02/62-10-02_11.JPG");
        mName.add("เตรียมความพร้อมต้อนรับภริยาผู้นำประเทศอาเซียน");

        mImageURL.add("http://www.finearts.go.th/promotion/images/266/62-10-10/62-10-10_01.jpg");
        mName.add("ศิลปินสำนักการสังคีต ฝึกซ้อมการแสดงนาฏศิลป์และดนตรีไทย");

        initRecycleView();
    }

    private void initRecycleView() {
        Log.d(TAG, "initRecycleView: init RecycleView");

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView = findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(layoutManager);
        SlideRecycleViewAdapter adapter = new SlideRecycleViewAdapter(this,this, mName, mImageURL);
        recyclerView.setAdapter(adapter);
        scrollable();
    }

    public void scrollable() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstItemVisible = layoutManager.findFirstCompletelyVisibleItemPosition();
                int last = layoutManager.findLastCompletelyVisibleItemPosition();
                int finalSize = mImageURL.size() - 1;
                if (last == finalSize){
                    layoutManager.scrollToPosition(0);
                }
            }
        });
    }

    public void autoScrolltoLeft() {
        handler = new Handler();
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
}
