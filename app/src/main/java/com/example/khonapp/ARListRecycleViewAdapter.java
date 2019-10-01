package com.example.khonapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ARListRecycleViewAdapter extends RecyclerView.Adapter<ARListRecycleViewAdapter.ViewHolder>{

    private static final String TAG = "ARRecycle";

    private Context context;
    private ArrayList<String> ARName = new ArrayList<>();
    private ArrayList<String> FolderName = new ArrayList<>();

    public ARListRecycleViewAdapter(Context context, ArrayList<String> ARName, ArrayList<String> folderName) {
        this.context = context;
        this.ARName = ARName;
        this.FolderName = folderName;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder:  called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ar_list_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ARListRecycleViewAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");

        holder.text.setText(ARName.get(position));
        holder.parentLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(TAG, "onClick: clicked on "+ARName.get(position)+" in folder "+FolderName.get(position));
                Toast.makeText(context,ARName.get(position)+" from folder : "+FolderName.get(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context,ARActivity.class);
                intent.putExtra("foldername",FolderName.get(position));
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return ARName.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView text;
        CardView parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.ar_text_view);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }

}
