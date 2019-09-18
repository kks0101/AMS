package com.example.ams.student;

import androidx.annotation.NonNull;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ams.others.BaseActivity;
import com.example.ams.R;
import com.example.ams.others.Upload;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentDetail extends BaseActivity {

    private CircleImageView profileImage;
    private ImageButton addProfileImage, uplaoadProfileImage;
    private ImageView uploadDp;
    private FirebaseDatabase firebaseDatabase;
    private TextView nameTextView, emailIdTextView, regNoTextView, branchTextView, phoneNoTextView, semesterTextView, groupTextView;
    String name , regNo, emailId, branch, semester, phoneNo,groupName;
    private DatabaseReference mDatabase;



    private Uri mImageUri;
    private ProgressBar mProgressBar;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;

    private final String BASE_URL = "https://amscollege.000webhostapp.com/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);
        nameTextView = (TextView)findViewById(R.id.nameTextView);
        emailIdTextView = (TextView)findViewById(R.id.emailIdTextView);
        regNoTextView = (TextView)findViewById(R.id.regNoTextView);
        branchTextView = (TextView)findViewById(R.id.branchTextView);
        phoneNoTextView = (TextView)findViewById(R.id.phoneNoTextView);
        semesterTextView = (TextView)findViewById(R.id.semesterTextView);
        groupTextView = (TextView)findViewById(R.id.groupNameTextView);
        profileImage = (CircleImageView)findViewById(R.id.profileImage);

        Button back = (Button)findViewById(R.id.back_Button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String deviceId = getIntent().getStringExtra("deviceId");
        GetStudentDetails getStudentDetails = new GetStudentDetails();
        getStudentDetails.execute(deviceId);
    }


    private class GetStudentDetails extends AsyncTask<String, String , String > {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Getting details\n..please wait..");
        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("deviceId", strings[0].trim());

            String link = BASE_URL + "get_student_details_device.php";

            Set set = params.entrySet();
            Iterator iterator = set.iterator();
            try {
                String data = "";
                while (iterator.hasNext()) {
                    Map.Entry mEntry = (Map.Entry) iterator.next();
                    data += URLEncoder.encode(mEntry.getKey().toString(), "UTF-8") + "=" +
                            URLEncoder.encode(mEntry.getValue().toString(), "UTF-8");
                    data += "&";
                }

                if (data != null && data.length() > 0 && data.charAt(data.length() - 1) == '&') {
                    data = data.substring(0, data.length() - 1);
                }
                Log.d("Debug", data);

                URL url=new URL(link);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Accept","*/*");
                OutputStream out = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                Log.d("data", data);
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());


                String result = convertStreamToString(inputStream);
                httpURLConnection.disconnect();
                Log.d("TAG",result);
                return result;
            }
            catch(Exception e){
                Log.d("debug",e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        private String convertStreamToString(InputStream inputStream) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder("");
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //if string returned from doinbackground is null, that means Exception occured while connectioon to server
            hideProgressDialog();
            if(s==null){
                Toast.makeText(StudentDetail.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
                //if could not know whether the current user is student or teacher
            }
            else {

                //otherwise string would contain the JSON returned from php
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = null;
                try {
                    jsonObject = (JSONObject) parser.parse(s);
                    Object p = jsonObject.get("success");
                    int successCode = Integer.parseInt(p.toString());

                    if( successCode==0){
                        Toast.makeText(StudentDetail.this, "Some error occurred", Toast.LENGTH_LONG).show();
                        //if could not know whether the current user is student or teacher

                    }
                    else{
                        name = jsonObject.get("name").toString();
                        regNo = jsonObject.get("regNo").toString();
                        emailId = jsonObject.get("emailId").toString();
                        branch = jsonObject.get("branch").toString();
                        semester = jsonObject.get("semester").toString();
                        phoneNo = jsonObject.get("phoneNo").toString();
                        groupName = jsonObject.get("groupName").toString();
                        String userID = jsonObject.get("userId").toString();
                        nameTextView.setText(name);
                        regNoTextView.setText(regNo);
                        emailIdTextView.setText(emailId);
                        groupTextView.setText(groupName);
                        semesterTextView.setText(semester);
                        phoneNoTextView.setText(phoneNo);
                        branchTextView.setText(branch);


                        final ProgressBar imageLoaderProgressBar = (ProgressBar)findViewById(R.id.imageProgressBar);
                        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
                        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("uploads");
                        reference.child(userID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                imageLoaderProgressBar.setVisibility(View.GONE);
                                //fetch data from Firebase database corresponding to current user
                                Upload upload = dataSnapshot.getValue(Upload.class);
                                if(upload!=null) {

                                    //  Picasso.get().load(upload.getImageUrl()).into(profileImage);
                                    Picasso.get().load(upload.getImageUrl()).placeholder(R.drawable.image_shape).error(R.drawable.ic_profile2).into(profileImage);
                                }
                                else{
                                    profileImage.setImageResource(R.drawable.ic_profile2);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }catch(ParseException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
