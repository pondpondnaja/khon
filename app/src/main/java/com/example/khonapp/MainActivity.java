package com.example.khonapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
    private static final String TAG = "main";

    private DrawerLayout drawer;
    FragmentManager fragmentManager;
    NavigationView navigationView;
    Toolbar toolbar;
    RecyclerView recyclerView;
    Runnable runtoLeft;
    Handler handler;
    MenuItem menuItem;

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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CameraFragment(),"camera").addToBackStack(null).commit();
                onPause();
                getSupportActionBar().hide();
                break;

            case R.id.ar_model:
                toolbar.setTitle(menuItem.getTitle().toString());
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ARFragment(),"ar_model").addToBackStack(null).commit();
                onPause();
                break;

            case R.id.exit_btn:
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Exit");
                builder.setMessage("Do you want to exit ?");
                builder.setPositiveButton("Yes, Exit now !", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;

        }

        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        if(fragmentManager.getBackStackEntryCount() == 0) {
            Log.d(TAG, "onBackPressed: Backstack = " + fragmentManager.getBackStackEntryCount());
            toolbar.setTitle("KHON APPLICATION");
            getSupportActionBar().show();
            onResume();
        }
        getSupportActionBar().show();

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
        SlideRecycleViewAdapter adapter = new SlideRecycleViewAdapter(this, mName, mImageURL);
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
                Log.d(TAG, "onScrolled: firstitem : " + firstItemVisible + " Lastitem : " + last + " FinalSize : " + finalSize);
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
                recyclerView.scrollBy(2, 0);
                handler.postDelayed(this, 0);
            }
        };
        handler.postDelayed(runtoLeft, 0);
    }
}
