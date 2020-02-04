package com.example.khonapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {//implements NavigationView.OnNavigationItemSelectedListener
    private static final String TAG = "mainAc";

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int GALLERY_REQUEST_CODE = 1002;
    private static final int Limit = 4;
    //private static final String URL = "http://192.168.64.2/3D/news.php";
    private static final String URL = "https://utg-fansub.me/3D/news.php";

    //private DrawerLayout drawer;
    private Toast backToast;

    boolean doubleBackToExitPressedOnce = false;
    boolean isRunning = false;
    Uri image_uri;

    private FragmentManager fragmentManager;
    private RecyclerView recyclerView;

    public Toolbar toolbar;
    public ImageView overlay;
    public TextView toolbar_text;
    public CardView ar_card, detect_card, os_1, os_2, optional, camera_holder, gallery_holder;

    //Recycle vars.
    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mImageURL = new ArrayList<>();
    private ArrayList<String> mDescription = new ArrayList<>();

    //Layout
    LinearLayoutManager layoutManager;
    SlideRecycleViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: MainScreen");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }

        fragmentManager = getSupportFragmentManager();
        toolbar = findViewById(R.id.toolbar);
        ar_card = findViewById(R.id.card_1);
        detect_card = findViewById(R.id.card_2);
        os_1 = findViewById(R.id.os_1);
        os_2 = findViewById(R.id.os_2);
        overlay = findViewById(R.id.img_overlay_optional);
        optional = findViewById(R.id.option_holder);
        camera_holder = findViewById(R.id.camera_optional_holder);
        gallery_holder = findViewById(R.id.gallery_holder);
        toolbar_text = toolbar.findViewById(R.id.text_toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar_text.setText(toolbar.getTitle());
        /*
        navigationView  = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawer, toolbar,
                R.string.navigation_draw_open, R.string.navigation_draw_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        */

        if (savedInstanceState == null) {
            toolbar_text.setText(getResources().getString(R.string.sp_text1));
        }

        ar_card.setOnClickListener(view -> ARClick());

        detect_card.setOnClickListener(view -> {
            optional.setVisibility(View.VISIBLE);
            overlay.setVisibility(View.VISIBLE);
        });

        overlay.setOnClickListener(view -> {
            if (optional.getVisibility() == View.VISIBLE) {
                optional.setVisibility(View.GONE);
                overlay.setVisibility(View.GONE);
            }
        });

        camera_holder.setOnClickListener(view -> CameraClick());
        gallery_holder.setOnClickListener(view -> GalleryClick());

        os_1.setOnClickListener(view -> {
            String mailto = "mailto:supporter@gmail.com" +
                    "?subject=" + Uri.encode("Provide support");
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailto));
            startActivity(emailIntent);
        });
        //initImageBitmap();
    }


    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: AutoScroll Enable");
        initImageBitmap();
        super.onStart();
    }

    @Override
    protected void onResume() {
        if (optional.getVisibility() == View.VISIBLE) {
            optional.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
        }
        if (fragmentManager.getBackStackEntryCount() == 0) {
            Log.d(TAG, "onResume: RecycleView AutoScroll Resume BackStack = " + fragmentManager.getBackStackEntryCount());
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: RecycleView AutoScroll Pause and Remove Callback");
        //recyclerView.clearFocus();
        //recyclerView.clearOnScrollListeners();
        Log.d(TAG, "onPause: mName     : " + mName.size());
        Log.d(TAG, "onPause: mImageURL : " + mImageURL.size());
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: recycle destroy");
        //handler.removeCallbacks(runtoLeft);
        recyclerView.clearFocus();
        recyclerView.stopScroll();
        recyclerView.clearOnScrollListeners();
        super.onDestroy();
    }

/*Side Menu.
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
*/

    public void initImageBitmap() {

        if (!mImageURL.isEmpty() || !mName.isEmpty() || !mDescription.isEmpty()) {
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                response -> {
                    Log.d(TAG, "onResponse: JSON respond : " + response);
                    for (int i = 0; i < Limit; i++) {                    // Parsing json
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String title = obj.getString("title");
                            String description = obj.getString("description");
                            String image_url = obj.getString("img_url");
                            Log.d(TAG, "onResponse: Title : " + title + " Image url : " + image_url);

                            mName.add(title);
                            mDescription.add(description);
                            mImageURL.add(image_url);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    mName.add("Read more");
                    mDescription.add("Read more");
                    mImageURL.add("Read more");
                    initRecycleView();
                    //scrollable();
                    //autoScrolltoLeft();
                }, error -> {
            Log.d(TAG, "onErrorResponse: Error appear");
            Toast.makeText(MainActivity.this, "Please check your internet connection or go to contact us.", Toast.LENGTH_LONG).show();
            error.printStackTrace();
        });
        requestQueue.add(request);
    }

    private void initRecycleView() {
        Log.d(TAG, "initRecycleView: init RecycleView");
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView = findViewById(R.id.recycleview);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SlideRecycleViewAdapter(this, this, mName, mImageURL, mDescription);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
        //scrollable();
    }

    /*public void scrollable() {
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
    }*/

    public void ARClick() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                .replace(R.id.fragment_container, new ARFragment(), "ar_model").addToBackStack("ar").commit();
        Log.d(TAG, "onNavigationItemSelected: Back stack AR = " + getSupportFragmentManager().getBackStackEntryCount());
        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ARFragment(),"ar_model").addToBackStack(null).commit();
        onPause();
        getSupportActionBar().hide();
    }

    public void GalleryClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                //permission not enabled, request it
                String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                //show popup to request permissions
                requestPermissions(permission, PERMISSION_CODE);
            } else {
                //permission already granted
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                String[] mimeTypes = {"image/jpeg", "image/png"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        }
    }

    public void CameraClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                //permission not enabled, request it
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                //show popup to request permissions
                requestPermissions(permission, PERMISSION_CODE);
            } else {
                //permission already granted
                openCamera();
            }
        } else {
            //system os < marshmallow
            openCamera();
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //called when image was captured from camera
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case IMAGE_CAPTURE_CODE:
                    //set the image captured to our ImageView
                    Intent previewIn = new Intent(MainActivity.this, ResultActivity.class);
                    previewIn.putExtra("img_path", image_uri.toString());
                    startActivity(previewIn);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;

                case GALLERY_REQUEST_CODE:
                    Uri selectedImage = data.getData();
                    Intent galleryIn = new Intent(MainActivity.this, ResultActivity.class);
                    assert selectedImage != null;
                    galleryIn.putExtra("img_path", selectedImage.toString());
                    startActivity(galleryIn);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void eventClick(View view) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                .replace(R.id.fragment_container, new CalendarFragment(), "event").addToBackStack("event").commit();
        onPause();
    }

    public void setToolbarTitle(String text) {
        toolbar_text.setText(text);
    }

    @Override
    public void onBackPressed() {

        Objects.requireNonNull(getSupportActionBar()).show();

        /*
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            Log.d(TAG, "onBackPressed: Backstack 1 = "+fragmentManager.getBackStackEntryCount());

        }else
        */

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        } else if (optional.getVisibility() == View.VISIBLE) {
            optional.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
        } else if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            backToast = Toast.makeText(this, "BACK again to exit.", Toast.LENGTH_SHORT);
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

        if (fragmentManager.getBackStackEntryCount() == 0) {
            Log.d(TAG, "onBackPressed: BackStack = " + fragmentManager.getBackStackEntryCount());

            toolbar_text.setText(getResources().getString(R.string.sp_text1));
            getSupportActionBar().show();
            Log.d(TAG, "onBackPressed: RunAble status: " + isRunning);
            if (!isRunning) {
                Log.d(TAG, "onBackPressed: Resume!!");
                Log.d(TAG, "onBackPressed: RunAble status: " + isRunning);
            }
        }
    }

    /*--------------------------------------------PERMISSION CHECK--------------------------------------------*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission from popup was granted
                openCamera();
            } else {
                //permission from popup was denied
                Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
