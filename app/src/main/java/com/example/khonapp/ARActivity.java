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
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class ARActivity extends AppCompatActivity {
    private static final String TAG = "loadModel";
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private Context context;
    //private String path = "http://192.168.64.2/3D/";
    private String path = "http://mungmee.ddns.net/3D/";
    private String extension = ".glb";
    private String ASSET_3D = "";
    private String foldername = "";
    /*private String path = "3DModel";
    private String filename_model = "lion.glb";
    private String filename_bin = "NOVELO_EARTH.bin";

    FirebaseStorage firebaseStorage;
    StorageReference storageRef;
    StorageReference ref;
    StorageReference ref_bin;

    DownloadManager downloadManager;
    */
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
            arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
                Log.d(TAG, "onCreate: placemodel init");
                placeModel(hitResult.createAnchor());
            });
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

    /*private void download() {
        storageRef = firebaseStorage.getInstance().getReference();

        ref = storageRef.child(path+"/"+"lion_model"+"/"+filename_model);
        ref_bin = storageRef.child(filename_bin);

        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "onSuccess: Path_model : "+uri);
                String url = uri.toString();
                downloadFiles(ARActivity.this,filename_model,".glb",DIRECTORY_DOWNLOADS,url);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void downloadFiles(Context context, String filename, String fileextension, String destinationDirectory, String url){
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context,destinationDirectory,filename);
        downloadManager.enqueue(request);

        File file = new File(getExternalFilesDir(destinationDirectory).getPath());
        ASSET_3D = file.toString()+"/"+filename_model;
        Log.d(TAG, "onReceive: Success PATH : " + ASSET_3D);
    }*/

    private void placeModel(Anchor anchor) {
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
