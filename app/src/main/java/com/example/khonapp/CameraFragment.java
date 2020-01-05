package com.example.khonapp;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Mode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class CameraFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "cameraActivity";
    private static final String URL = "http://192.168.1.41:5000/connectFromAndroid";
    private static final int INPUT_SIZE = 224;
    private static final int RESULT_LOAD_IMG = 10;

    private CameraView cameraView;
    private ImageView cap_btn, gall_btn, progress_back;
    private Bitmap bitmap;
    private String selectedImagePath;
    private ProgressBar progressBar;

    private Bundle bundle;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        context = rootView.getContext();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AppCompatActivity canera_activity = (AppCompatActivity) context;
        canera_activity.getSupportActionBar().hide();

        cap_btn = rootView.findViewById(R.id.detection);
        gall_btn = rootView.findViewById(R.id.gallery_btn);
        progressBar = rootView.findViewById(R.id.progressBar);
        progress_back = rootView.findViewById(R.id.img_overlay);

        cameraView = rootView.findViewById(R.id.camera);
        cameraView.setLifecycleOwner(getViewLifecycleOwner());
        cameraView.addCameraListener(new Listener());
        cameraView.setPlaySounds(false);

        cap_btn.setOnClickListener(this);
        gall_btn.setOnClickListener(this);
        /*cap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Called");
                cameraView.takePicture();
                cameraView.addCameraListener(new CameraListener() {
                    @Override
                    public void onPictureTaken(@NonNull PictureResult result) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                        String currentDateAndTime = sdf.format(new Date());
                        File savedPhoto = new File(Environment.DIRECTORY_PICTURES, currentDateAndTime+".jpg");
                        byte[] capturedImage = result.getData();
                        try {
                            FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                            outputStream.write(capturedImage);
                            outputStream.close();
                            Log.d(TAG, "onImage: PATH : "+savedPhoto.getPath());
                            selectedImagePath = savedPhoto.getPath();
                            connectServer();

                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onVideoTaken(@NonNull VideoResult result) {
                        super.onVideoTaken(result);
                    }
                });
            }
        });*/

        //bitmap = cameraKitImage.getBitmap();
        //bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: camera start");
        cameraView.open();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cameraView.open();
        Log.d(TAG, "onResume: camera resume called");
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.close();
        Log.d(TAG, "onPause: camera pause");
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.detection:
                capturePicture();
                break;
            case R.id.gallery_btn:
                selectImage();
                break;
        }
    }

    private class Listener extends CameraListener {

        @Override
        public void onPictureTaken(@NonNull PictureResult result) {
            super.onPictureTaken(result);
            // This can happen if picture was taken with a gesture.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String currentDateAndTime = sdf.format(new Date());
            File savedPhotoFolder = new File(Environment.getExternalStorageDirectory(), "Download");
            File imageFile = new File(savedPhotoFolder, currentDateAndTime + ".jpeg");
            byte[] capturedImage = result.getData();
            try {
                FileOutputStream outputStream = new FileOutputStream(imageFile.getPath());
                outputStream.write(capturedImage);
                outputStream.close();
                Log.d(TAG, "onImage: PATH : " + imageFile.getPath());
                selectedImagePath = imageFile.getPath();
                previewImage();
            } catch (java.io.IOException e) {
                Log.d(TAG, "onPictureTaken: Error : " + e.toString());
            }
        }

        @Override
        public void onZoomChanged(float newValue, @NonNull float[] bounds, @Nullable PointF[] fingers) {
            super.onZoomChanged(newValue, bounds, fingers);
            Toast.makeText(getContext(), "Zoom:" + newValue, Toast.LENGTH_SHORT).show();
        }
    }

    private void capturePicture() {
        if (cameraView.getMode() == Mode.VIDEO) {
            Toast.makeText(getContext(), "Can't take HQ pictures while in VIDEO mode.", Toast.LENGTH_SHORT).show();
            return;
        }
        cameraView.takePicture();
    }

    private void selectImage() {
        Toast.makeText(getContext(), "Opening gallery", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "selectImage: Selected called");
        Intent intent = new Intent();
        //intent.setType("*/*");
        /*intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, RESULT_LOAD_IMG);*/
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMG);
        onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
            Log.d(TAG, "onActivityResult: ImageSelected");
            Toast.makeText(getContext(), "ImageSelected", Toast.LENGTH_LONG).show();
            Uri uri = data.getData();

            selectedImagePath = getPath(getContext(), uri);
            //imgPath.setText(selectedImagePath);
            Log.d(TAG, "onActivityResult: IMG_Path : " + selectedImagePath);
            Toast.makeText(getContext(), selectedImagePath, Toast.LENGTH_LONG).show();
            previewImage();
        }
    }

    private void previewImage() {
        progressBar.setVisibility(View.VISIBLE);
        progress_back.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                progress_back.setVisibility(View.GONE);

                bundle = new Bundle();
                bundle.putString("img_path", selectedImagePath);
                CameraResultFragment cameraResultFragment = new CameraResultFragment();
                cameraResultFragment.setArguments(bundle);

                AppCompatActivity activity = (AppCompatActivity) context;
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
                        .add(R.id.fragment_container, cameraResultFragment, "detect_result").addToBackStack("detect_result").commit();
            }
        }, 2000);
    }

    public void connectServer() {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // Read BitMap by file path
        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .addFormDataPart("algo", "0")
                .build();

        postRequest(URL, postBodyImage);
    }

    public void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();
        AppCompatActivity appCompatActivity = new AppCompatActivity();
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                appCompatActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            Toast.makeText(getContext(), "Fail to connect server", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                appCompatActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            Toast.makeText(getContext(), "Result = " + response.body().string(), Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // Implementation of the getPath() method and all its requirements is taken from the StackOverflow Paul Burke's answer: https://stackoverflow.com/a/20559175/5426539
    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    Log.d(TAG, "getPath: Primary : " + Environment.getExternalStorageDirectory() + "/" + split[1]);
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                Log.d(TAG, "getPath: isDownloadsDocument(uri) : " + getDataColumn(context, contentUri, null, null));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
