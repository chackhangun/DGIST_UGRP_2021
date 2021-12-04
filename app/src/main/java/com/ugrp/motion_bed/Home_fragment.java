package com.ugrp.motion_bed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


public class Home_fragment extends Fragment {
    //private static final int GET_GALLERY_IMAGE = ;
    TextView status_box;
    public int bt_check = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home_fragment, container, false);
        status_box = (TextView) view.findViewById(R.id.status_box);
        change_status(bt_check);
        return view;
    }
    /*
    public void baby(View view){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, GET_GALLERY_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            baby_image_view.setImageURI(selectedImageUri);
        }
    }*/

    public void change_status(int bt_check){
        if(bt_check == 1){
            status_box.setText("SAFE");
        }
        else{
            status_box.setText("NEED CONNECTION");
        }
    }
}

