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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.Collection;

public class ARActivity extends AppCompatActivity {
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

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mtextViewState = findViewById(R.id.bottom_text);
        mbottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            Intent goback = new Intent(context,MainActivity.class);
            startActivity(goback);
        }else {
            if (savedInstanceState == null){
                Bundle extras = getIntent().getExtras();
                if (extras == null) {
                    foldername = null;
                }else{
                    foldername = extras.getString("foldername");
                    Log.d(TAG, "onCreate: extra : " + foldername);
                }
            }else{
                foldername = (String) savedInstanceState.getSerializable("foldername");
            }
            //Build Path
            ASSET_3D = path + foldername + "/" + foldername + extension;
            Log.d(TAG, "onCreate: Final Path : " + ASSET_3D);

            //download();

            arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arfragment_model);
            //Tap to place model.
            /*arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
                Log.d(TAG, "onCreate: placemodel init");
                placeModel(hitResult.createAnchor());
            });*/
            //Auto detection surface.
            arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);
        }

        mbottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View view, int newState) {
                switch (newState){
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        mtextViewState.setText(foldername);
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        mtextViewState.setText(foldername);
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        mtextViewState.setText(foldername);
                        break;

                    case BottomSheetBehavior.STATE_HIDDEN:
                        mtextViewState.setText(foldername);
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        mtextViewState.setText(foldername);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                mtextViewState.setText(foldername);
            }
        });
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
                Toast.makeText(getApplicationContext(),"Surface Detected",Toast.LENGTH_SHORT).show();
                placeModel(anchor);
                break;
            }
        }
    }


    private void placeModel(Anchor anchor) {
        isModelPlace = true;

        Log.d(TAG, "onCreate: Place Model From "+ASSET_3D);
        Toast.makeText(getApplicationContext(),"Fetching Model : "+foldername,Toast.LENGTH_SHORT).show();
        ModelRenderable
                .builder()
                .setSource(
                        this,
                        RenderableSource
                        .builder()
                        .setSource(this, Uri.parse(ASSET_3D), RenderableSource.SourceType.GLB)
                        .setScale(0.03f)
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

    private void addNodeToScene(ModelRenderable modelRenderable, Anchor anchor) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
    }
}
