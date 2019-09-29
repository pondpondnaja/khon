package com.example.khonapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

public class CameraFragment extends Fragment{
    private static final String TAG = "camera";
    private static final int INPUT_SIZE = 224;
    private CameraView cameraView;
    private Button cap_btn;
    private Button gall_btn;
    private Bitmap bitmap;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_camera,container,false);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        cap_btn = rootView.findViewById(R.id.detection);
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

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        cameraView.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
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
}
