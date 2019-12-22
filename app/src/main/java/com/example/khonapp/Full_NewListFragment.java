package com.example.khonapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Full_NewListFragment extends Fragment {
    private static final String TAG = "FN";
    private static final String URL = "http://192.168.64.2/3D/news.php";

    private ArrayList<String> mName = new ArrayList<>();
    private ArrayList<String> mImageURL = new ArrayList<>();
    private ArrayList<String> mDescription = new ArrayList<>();

    LinearLayoutManager layoutManager;
    RecyclerView recyclerView;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: CreateView called");
        final View view = inflater.inflate(R.layout.fragment_full_new, container, false);
        context = view.getContext();

        recyclerView = view.findViewById(R.id.news_fullList_view);

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: Initial data");
        initdata();
        super.onStart();
    }

    private void initdata() {

        if (!mImageURL.isEmpty() || !mName.isEmpty() || !mDescription.isEmpty()) {
            return;
        }

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "onResponse: JSON respond : " + response);
                        for (int i = 0; i < response.length(); i++) {                    // Parsing json
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                String title = obj.getString("title");
                                String description = obj.getString("description");
                                String image_url = obj.getString("img_url");
                                Log.d(TAG, "onResponse: Title : " + title + " Image url : " + image_url);

                                mName.add(title);
                                mDescription.add(description);
                                mImageURL.add(image_url);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        initrecycleView();
                        //scrollable();
                        //autoScrolltoLeft();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: Error appear");
                Toast.makeText(context, "Please check your internet connection or go to contact us.", Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    private void initrecycleView() {
        layoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        Full_NewViewAdapter adapter = new Full_NewViewAdapter(mName, mImageURL, mDescription, context);
        recyclerView.setAdapter(adapter);
    }
}
