package com.example.ams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class StudentProfile extends BaseActivity {
    FirebaseAuth mAuth;
    private static int QRCodeWidth = 500;
    private final String BASE_URL = "http://192.168.43.99:1234/ams/";

    private static int request_code = 20;

    private int PICK_IMAGE_REQUEST = 1;
    private Bitmap bitmapUpload;
    private Bitmap bitmap;
    private StorageReference mStorage;
    private Uri filePath;
    private Button showQr, logOut;
    private ImageButton addProfileImage, uplaoadProfileImage;
    private ImageView profileImage;
    private FirebaseDatabase firebaseDatabase;
    private TextView nameTextView, emailIdTextView, regNoTextView, branchTextView, phoneNoTextView, semesterTextView, groupTextView;
    String name , regNo, emailId, branch, semester, phoneNo,groupName;
    private DatabaseReference mDatabase;



    private Uri mImageUri;
    private ProgressBar mProgressBar;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);
        mAuth = FirebaseAuth.getInstance();

        nameTextView = (TextView)findViewById(R.id.nameTextView);
        emailIdTextView = (TextView)findViewById(R.id.emailIdTextView);
        regNoTextView = (TextView)findViewById(R.id.regNoTextView);
        branchTextView = (TextView)findViewById(R.id.branchTextView);
        phoneNoTextView = (TextView)findViewById(R.id.phoneNoTextView);
        semesterTextView = (TextView)findViewById(R.id.semesterTextView);
        groupTextView = (TextView)findViewById(R.id.groupNameTextView);
        showQr = (Button)findViewById(R.id.showQR);
        logOut = (Button)findViewById(R.id.logoutStudent);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = firebaseDatabase.getReference("profileImages");


        profileImage = (ImageView) findViewById(R.id.profileImage);
        mProgressBar = findViewById(R.id.progress_bar);
        final ProgressBar imageLoaderProgressBar = (ProgressBar)findViewById(R.id.imageProgressBar);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(StudentProfile.this, "Upload in progress", Toast.LENGTH_SHORT).show();
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

                    Picasso.get().load(upload.getImageUrl()).into(profileImage);
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
                            Toast.makeText(StudentProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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



    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission( StudentProfile.this, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED ){
            requestPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

                    Toast.makeText(getApplicationContext(),"You are not online!!!!",Toast.LENGTH_LONG).show();
                }else {
                    if (ContextCompat.checkSelfPermission(StudentProfile.this,
                            Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "Permission Needed to read Phone state", Toast.LENGTH_LONG).show();
                    } else {
                        GenerateQr generateQr = new GenerateQr();
                        generateQr.execute();
                    }
                }
            }
        });

        logOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!AppStatus.getInstance(getApplicationContext()).isOnline()) {

                    Toast.makeText(getApplicationContext(),"You are not online!!!!",Toast.LENGTH_LONG).show();
                }else {
                    FirebaseAuth.getInstance().signOut();
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("details", 0); //Mode_private
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("user", null);
                    editor.commit();
                    Intent intent = new Intent(StudentProfile.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void requestPermission(){
        if (ContextCompat.checkSelfPermission( StudentProfile.this, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(StudentProfile.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    1);

        }
    }

    private class GetProfileDetails extends AsyncTask<String, String , String > {
        String userId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Getting details\n..please wait..");
            userId = mAuth.getCurrentUser().getUid();
        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("userId", userId);

            String link = BASE_URL + "get_student_details.php";

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
                Toast.makeText(StudentProfile.this, "Coudlnt connect to PHPServer", Toast.LENGTH_LONG).show();
                //if could not know whether the current user is student or teacher
            }
            else {

                //otherwise string would contain the JSON returned from php
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = null;
                try {
                    jsonObject = (JSONObject) parser.parse(s);
                }catch(ParseException e){
                    e.printStackTrace();
                }
                int successCode = 0;
                if(jsonObject!=null) {
                    Object p = jsonObject.get("success");
                    successCode = Integer.parseInt(p.toString());
                }
                if( jsonObject!=null && successCode==0){
                    Toast.makeText(StudentProfile.this, "Some error occurred", Toast.LENGTH_LONG).show();
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

                    nameTextView.setText(name);
                    regNoTextView.setText(regNo);
                    emailIdTextView.setText(emailId);
                    groupTextView.setText(groupName);
                    semesterTextView.setText(semester);
                    phoneNoTextView.setText(phoneNo);
                    branchTextView.setText(branch);
                }
            }
        }
    }

    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRCodeWidth, QRCodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_8888);

        bitmap.setPixels(pixels, 0, QRCodeWidth, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    private class GenerateQr extends AsyncTask<String, String, String>{
        String deviceId;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Generating QR code..Please Wait");
            if (ContextCompat.checkSelfPermission(StudentProfile.this,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
            }
            else{
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                try {
                    //uniquely identifies phone
                    deviceId = telephonyManager.getDeviceId();
                }
                catch(SecurityException e){
                    Log.i("PERMISSION", e.toString());
                }
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            org.json.JSONObject credentials = new org.json.JSONObject();


            try{
                credentials.put("deviceId", deviceId);
            }catch (JSONException e){
                e.printStackTrace();
            }
            String value = credentials.toString();
            try {
                bitmap = TextToImageEncode(value);

            } catch (WriterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            hideProgressDialog();
            final Dialog dialog = new Dialog(StudentProfile.this);
            dialog.setContentView(R.layout.layout_qr_display);
            ImageView showQrImageView = (ImageView)dialog.findViewById(R.id.showQrImageView);
            showQrImageView.setImageBitmap(bitmap);
            dialog.setTitle("Details :");

            if(!isFinishing())
                dialog.show();
        }
    }
}
