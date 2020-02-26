package com.example.khonapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ARActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "loadModel";
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private boolean isModelPlace;

    private String url = "http://192.168.64.2/3D/ar_path.php?";
    //private String url = "https://utg-fansub.me/3D/ar_path.php?";
    private String ASSET_3D = "";
    private String FolderName = "";
    private String model_url = "";
    private String description = "";

    private BottomSheetBehavior mBottomSheetBehavior;
    private TextView mTextViewState, mTextInfo;
    AnchorNode anchorNode;
    Anchor anchor;

    CircleImageView human_m, human_fm, giant, monkey;
    Button moreInfo;
    CoordinatorLayout parentView;
    View[] arrayView;

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {

        String openGlVersionString = ((ActivityManager) Objects
                .requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE)))
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

        parentView = findViewById(R.id.parentView);
        human_m = findViewById(R.id.human_m);
        human_fm = findViewById(R.id.human_fm);
        giant = findViewById(R.id.giant);
        monkey = findViewById(R.id.monkey);
        moreInfo = findViewById(R.id.more);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mTextViewState = findViewById(R.id.bottom_text);
        mTextInfo = findViewById(R.id.text_info);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(ARActivity.this, "Failed to create AR session.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ARActivity.this, MainActivity.class));
            finish();
        } else {
            if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();
                if (extras == null) {
                    FolderName = null;
                } else {
                    FolderName = extras.getString("FolderName");
                    Log.d(TAG, "onCreate: FolderName : " + FolderName);
                }
            } else {
                FolderName = (String) savedInstanceState.getSerializable("FolderName");
            }

            //Build Path
            getPath(FolderName, "human_m");
            arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment_model);

            //init model border
            human_m.setBorderColor(getResources().getColor(R.color.select_ar, getApplicationContext().getTheme()));
            human_m.setBorderWidth(15);

            setArrayView();
            setClickListener();

            //Tap to place model Method.
            /*arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
                Log.d(TAG, "onCreate: PlaceModel init");
                placeModel(hitResult.createAnchor());
            });*/

            //Auto detection surface Method.
            arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);
        }

        moreInfo.setOnClickListener(view -> {

            if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {

                moreInfo.setBackgroundTintList(getResources().getColorStateList(R.color.Main_color_2, getApplicationContext().getTheme()));
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                switch (FolderName) {
                    case "Am":
                        mTextViewState.setText("ท่าฉัน");
                        mTextInfo.setText(description);
                        break;

                    case "Angry":
                        mTextViewState.setText("ท่าโกรธ");
                        mTextInfo.setText(description);
                        break;

                    case "Cry":
                        mTextViewState.setText("ท่าร้องไห้");
                        mTextInfo.setText(description);
                        break;

                    case "Shy":
                        mTextViewState.setText("ท่าเขิน");
                        mTextInfo.setText(description);
                        break;

                    case "Smile":
                        mTextViewState.setText("ท่ายิ้ม");
                        mTextInfo.setText(description);
                        break;
                }

            } else {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                moreInfo.setBackgroundTintList(getResources().getColorStateList(R.color.Main_color_1, getApplicationContext().getTheme()));
            }
        });
    }

    private void setArrayView() {
        arrayView = new View[]{
                human_m, human_fm, giant, monkey
        };
    }

    private void setClickListener() {
        for (View view : arrayView) {
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.human_m) {

            removeAnchorNode(anchorNode);
            human_m.setBorderColor(getResources().getColor(R.color.select_ar, getApplicationContext().getTheme()));

            human_m.setBorderWidth(15);
            human_fm.setBorderWidth(0);
            giant.setBorderWidth(0);
            monkey.setBorderWidth(0);
            getPath(FolderName, "human_m");
            Log.d(TAG, "onClick: New Path : " + ASSET_3D);

        } else if (view.getId() == R.id.human_fm) {

            removeAnchorNode(anchorNode);
            human_fm.setBorderColor(getResources().getColor(R.color.select_ar, getApplicationContext().getTheme()));

            human_m.setBorderWidth(0);
            human_fm.setBorderWidth(15);
            giant.setBorderWidth(0);
            monkey.setBorderWidth(0);
            getPath(FolderName, "human_fm");
            Log.d(TAG, "onClick: New Path : " + ASSET_3D);

        } else if (view.getId() == R.id.giant) {

            removeAnchorNode(anchorNode);
            giant.setBorderColor(getResources().getColor(R.color.select_ar, getApplicationContext().getTheme()));

            human_m.setBorderWidth(0);
            human_fm.setBorderWidth(0);
            giant.setBorderWidth(15);
            monkey.setBorderWidth(0);
            getPath(FolderName, "giant");
            Log.d(TAG, "onClick: New Path : " + ASSET_3D);

        } else if (view.getId() == R.id.monkey) {

            removeAnchorNode(anchorNode);
            monkey.setBorderColor(getResources().getColor(R.color.select_ar, getApplicationContext().getTheme()));

            human_m.setBorderWidth(0);
            human_fm.setBorderWidth(0);
            giant.setBorderWidth(0);
            monkey.setBorderWidth(15);
            getPath(FolderName, "monkey");
            Log.d(TAG, "onClick: New Path : " + ASSET_3D);

        }
    }

    private void onUpdate(FrameTime FrameTime) {

        if (isModelPlace || ASSET_3D.equals("")) {
            return;
        }

        Frame frame = arFragment.getArSceneView().getArFrame();
        assert frame != null;
        Collection<Plane> planes = frame.getUpdatedTrackables(Plane.class);
        for (Plane plane : planes) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                anchor = plane.createAnchor(plane.getCenterPose());
                Log.d(TAG, "onCreate: Surface Detected.");
                Toast.makeText(getApplicationContext(), "Surface Detected", Toast.LENGTH_SHORT).show();
                placeModel(anchor);
                break;
            }
        }
    }

    private void getPath(String action, String races) {

        String build_url = url + "action=" + action + "&" + "races=" + races;
        Log.d(TAG, "getPath: Final url : " + build_url);
        RequestQueue requestQueue = Volley.newRequestQueue(ARActivity.this);
        StringRequest request = new StringRequest(Request.Method.GET, build_url,
                response -> {
                    Log.d(TAG, "onResponse: " + response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject item = jsonArray.getJSONObject(0);
                        model_url = item.getString("file_url");
                        description = item.getString("description");
                        ASSET_3D = model_url.replace("http://", "https://");
                        Log.d(TAG, "onResponse: Path from respond : " + ASSET_3D);
                        Log.d(TAG, "getPath: Description : " + description);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.d("onError", error.toString());
            Toast.makeText(ARActivity.this, "เกิดข้อผิดพลาดโปรดลองอีกครั้ง", Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(request);
    }

    private void placeModel(Anchor anchor) {

        isModelPlace = true;

        Log.d(TAG, "onCreate: Place Model From " + ASSET_3D);
        Toast.makeText(getApplicationContext(), "Fetching Model From : " + FolderName, Toast.LENGTH_SHORT).show();
        ModelRenderable
                .builder()
                .setSource(ARActivity.this, RenderableSource
                        .builder()
                        .setSource(ARActivity.this, Uri.parse(ASSET_3D), RenderableSource.SourceType.GLB)
                        .setScale(0.01f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build()
                ).setRegistryId(ASSET_3D)
                .build()
                .thenAccept(modelRenderAble -> addNodeToScene(modelRenderAble, anchor))
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage()).show();
                    return null;
                });
    }

    private void addNodeToScene(ModelRenderable modelRenderable, Anchor anchor) {
        anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode transformNode = new TransformableNode(arFragment.getTransformationSystem());
        transformNode.setParent(anchorNode);
        transformNode.setRenderable(modelRenderable);
        transformNode.select();

    }

    public void removeAnchorNode(AnchorNode nodeToRemove) {

        if (nodeToRemove != null) {
            isModelPlace = false;
            ASSET_3D = "";
            Log.d(TAG, "removeAnchorNode: Remove Model and reset ASSET url Complete.");
            arFragment.getArSceneView().getScene().removeChild(nodeToRemove);
            Objects.requireNonNull(nodeToRemove.getAnchor()).detach();
            nodeToRemove.setParent(null);
        }
    }

    @Override
    public void onBackPressed() {

        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            moreInfo.setBackgroundTintList(getResources().getColorStateList(R.color.Main_color_1, getApplicationContext().getTheme()));
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void finish() {
        super.finish();
        Log.d(TAG, "finish: Finish called");
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
