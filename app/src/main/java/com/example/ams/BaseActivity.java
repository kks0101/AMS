package com.example.ams;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import dmax.dialog.SpotsDialog;

public class BaseActivity extends AppCompatActivity {
    public ProgressDialog progressDialog;

    public void showProgressDialog( String message){
        if(progressDialog==null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.setIndeterminate(true);

        }
        progressDialog.show();
    }

    public void hideProgressDialog(){
        if(progressDialog!=null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgressDialog();
    }
}
