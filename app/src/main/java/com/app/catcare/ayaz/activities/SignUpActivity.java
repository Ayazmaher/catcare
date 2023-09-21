package com.app.catcare.ayaz.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.app.catcare.ayaz.R;
import com.app.catcare.ayaz.databinding.ActivitySignUpBinding;
import com.app.catcare.ayaz.utils.Constants;
import com.app.catcare.ayaz.utils.PreferenceManager;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;

    private String totalAge;
    private Uri imageUri;
    private Bitmap imageBitmap;

    private String tempDate;

    private PreferenceManager preferenceManager;

    private DatePickerDialog datePickerDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding.datePickerbtn.setText(getCurrentDate());




        initDatePicker();
        binding.SigninAlreadyAccount.setOnClickListener(view -> {
            Intent myIntent = new Intent(SignUpActivity.this, SignInActivity.class);
            SignUpActivity.this.startActivity(myIntent);
        });
        binding.btnSignUp.setOnClickListener(view -> {
            Intent myIntent = new Intent(SignUpActivity.this, MainActivity.class);
            SignUpActivity.this.startActivity(myIntent);
        });


        binding.ibBack.setOnClickListener(view -> {
            onBackPressed();
        });

        binding.imageProfileSignUp.setOnClickListener(view -> {
            openImagePickerDialog();
        });

        binding.datePickerbtn.setOnClickListener(view -> {
            openDatePicker();
        });


        binding.btnSignUp.setOnClickListener(view -> {
            if (isValidSignUpDetails())
            {
                signUp();
            }
        });



        binding.checkboxPasswordSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.checkboxPasswordSignup.isChecked()) {
                    binding.registerPasswordEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    binding.checkboxPasswordSignup.setText(getString(R.string.hide_password));
                } else {
                    binding.registerPasswordEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    binding.checkboxPasswordSignup.setText(getString(R.string.show_password));
                }
            }
        });


    }

    private void signUp() {
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        HashMap<String,Object> user=new HashMap<>();
        user.put(Constants.KEY_NAME, Objects.requireNonNull(binding.registerUsername.getText()).toString());
        user.put(Constants.KEY_EMAIL, Objects.requireNonNull(binding.registerEmailEt.getText()).toString());
        user.put(Constants.KEY_PASSWORD, Objects.requireNonNull(binding.registerPasswordEt.getText()).toString());
        user.put(Constants.KEY_IMAGE,encodeImage(imageBitmap));
        user.put(Constants.KEY_DOB, binding.datePickerbtn.getText());
        user.put(Constants.KEY_TOTAL_AGE, totalAge);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,binding.registerUsername.getText().toString());
                    preferenceManager.putString(Constants.KEY_DOB,binding.datePickerbtn.getText().toString());
                    preferenceManager.putString(Constants.KEY_TOTAL_AGE,totalAge);
                    preferenceManager.putString(Constants.KEY_IMAGE,encodeImage(imageBitmap));
                    Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                }).addOnFailureListener(exception ->{
                    showToast(exception.getMessage());

                } );
    }
    private String encodeImage(Bitmap bitmap)
    {
        int previewWidth=150;
        int previewHeight=bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap=Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] bytes=byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    private void openDatePicker() {
        datePickerDialog.show();
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        month = month + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);

    }

    private void openImagePickerDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.layout_pick_image);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.findViewById(R.id.tvCameraProfile).setOnClickListener(view1->{
            openCamera();
            dialog.dismiss();
        });
        dialog.findViewById(R.id.tvGalleryProfile).setOnClickListener(view1->{
            openGallery();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void openGallery() {
        try {
            ImagePicker.with(this)
                    .crop()
                    .maxResultSize(512,512)
                    .galleryOnly()	//User can only capture image using Gallery
                    .start();
        }
        catch (Exception e)
        {
            Log.e("ErrorProfile2: ", e.getMessage());
        }
    }

    private void openCamera() {
        try
        {
            ImagePicker.with(this)
                .crop()
                .maxResultSize(512,512)
                .cameraOnly()	//User can only capture image using Camera
                .start();
        }
            catch (Exception e)
            {
                Log.e("ErrorProfile1: ", e.getMessage());
            }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== Activity.RESULT_OK){
            assert data != null;
            imageUri=data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                binding.imageProfileSignUp.setImageBitmap(imageBitmap);
                binding.textAddImage.setVisibility(View.GONE);
            } catch (IOException e) {

                throw new RuntimeException(e);
            }
        }
        else if (resultCode == ImagePicker.RESULT_ERROR) {
            showToast( ImagePicker.getError(data));

        } else {
            showToast("Task Cancelled");

        }
    }
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            month = month + 1;
            String date = makeDateString(dayOfMonth, month, year);

                binding.datePickerbtn.setText(date);
                tempDate=binding.datePickerbtn.getText().toString();
                totalAge= String.valueOf(getAge(getDateString(dayOfMonth,month,year)));
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;
        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }



    private String makeDateString(int dayOfMonth, int month, int year) {
        return getMonthFormat(month) + " " + dayOfMonth + " " + year;
    }
    private String getDateString(int dayOfMonth, int month, int year) {
        return dayOfMonth + "/" + month + "/" + year;
    }

    private String getMonthFormat(int month) {
        if (month == 1) {
            return "JAN";
        }
        if (month == 2) {
            return "FEB";
        }
        if (month == 3) {
            return "MAR";
        }
        if (month == 4) {
            return "APR";
        }
        if (month == 5) {
            return "MAY";
        }
        if (month == 6) {
            return "JUN";
        }
        if (month == 7) {
            return "JUL";
        }
        if (month == 8) {
            return "AUG";
        }
        if (month == 9) {
            return "SEP";
        }
        if (month == 10) {
            return "OCT";
        }
        if (month == 11) {
            return "NOV";
        }
        if (month == 12) {
            return "DEC";
        }
        return "JAN";
    }

    private int getAge(String dobString){

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            date = sdf.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(date == null) return 0;

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int year = dob.get(Calendar.YEAR);
        int month = dob.get(Calendar.MONTH);
        int day = dob.get(Calendar.DAY_OF_MONTH);

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }


        return age;
    }
    private Boolean isValidSignUpDetails() {
        if (imageBitmap == null) {
            showToast("Select Profile Image!");
            return false;
        } else if (Objects.requireNonNull(binding.registerUsername.getText()).toString().trim().isEmpty()) {
            showToast("Enter Name!");
            return false;
        } else if (Objects.requireNonNull(binding.registerEmailEt.getText()).toString().trim().isEmpty()) {
            showToast("Enter Name!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.registerEmailEt.getText().toString().trim()).matches()) {
            showToast("Enter Valid Email!");
            return false;
        } else if (Objects.requireNonNull(binding.registerPasswordEt.getText()).toString().trim().length() < 6) {
            showToast("Password must be greater than 6 characters!");
            return false;
        } else if (binding.registerPasswordEt.getText().toString().trim().isEmpty()) {
            showToast("Enter Password!");
            return false;
        }
        else if (Objects.equals(tempDate, ""))
        {
            showToast("Select Date");
            return false;
        }
        else {
            return true;
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}