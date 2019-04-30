package com.sanxynet_ads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.image_view)
    ImageView imageView;
    @BindView(R.id.button_select_pic)
    Button selectPicture;
    @BindView(R.id.button_upload)
    Button upload;
    @BindView(R.id.button_logout)
    Button logout;

    @BindView(R.id.spinner_time)
    Spinner spinnerTime;
    @BindView(R.id.edit_text_date)
    EditText mEditText;
    @BindView(R.id.edit_text_start_time)
    EditText startTime;
    @BindView(R.id.edit_text_end_time)
    EditText endTime;

    DatePickerDialog picker;

    /* Image request code */
    private int PICK_IMAGE_REQUEST = 1;

    /* Uri to store the image uri */
    private Uri filePath;
    /* storage permission code */
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Context context;
    String imagepath;
    File sourceFile;
    int totalSize = 0;
    ProgressDialog mProgressDialog;

    private ArrayAdapter<CharSequence> adapter;
    private List<CharSequence> items;
    private String[] strings;
    String time, value, date, start, end;
    TimePickerDialog timePickerDialog;
    Calendar calendar;
    int currentHour;
    int currentMinute;
    String amPm;
    int[] ids = new int[]
            {
                    R.id.edit_text_date,
                    R.id.edit_text_start_time,
                    R.id.edit_text_end_time
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        context = getApplicationContext();
        checkConnection();
        mProgressDialog = new ProgressDialog(this);

        strings = getResources().getStringArray(R.array.time_array);
        items = new ArrayList<>(Arrays.asList(strings));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerTime.setAdapter(adapter);

        mEditText.setInputType(InputType.TYPE_NULL);
        startTime.setInputType(InputType.TYPE_NULL);
        endTime.setInputType(InputType.TYPE_NULL);

    }


    @OnItemSelected(R.id.spinner_time)
        //default callback : ITEM_SELECTED
    void onItemSelected(int position) {
        time = spinnerTime.getItemAtPosition(position).toString();
    }

    @OnItemSelected(value = R.id.spinner_time, callback = OnItemSelected.Callback.NOTHING_SELECTED)
    void onNothingSelected() {
    }

    @SuppressLint("SetTextI18n")
    @OnClick(R.id.edit_text_date)
    public void setDate(View view) {
        /* set date */
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
//        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        // date picker dialog
        picker = new DatePickerDialog(MainActivity.this,
                (view1, year1, monthOfYear, dayOfMonth) ->
                mEditText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1), year, month, day);
        picker.show();
    }

    @SuppressLint("SetTextI18n")
    @OnClick(R.id.edit_text_start_time)
    public void startTime(View view) {
        /* set Start Time */
        calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinute = calendar.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(MainActivity.this, (timePicker, hourOfDay, minutes) -> {
            if (hourOfDay >= 12) {
                amPm = "PM";
            } else {
                amPm = "AM";
            }
            startTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minutes));

        }, currentHour, currentMinute, false);

        timePickerDialog.show();

    }

    @SuppressLint("SetTextI18n")
    @OnClick(R.id.edit_text_end_time)
    public void endTime(View view) {
        /* set End Time */
        calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinute = calendar.get(Calendar.MINUTE);
        timePickerDialog = new TimePickerDialog(MainActivity.this, (timePicker, hourOfDay, minutes) -> {
            if (hourOfDay >= 12) {
                amPm = "PM";
            } else {
                amPm = "AM";
            }
            endTime.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minutes) );

        }, currentHour, currentMinute, false);

        timePickerDialog.show();

    }

    @OnClick(R.id.button_select_pic)
    public void selectImage(View view) {
        /* select image from storage */
        selectImg();
    }

    private void selectImg(){
        /* Check whether the app is installed on Android 6.0 or higher// */

        if (Build.VERSION.SDK_INT >= 23) {

            /* Check whether your app has access to the READ permission// */
            if (checkPermission()) {

                /* If your app has access to the device’s storage, then print the following message to Android Studio’s Logcat// */
                showFileChooser();
                Log.e("permission", "Permission already granted.");
            } else {

                /* If your app doesn’t have permission to access external storage, then call requestPermission// */
                requestPermission();
            }
        }
    }

    @OnClick(R.id.button_upload)
    public void upload(View view) {
        // upload image to database
        uploadImg();
    }

    private void uploadImg() {
        if (imagepath != null) {
            if(!validateEditText(ids)) {
                //if not empty do something
                alertDialog();
            } else if (validateEditText(ids)){
                //if empty do something else
                Toast.makeText(getApplicationContext(), "Set Time and Date", Toast.LENGTH_LONG).show();

            }

        }else{
            Toast.makeText(getApplicationContext(), "Please select a file to upload", Toast.LENGTH_LONG).show();
        }
    }

    private void alertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("You will be charged per hour selected");
        dialog.setTitle("Confirm Charges");
        dialog.setPositiveButton("YES",
                (dialog1, which) ->
                        new UploadFileToServer().execute());

        dialog.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();

    }



    private void calAmount(){

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
        String currentDateandTime = sdf.format(new Date());
        Date date = null;
        try {
            date = sdf.parse(currentDateandTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (time.equals("1 Hour")){
            calendar.add(Calendar.HOUR, 1);
            value = String.valueOf(calendar.getTime());


//            value = "rs 500";
        } if (time.equals("2 Hours")){
            calendar.add(Calendar.HOUR, 2);
            value = String.valueOf(calendar.getTime());
//            value = "rs 1000";
        } if (time.equals("3 Hours")){
            calendar.add(Calendar.HOUR, 3);
            value = String.valueOf(calendar.getTime());
//            value = "rs 1500";
        } if (time.equals("4 Hours")){
            calendar.add(Calendar.HOUR, 4);
            value = String.valueOf(calendar.getTime());
//            value = "rs 2000";
        } if (time.equals("5 Hours")){
            calendar.add(Calendar.HOUR, 5);
            value = String.valueOf(calendar.getTime());
//            value = "rs 2500";
        } if (time.equals("6 Hours")){
            calendar.add(Calendar.HOUR, 6);
            value = String.valueOf(calendar.getTime());
//            value = "rs 3000";
        } if (time.equals("7 Hours")){
            calendar.add(Calendar.HOUR, 7);
            value = String.valueOf(calendar.getTime());
//            value = "rs 3500";
        } if (time.equals("8 Hours")){
            calendar.add(Calendar.HOUR, 8);
            value = String.valueOf(calendar.getTime());
//            value = "rs 4000";
        } if (time.equals("9 Hours")){
            calendar.add(Calendar.HOUR, 9);
            value = String.valueOf(calendar.getTime());
//            value = "rs 4500";
        } if (time.equals("10 Hours")){
            calendar.add(Calendar.HOUR, 10);
            value = String.valueOf(calendar.getTime());
//            value = "rs 5000";
        } if (time.equals("11 Hours")){
            calendar.add(Calendar.HOUR, 11);
            value = String.valueOf(calendar.getTime());
//            value = "rs 5500";
        } if (time.equals("12 Hours")){
            calendar.add(Calendar.HOUR, 12);
            value = String.valueOf(calendar.getTime());
//            value = "rs 6000";
        } if (time.equals("13 Hours")){
            calendar.add(Calendar.HOUR, 13);
            value = String.valueOf(calendar.getTime());
//            value = "rs 6500";
        } if (time.equals("14 Hours")){
            calendar.add(Calendar.HOUR, 14);
            value = String.valueOf(calendar.getTime());
//            value = "rs 7000";
        } if (time.equals("15 Hours")){
            calendar.add(Calendar.HOUR, 15);
            value = String.valueOf(calendar.getTime());
//            value = "rs 7500";
        } if (time.equals("16 Hours")){
            calendar.add(Calendar.HOUR, 16);
            value = String.valueOf(calendar.getTime());
//            value = "rs 8000";
        } if (time.equals("17 Hours")){
            calendar.add(Calendar.HOUR, 17);
            value = String.valueOf(calendar.getTime());
//            value = "rs 8500";
        } if (time.equals("18 Hours")){
            calendar.add(Calendar.HOUR, 18);
            value = String.valueOf(calendar.getTime());
//            value = "rs 9000";
        } if (time.equals("19 Hours")){
            calendar.add(Calendar.HOUR, 19);
            value = String.valueOf(calendar.getTime());
//            value = "rs 9500";
        } if (time.equals("20 Hours")){
            calendar.add(Calendar.HOUR, 20);
            value = String.valueOf(calendar.getTime());
//            value = "rs 1000";
        } if (time.equals("21 Hours")){
            calendar.add(Calendar.HOUR, 21);
            value = String.valueOf(calendar.getTime());
//            value = "rs 10500";
        } if (time.equals("22 Hours")){
            calendar.add(Calendar.HOUR, 22);
            value = String.valueOf(calendar.getTime());
//            value = "rs 11000";
        } if (time.equals("23 Hours")){
            calendar.add(Calendar.HOUR, 23);
            value = String.valueOf(calendar.getTime());
//            value = "rs 11500";
        } if (time.equals("24 Hours")){
            calendar.add(Calendar.HOUR, 24);
            value = String.valueOf(calendar.getTime());
//            value = "rs 12000";
        }
    }

    @OnClick(R.id.button_logout)
    public void logout(View view) {
        // Sign out
        signOut();
    }

    private void signOut() {
        ParseUser.logOutInBackground(e -> {
            if (e == null) {
                new User(Objects.requireNonNull(getApplicationContext())).removeUser();
                Intent signout = new Intent(getApplicationContext(), LoginActivity.class);
                signout.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getApplicationContext().startActivity(signout);

            } else {
                Toast.makeText(getApplicationContext(), "Error Occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }

    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            Uri selectedImageUri = data.getData();
            imagepath = getPath(selectedImageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            // down sizing image as it throws OutOfMemory Exception for larger images
            // options.inSampleSize = 10;
            final Bitmap bitmap = BitmapFactory.decodeFile(imagepath, options);
            imageView.setImageBitmap(bitmap);

        }
    }

    //method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        assert cursor != null;
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private boolean checkPermission() {

        /* Check for READ_EXTERNAL_STORAGE access, using ContextCompat.checkSelfPermission()// */

        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        /* If the app does have this permission, then return true// */

        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {

            /* If the app doesn’t have this permission, then return false// */

            return false;
        }
    }

    //Requesting permission
    private void requestPermission() {

    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,
                            "Permission accepted", Toast.LENGTH_LONG).show();
                    showFileChooser();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Permission denied", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    private void checkConnection(){
        if (ConnectivityHelper.isConnectedToNetwork(context)) {
            //Show the connected screen

        } else {
            //Show disconnected screen
            Toast.makeText(MainActivity.this,
                    "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkConnection();
    }

    @SuppressLint("StaticFieldLeak")
    private class UploadFileToServer extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            sourceFile = new File(imagepath);
            totalSize = (int)sourceFile.length();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Sending image to database");
            mProgressDialog.setMessage("Uploading........");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            Object image = null;
            try {
                image = readInFile(sourceFile);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            // Create the ParseFile
            ParseFile file = new ParseFile("pictureUpload.jpg", (byte[]) image);
            // Upload the image into Parse Cloud
            file.saveInBackground();

            // Create a New Class called "ImageUpload" in Parse
            ParseObject imgupload = new ParseObject("imageupload");

            // Create a column named "ImageName" and set the string
            imgupload.put("ImageName", "pictureUpload");

            // Get the value of date from edit text
            date = mEditText.getText().toString();
            start = startTime.getText().toString();
            end = endTime.getText().toString();
            calAmount();

            // Create a column named "ImageFile" and insert the image
            imgupload.put("ImageFile", file);
            imgupload.put("senderId", ParseUser.getCurrentUser().getObjectId());
            imgupload.put("username", ParseUser.getCurrentUser().getUsername());
            imgupload.put("date", date);
            imgupload.put("time", time);
            imgupload.put("startTime", start);
            imgupload.put("endTime", end);

            // Create the class and the columns
            imgupload.saveInBackground();

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("Response", "Response from server: " + result);
            mProgressDialog.dismiss();
            // Show a simple toast message
            Toast.makeText(MainActivity.this, "Image Saved Successfully",Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);
        }

    }

    private byte[] readInFile(File path) throws IOException {
        // TODO Auto-generated method stub
        byte[] data = null;
        File file = new File(String.valueOf(path));
        InputStream input_stream = new BufferedInputStream(new FileInputStream(
                file));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        data = new byte[16384]; // 16K
        int bytes_read;
        while ((bytes_read = input_stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytes_read);
        }
        input_stream.close();
        return buffer.toByteArray();

    }

    public boolean validateEditText(int[] ids) {
        boolean isEmpty = false;

        for(int id: ids)
        {
            EditText et = findViewById(id);

            if(TextUtils.isEmpty(et.getText().toString()))
            {
                et.setError("Must enter Value");
                isEmpty = true;
            }
        }

        return isEmpty;
    }

}

