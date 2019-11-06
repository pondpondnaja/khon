package com.example.khonapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import static android.app.Activity.RESULT_OK;

public class CameraFragment extends Fragment{
    private static final String TAG = "cameraActivity";
    private static final int INPUT_SIZE = 224;
    private CameraView cameraView;
    private ImageButton cap_btn,gall_btn;
    private Bitmap bitmap;
    private int RESULT_LOAD_IMG = 10;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_camera,container,false);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        cap_btn = rootView.findViewById(R.id.detection);
        gall_btn = rootView.findViewById(R.id.gallery_btn);

        cameraView = rootView.findViewById(R.id.main_camera);
        cameraView.addCameraKitListener(new CameraKitEventListener() {

            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,INPUT_SIZE,INPUT_SIZE,false);
                Log.d(TAG, "onImage: Byte of image "+bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }

        });

        cap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.captureImage();
            }
        });

        gall_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i,RESULT_LOAD_IMG);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onPause: camera start");
        cameraView.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onPause: camera resume");
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cameraView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: camera pause");
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cameraView.stop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Log.d(TAG, "onDestroy: Camera has been destroyed.");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data){
            Log.d(TAG, "onActivityResult: ImageSelected");
            Toast.makeText(getActivity().getApplicationContext(), "ImageSelected", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(),ResultActivity.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        }
    }
}
