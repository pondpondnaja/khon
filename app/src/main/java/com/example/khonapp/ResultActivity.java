package com.example.khonapp;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1000;
    private static final String TAG = "cameraResult";
    private static final String URL = "http://192.168.1.43:5000/connectFromAndroid";
    private String img_path, img_real_path, previewPath;

    private TextView mTItle, mDescription;
    private ImageView mImage;
    private ProgressBar progressBar;

    Uri img_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }

        progressBar = findViewById(R.id.progressBar);
        mImage = findViewById(R.id.preview_img);

        if (savedInstanceState == null) {
            Bundle extra = getIntent().getExtras();
            if (extra == null) {
                img_path = null;
            } else {
                img_path = extra.getString("img_path");
                img_address = Uri.parse(img_path);
                img_real_path = getPath(ResultActivity.this, img_address);
                progressBar.setVisibility(View.GONE);
                setData();

                //connectServer();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void connectServer() {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // Read BitMap by file path
        Bitmap bitmap = BitmapFactory.decodeFile(img_real_path, options);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        postRequest(URL, postBodyImage);
    }

    private void postRequest(String postUrl, RequestBody postBody) {

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
                appCompatActivity.runOnUiThread(() -> {
                    try {
                        Toast.makeText(getApplicationContext(), "Fail to connect server", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                appCompatActivity.runOnUiThread(() -> {
                    try {
                        assert response.body() != null;
                        Toast.makeText(getApplicationContext(), "Result = " + response.body().string(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "onResponse: PATH : " + img_address);
                        setData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    public void setData() {
        Log.d(TAG, "setPreviewImage: Image Path : " + img_real_path);
        Bitmap bMap = BitmapFactory.decodeFile(img_real_path);
        Log.d(TAG, "setData: Orientation : " + getOrientation(img_address));
        String orientation = getOrientation(img_address);
        if (orientation.equals("landscape")) {
            FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            mImage.setLayoutParams(imageViewParams);
        } else if (orientation.equals("portrait")) {
            FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(1000, 1500);
            mImage.setLayoutParams(imageViewParams);
        }
        mImage.setImageURI(Uri.parse(img_real_path));
    }

    private String getOrientation(Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        String orientation = "landscape";
        try {
            BitmapFactory.decodeFile(img_real_path, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            Log.d(TAG, "getOrientation: H : " + imageHeight + " W : " + imageWidth);
            if (imageHeight > imageWidth) {
                orientation = "portrait";
            }
        } catch (Exception e) {
            //Do nothing
        }
        return orientation;
    }

    private static String getPath(final Context context, final Uri uri) {
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

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        final String column = "_data";
        final String[] projection = {column};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this method is called, when user presses Allow or Deny from Permission Request Popup
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup was granted
                    setData();
                } else {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
