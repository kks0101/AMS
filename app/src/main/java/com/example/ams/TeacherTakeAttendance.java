package com.example.ams;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class TeacherTakeAttendance extends BaseActivity {
    private TextView displaySubjectCode, displayGroup;
    private Button generateQr;
    private static int QRCodeWidth = 1000;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_take_attendance);

        TeacherSubjectDetail teacherSubjectDetail = (TeacherSubjectDetail) getIntent().getSerializableExtra("TeacherSubjectDetail");
        displaySubjectCode = (TextView)findViewById(R.id.displaySubjectCode);
        displayGroup = (TextView)findViewById(R.id.displayGroup);
        generateQr = (Button)findViewById(R.id.scanQrCode);
        if(teacherSubjectDetail!=null) {
            displaySubjectCode.setText(teacherSubjectDetail.getSubjectCode());
            displayGroup.setText(teacherSubjectDetail.getBranch());
        }

        generateQr.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                GenerateQr generateQr = new GenerateQr();
                generateQr.execute();

            }
        });
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

    class GenerateQr extends AsyncTask<String, String, String>{
        String subject, branch;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog("Generating QR Code..Please Wait");
            subject = displaySubjectCode.getText().toString();
            branch = displayGroup.getText().toString();
        }

        @Override
        protected String doInBackground(String... strings) {
            JSONObject credentials = new JSONObject();
            try{
                credentials.put("subject", subject);
                credentials.put("group", branch);
            }catch (JSONException e){
                e.printStackTrace();
            }
            String value = credentials.toString();
            try {
                bitmap = TextToImageEncode(value);
                Intent intent = new Intent(TeacherTakeAttendance.this, QRCodeGenerator.class);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                intent.putExtra("bitmap", byteArray);
                startActivity(intent);
            } catch (WriterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            hideProgressDialog();
        }
    }
}
