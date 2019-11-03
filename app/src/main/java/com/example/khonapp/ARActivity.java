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

import java.util.Collection;

import de.hdodenhof.circleimageview.CircleImageView;


public class ARActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "loadModel";
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private boolean isModelPlace;
    private Context context;
    //private String path = "http://192.168.64.2/3D/";
    private String path = "http://mungmee.ddns.net/3D/";
    private String extension = ".glb";
    private String ASSET_3D = "";
    private String foldername = "";

    private BottomSheetBehavior mbottomSheetBehavior;
    private TextView mtextViewState;
    AnchorNode anchorNode;

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
            Toast.makeText(context, "Failed to create AR session.", Toast.LENGTH_LONG).show();
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
            ASSET_3D = path + foldername + "/" + "human_m" + extension;
            Log.d(TAG, "onCreate: Final Path is : " + ASSET_3D);
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
            ASSET_3D = path + foldername + "/" + "human_m" + extension;
            Log.d(TAG, "onClick: New Path : "+ASSET_3D);

        }else if(view.getId() == R.id.human_fm){

            removeAnchorNode(anchorNode);
            human_fm.setBorderColor(getResources().getColor(R.color.select_ar,getApplicationContext().getTheme()));

            human_m.setBorderWidth(0);
            human_fm.setBorderWidth(15);
            giant.setBorderWidth(0);
            monkey.setBorderWidth(0);
            ASSET_3D = path + foldername + "/" + "human_fm" + extension;
            Log.d(TAG, "onClick: New Path : "+ASSET_3D);

        }else if(view.getId() == R.id.giant){

            removeAnchorNode(anchorNode);
            giant.setBorderColor(getResources().getColor(R.color.select_ar,getApplicationContext().getTheme()));

            human_m.setBorderWidth(0);
            human_fm.setBorderWidth(0);
            giant.setBorderWidth(15);
            monkey.setBorderWidth(0);
            ASSET_3D = path + foldername + "/" + "giant" + extension;
            Log.d(TAG, "onClick: New Path : "+ASSET_3D);

        }else if(view.getId() == R.id.monkey){

            removeAnchorNode(anchorNode);
            monkey.setBorderColor(getResources().getColor(R.color.select_ar,getApplicationContext().getTheme()));

            human_m.setBorderWidth(0);
            human_fm.setBorderWidth(0);
            giant.setBorderWidth(0);
            monkey.setBorderWidth(15);
            ASSET_3D = path + foldername + "/" + "monkey" + extension;
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
                Anchor anchor = plane.createAnchor(plane.getCenterPose());
                Log.d(TAG, "onCreate: Surface Detected.");
                Toast.makeText(getApplicationContext(),"Surface Detected",Toast.LENGTH_SHORT).show();
                placeModel(anchor);
                break;
            }
        }
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
}
