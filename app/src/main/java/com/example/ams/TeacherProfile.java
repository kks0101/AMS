package com.example.ams;

import androidx.annotation.NonNull;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
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

/**
 * This is the activity that handles and configures Teacher Profile display.
 * Retrieves teacher credentials from php server and display it.
 */

public class TeacherProfile extends BaseActivity {
    private FirebaseAuth mAuth;
    private TextView nameTextView, emailIdTextView, teacherIdTextView, verifiedTextView, phoneNoTextView;

    private final String BASE_URL = "https://amscollege.000webhostapp.com/";
    //private final String BASE_URL = "http://192.168.43.99:1234/ams/";

    private Button logoutButton;
    private Uri mImageUri;
    private ProgressBar mProgressBar;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mDatabase;
    private StorageTask mUploadTask;
    private ImageView uploadDpTeacaher;
    private FirebaseDatabase firebaseDatabase;
    private CircleImageView profileImage;
    private int PICK_IMAGE_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);

        mAuth = FirebaseAuth.getInstance();
        nameTextView = (TextView)findViewById(R.id.nameTextView);
        emailIdTextView = (TextView)findViewById(R.id.emailIdTextView);
        teacherIdTextView = (TextView)findViewById(R.id.teacherIdTextView);
        verifiedTextView = (TextView)findViewById(R.id.verifiedTextView);
        phoneNoTextView = (TextView)findViewById(R.id.phoneNoTextView);
        logoutButton = (Button) findViewById(R.id.logoutTeacher);
        //profileImage= (ImageView) findViewById(R.id.profileImage);

        profileImage = (CircleImageView) findViewById(R.id.profileImage);
        uploadDpTeacaher = (ImageView) findViewById(R.id.uploadDpTeacher);
        mProgressBar = findViewById(R.id.progress_bar);
        final ProgressBar imageLoaderProgressBar = (ProgressBar)findViewById(R.id.imageProgressBar);
        imageLoaderProgressBar.setVisibility(View.VISIBLE);
        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference("profileImages");

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        uploadDpTeacaher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(TeacherProfile.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    openFileChooser();

                }
            }
        });
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("uploads");
        reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageLoaderProgressBar.setVisibility(View.GONE);
                //fetch data from Firebase database corresponding to current user
                Upload upload = dataSnapshot.getValue(Upload.class);
                if(upload!=null) {

                    //Picasso.get().load(upload.getImageUrl()).into(profileImage);
                    //Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_LONG).show();
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

        GetProfileDetails getProfileDetails = new GetProfileDetails();
        getProfileDetails.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("details", 0); //Mode_private
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("user", null);
                editor.apply();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(TeacherProfile.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            uploadFile();
            Picasso.get().load(mImageUri).into(profileImage);
        }
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void uploadFile() {
        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 500);

                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUrl = uri;
                                    Upload upload = new Upload("photo", uri.toString());
                                    String uploadId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    mDatabaseRef.child(uploadId).setValue(upload);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(TeacherProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private class GetProfileDetails extends AsyncTask<String, String , String > {
        String userId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Retrieving details\n..please wait..");
            userId = mAuth.getCurrentUser().getUid();
        }


        @Override
        protected String doInBackground(String... strings) {

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("userId", userId);

            String link = BASE_URL + "get_teacher_details.php";

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
                Toast.makeText(TeacherProfile.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(TeacherProfile.this, "Some error occurred", Toast.LENGTH_LONG).show();
                        //if could not know whether the current user is student or teacher

                    }
                    else{
                        String name = jsonObject.get("name").toString();
                        String teacherId = jsonObject.get("teacherId").toString();
                        String emailId = jsonObject.get("emailId").toString();
                        String phoneNo = jsonObject.get("phoneNo").toString();
                        String verified = jsonObject.get("verified").toString();

                        nameTextView.setText(name);
                        teacherIdTextView.setText(teacherId);
                        emailIdTextView.setText(emailId);
                        if(Integer.parseInt(verified) == 0) {
                            verifiedTextView.setText("You are not verified!");
                            verifiedTextView.setTextColor(Color.RED);
                        }
                        else{
                            verifiedTextView.setText("You are verified. :)");
                            verifiedTextView.setTextColor(Color.GREEN);
                        }
                        phoneNoTextView.setText(phoneNo);
                    }
                }catch(ParseException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
