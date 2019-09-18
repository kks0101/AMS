package com.example.ams;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * This Activity handles Teacher registration. It uses Firebase Authentication along with Php server
 * verification to create teacher account.
 */


public class TeacherRegister extends BaseActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private EditText nameField, teacherIdField, emailField, phnNoField, passwordField, retypePasswordField;
    private Button registerButton;
    private FirebaseAuth mAuth;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private final String BASE_URL = "https://amscollege.000webhostapp.com/";
    //private final String BASE_URL = "http://192.168.43.99:1234/ams/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_register);

        ///check for permission
        requestPermission();

        nameField = (EditText) findViewById(R.id.teacherName);
        teacherIdField = (EditText) findViewById(R.id.teacherId);
        emailField = (EditText) findViewById(R.id.teacherEmail);
        phnNoField = (EditText) findViewById(R.id.teacherPhone);
        passwordField = (EditText) findViewById(R.id.password);
        retypePasswordField = (EditText) findViewById(R.id.retypePassword);
        registerButton = (Button) findViewById(R.id.teacherRegisterButton);

        registerButton.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

    }

    /**
     * uses Firebase email and password authentication methods to handle teacher registration.
     * Once successfully Authenticated, updates the data on the php server.
     * @param email
     * @param password
     */
    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }
        showProgressDialog("Registering..Please wait");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            CreateNewTeacher createNewTeacher = new CreateNewTeacher();
                            createNewTeacher.execute();
                        } else {
                            Toast.makeText(TeacherRegister.this, "Account Authentication failed!!", Toast.LENGTH_LONG).show();
                            Log.e("Error Registering", task.getException().toString());

                        }
                        hideProgressDialog();
                    }
                });
    }

    /**
     * This method checks for the required permission. We require user permission to read
     * the phone state of the user. This is required to access phone details using Telephony Manager.
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(TeacherRegister.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(TeacherRegister.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(TeacherRegister.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(TeacherRegister.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        }
    }

    private boolean validateForm() {
        boolean valid = true;
        String name = nameField.getText().toString();
        String id = teacherIdField.getText().toString();
        String email = emailField.getText().toString();
        String phnNo = phnNoField.getText().toString();
        String password = passwordField.getText().toString();
        String rePassword = retypePasswordField.getText().toString();

        if (TextUtils.isEmpty(name)) {
            nameField.setError("Required!");
            valid = false;
        } else if (TextUtils.isEmpty(id)) {
            teacherIdField.setError("Required!");
            valid = false;
        } else if (TextUtils.isEmpty(email)) {
            emailField.setError("Required!");
            valid = false;
        } else if (TextUtils.isEmpty(phnNo)) {
            phnNoField.setError("Required!");
            valid = false;
        } else if (TextUtils.isEmpty(password)) {
            passwordField.setError("Required!");
            valid = false;
        } else if (TextUtils.isEmpty((rePassword))) {
            retypePasswordField.setError("Required!");
            valid = false;
        } else {
            if (!password.equals(rePassword)) {
                retypePasswordField.setError("Password dindn't match");
                valid = false;
            }
        }
        if (ContextCompat.checkSelfPermission(TeacherRegister.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            valid = false;
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null)
                user.delete();
            Toast.makeText(this, "You shoould provide permission!! ", Toast.LENGTH_LONG).show();


        }

        return valid;
    }

    @Override
    public void onClick(View view) {
        if (!AppStatus.getInstance(this).isOnline()) {
            Toast.makeText(this, "You are not online!!!!", Toast.LENGTH_LONG).show();
            return;
        }
        if (view.getId() == R.id.teacherRegisterButton) {
            createAccount(emailField.getText().toString(), passwordField.getText().toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }

        }
    }

    private class CreateNewTeacher extends AsyncTask<String, String, String> {
        String name, id, email, phnNo, deviceId, userId;
        int verified = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hideProgressDialog();
            showProgressDialog("Updating..please wait..");
            name = nameField.getText().toString();
            id = teacherIdField.getText().toString();
            email = emailField.getText().toString();
            phnNo = phnNoField.getText().toString();
            if (mAuth.getCurrentUser() != null)
                userId = mAuth.getCurrentUser().getUid();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            try {
                //uniquely identifies phone
                deviceId = telephonyManager.getDeviceId();
            } catch (SecurityException e) {
                Log.i("PERMISSION", e.toString());
            }
        }


        @Override
        protected String doInBackground(String... strings) {
            ///ContentValues params = new ContentValues();

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("name", name);
            params.put("id", id);
            params.put("email", email);
            params.put("phoneNo", phnNo);
            params.put("deviceId", deviceId);
            params.put("userId", userId);
            params.put("verified", verified);

            String link = BASE_URL + "create_teacher.php";

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

                URL url = new URL(link);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty("Accept", "*/*");
                OutputStream out = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

                Log.d("data", data);
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());


                String result = convertStreamToString(inputStream);
                httpURLConnection.disconnect();
                Log.d("TAG", result);
                return result;
            } catch (Exception e) {
                Log.d("debug", e.getMessage());
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
            //if string returned from doinbackground is null, that means Exception occurred while connection to server
            if (s == null) {
                Toast.makeText(TeacherRegister.this, "Could not connect to PHPServer", Toast.LENGTH_LONG).show();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null)
                    user.delete();
            } else {
                //otherwise string would contain the JSON returned from php
                JSONParser parser = new JSONParser();
                try {
                    JSONObject jsonObject = (JSONObject) parser.parse(s);
                    Object p = jsonObject.get("success");
                    int successCode = Integer.parseInt(p.toString());
                    Log.d("TAGS", Integer.toString(successCode));


                    if (successCode == 0) {

                        Toast.makeText(TeacherRegister.this, "Error occurred: device Id /teacher Id already present", Toast.LENGTH_LONG).show();
                        //in this case delete the already authenticated user from Firebase
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null)
                            user.delete();

                    } else {

                        Toast.makeText(TeacherRegister.this, "Registered Successfully!!", Toast.LENGTH_LONG).show();
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("details", 0); //Mode_private
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("user", "teacher");
                        editor.apply();

                        //To get the token in order to send notification to this user when the request to verify is
                        //approved by the admin
                        //Uses Firebase database to store these token in form of key value pair, with teacherId as key
                        //This is retrieved when the admin verifies
                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        if (!task.isSuccessful()) {
                                            Log.w("TAG", "getInstanceId failed", task.getException());
                                            return;
                                        }
                                        String token = task.getResult().getToken();
                                        // Log and toast
                                        Log.d("TAG", token);
                                        //Firebase database
                                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("tokens");
                                        mDatabase.child(id).setValue(token);
                                    }
                                });

                        //getting the admin token to send notification to the admin
                        //that new teacher is registered.
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        reference.child("admin").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String token = dataSnapshot.getValue().toString();
                                    Log.d("admin", token);
                                    try {
                                        FireMessage fm = new FireMessage("New Request For Verification", name + " Requested for verification");
                                        fm.sendToToken(token);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        Intent intent = new Intent(getApplicationContext(), TeacherSubject.class);
                        startActivity(intent);
                        finish();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
