package com.example.khonapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.Objects;

public class CameraResultFragment extends Fragment {
    private static final String TAG = "cameraResult";
    private String img_path;

    private TextView mTItle, mDescription;
    private ImageView mImage;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_cameraresult, container, false);
        context = view.getContext();

        Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mImage = view.findViewById(R.id.preview_img);
        mTItle = view.findViewById(R.id.result_title);
        mDescription = view.findViewById(R.id.result_description);

        Bundle bundle = getArguments();
        if (bundle != null) {
            img_path = bundle.getString("img_path");
        } else {
            img_path = null;
        }

        setData();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getActivity()).getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppCompatActivity camera_activity = (AppCompatActivity) context;
        Objects.requireNonNull(camera_activity.getSupportActionBar()).hide();
        Log.d(TAG, "onDestroy: Called");
    }

    private void setData() {
        File imgFile = new File(img_path);

        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            try {
                ExifInterface exif = new ExifInterface(imgFile.getAbsolutePath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.d(TAG, "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // rotating bitmap
                mImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.d(TAG, "setData: Error : " + e.toString());
            }
        } else {
            Toast.makeText(getContext(), "Error : image doesn't exists", Toast.LENGTH_LONG).show();
        }
        /*if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            mImage.setImageBitmap(myBitmap);
        }else{
            Toast.makeText(getContext(),"Error : image doesn't exists",Toast.LENGTH_LONG).show();
        }*/
    }
}
