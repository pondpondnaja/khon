package com.example.khonapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import de.hdodenhof.circleimageview.CircleImageView;


public class ARActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "loadModel";
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private boolean isModelPlace;
    //private String url = "http://192.168.64.2/3D/ar_path.php?";
    private String url = "http://mungmee.ddns.net/3D/ar_path.php?";
    private String extension = ".glb";
    private String ASSET_3D = "";
    private String foldername = "";
    private String head = "http://";
    private String build_url;
    private String model_url = "";

    private BottomSheetBehavior mbottomSheetBehavior;
    private TextView mtextViewState;
    AnchorNode anchorNode;
    Anchor anchor;

    CircleImageView human_m,human_fm,giant,monkey;
    Button moreinfo;
    CoordinatorLayout parentView;
    View arrayView[];

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "SceneForm Requires Android N (Android 8.0) or later");
            Toast.makeText(activity, "SceneForm Requires Android N (Android 8.0) or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "SceneForm requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "SceneForm requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        parentView = findViewById(R.id.parentview);
        human_m  = findViewById(R.id.human_m);
        human_fm = findViewById(R.id.human_fm);
        giant    = findViewById(R.id.giant);
        monkey   = findViewById(R.id.monkey);
        moreinfo = findViewById(R.id.more);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mtextViewState = findViewById(R.id.bottom_text);
        mbottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            Intent goback = new Intent(ARActivity.this,MainActivity.class);
            Toast.makeText(ARActivity.this, "Failed to create AR session.", Toast.LENGTH_LONG).show();
            startActivity(goback);
        }else{
            if (savedInstanceState == null){
                Bundle extras = getIntent().getExtras();
                if (extras == null) {
                    foldername = null;
                }else{
                    foldername = extras.getString("foldername");
                    Log.d(TAG, "onCreate: FolderName : " + foldername);
                }
            }else{
                foldername = (String) savedInstanceState.getSerializable("foldername");
            }

            //Build Path
            getPath(foldername,"human_m");
            arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arfragment_model);

            //init model border
            human_m.setBorderColor(getResources().getColor(R.color.select_ar,getApplicationContext().getTheme()));
            human_m.setBorderWidth(15);

            setArrayView();
            setClickListener();

            //Tap to place model Method.
            /*arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
                Log.d(TAG, "onCreate: placemodel init");
                placeModel(hitResult.createAnchor());
            });*/

            //Auto detection surface Method.
            arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);
        }
        moreinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mbottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){

                    moreinfo.setBackgroundTintList(getResources().getColorStateList(R.color.Main_color_2,getApplicationContext().getTheme()));
                    mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    if(foldername.equals("Am")){

                        mtextViewState.setText("ท่าฉัน");

                    }else if(foldername.equals("Angry")){

                        mtextViewState.setText("ท่าโกรธ");

                    }else if(foldername.equals("Cry")){

                        mtextViewState.setText("ท่าร้องไห้");

                    }else if(foldername.equals("Shy")){

                        mtextViewState.setText("ท่าเขิน");

                    }else if(foldername.equals("Smile")){

                        mtextViewState.setText("ท่ายิ้ม");

                    }
                }else{
                    mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    moreinfo.setBackgroundTintList(getResources().getColorStateList(R.color.Main_color_1,getApplicationContext().getTheme()));
                }
            }
        });
    }

    private void setArrayView() {
        arrayView = new View[]{
                human_m,human_fm,giant,monkey
        };
    }

    private void setClickListener() {
        for(int i = 0; i < arrayView.length; i++) {
            arrayView[i].setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.human_m){

            removeAnchorNode(anchorNode);
            human_m.setBorderColor(getResources().getColor(R.color.select_ar,getApplicationContext().getTheme()));

            human_m.setBorderWidth(15);
            human_fm.setBorderWidth(0);
            giant.setBorderWidth(0);
            monkey.setBorderWidth(0);
            getPath(foldername,"human_m");
            Log.d(TAG, "onClick: New Path : "+ASSET_3D);

        }else if(view.getId() == R.id.human_fm){

            removeAnchorNode(anchorNode);
            human_fm.setBorderColor(getResources().getColor(R.color.select_ar,getApplicationContext().getTheme()));

            human_m.setBorderWidth(0);
            human_fm.setBorderWidth(15);
            giant.setBorderWidth(0);
            monkey.setBorderWidth(0);
            getPath(foldername,"human_fm");
            Log.d(TAG, "onClick: New Path : "+ASSET_3D);

        }else if(view.getId() == R.id.giant){

            removeAnchorNode(anchorNode);
            giant.setBorderColor(getResources().getColor(R.color.select_ar,getApplicationContext().getTheme()));

            human_m.setBorderWidth(0);
            human_fm.setBorderWidth(0);
            giant.setBorderWidth(15);
            monkey.setBorderWidth(0);
            getPath(foldername,"giant");
            Log.d(TAG, "onClick: New Path : "+ASSET_3D);

        }else if(view.getId() == R.id.monkey){

            removeAnchorNode(anchorNode);
            monkey.setBorderColor(getResources().getColor(R.color.select_ar,getApplicationContext().getTheme()));

            human_m.setBorderWidth(0);
            human_fm.setBorderWidth(0);
            giant.setBorderWidth(0);
            monkey.setBorderWidth(15);
            getPath(foldername,"monkey");
            Log.d(TAG, "onClick: New Path : "+ASSET_3D);

        }
    }

    private void onUpdate(FrameTime frameTime) {

        if(isModelPlace){
            return;
        }

        Frame frame = arFragment.getArSceneView().getArFrame();
        Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);
        for(Plane plane : planes){
            if(plane.getTrackingState() == TrackingState.TRACKING){
                anchor = plane.createAnchor(plane.getCenterPose());
                Log.d(TAG, "onCreate: Surface Detected.");
                Toast.makeText(getApplicationContext(),"Surface Detected",Toast.LENGTH_SHORT).show();
                placeModel(anchor);
                break;
            }
        }
    }

    private void getPath(String action, String races){
        build_url = url + "action="+action+"&"+"races="+races;
        Log.d(TAG, "getPath: Final url : "+build_url);
        RequestQueue requestQueue = Volley.newRequestQueue(ARActivity.this);
        StringRequest request = new StringRequest(Request.Method.GET,build_url, new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: "+response);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject item = jsonArray.getJSONObject(0);
                    model_url = item.getString("file_url");
                    ASSET_3D = head+model_url;
                    build_url = "";

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //placeModel(anchor);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("onError",error.toString());
                Toast.makeText(ARActivity.this,"เกิดข้อผิดพลาดโปรดลองอีกครั้ง",Toast.LENGTH_SHORT).show();
                build_url = "";
            }
        });
        requestQueue.add(request);
    }

    private void placeModel(Anchor anchor) {

        isModelPlace = true;

        Log.d(TAG, "onCreate: Place Model From "+ASSET_3D);
        Toast.makeText(getApplicationContext(),"Fetching Model From : "+foldername,Toast.LENGTH_SHORT).show();
        ModelRenderable
                .builder()
                .setSource(
                        this,
                        RenderableSource
                        .builder()
                        .setSource(this, Uri.parse(ASSET_3D),RenderableSource.SourceType.GLB)
                        .setScale(0.01f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId(ASSET_3D)
                .build()
                .thenAccept(modelRenderable -> addNodeToScene(modelRenderable,anchor))
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage()).show();
                    return null;
                });
    }

    private void addNodeToScene(ModelRenderable modelRenderable, Anchor anchor){

        anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode transformNode = new TransformableNode(arFragment.getTransformationSystem());
        transformNode.setParent(anchorNode);
        transformNode.setRenderable(modelRenderable);
        transformNode.select();

    }

    public void removeAnchorNode(AnchorNode nodeToremove) {

        if (nodeToremove != null){
            isModelPlace = false;
            Log.d(TAG, "removeAnchorNode: Remove Model Complete.");
            arFragment.getArSceneView().getScene().removeChild(nodeToremove);
            nodeToremove.getAnchor().detach();
            nodeToremove.setParent(null);
            nodeToremove = null;
        }
    }

    @Override
    public void onBackPressed() {

        if(mbottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            moreinfo.setBackgroundTintList(getResources().getColorStateList(R.color.Main_color_1,getApplicationContext().getTheme()));
        }else{
            super.onBackPressed();
        }

    }

    @Override
    public void finish(){
        super.finish();
        Log.d(TAG, "finish: Finish called");
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }
}
