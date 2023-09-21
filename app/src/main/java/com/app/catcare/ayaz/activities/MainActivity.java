package com.app.catcare.ayaz.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.app.catcare.ayaz.R;
import com.app.catcare.ayaz.databinding.ActivityMainBinding;
import com.app.catcare.ayaz.utils.Constants;
import com.app.catcare.ayaz.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager=new PreferenceManager(getApplicationContext());

        //OCR
        binding.cardViewOcr.setOnClickListener(view->{
            Intent myIntent = new Intent(MainActivity.this, OCRActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        //Journey Planner
        binding.cardViewJp.setOnClickListener(view -> {
            Intent myIntent = new Intent(MainActivity.this, JourneyPlannerActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        //Task Reminder
        binding.cardViewReminder.setOnClickListener(view -> {
            Intent myIntent = new Intent(MainActivity.this, AddTaskActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        //Home Automation
        binding.cardViewHomeAuto.setOnClickListener(view -> {
            Intent myIntent = new Intent(MainActivity.this, IOTActivity.class);
            MainActivity.this.startActivity(myIntent);
        });

        //Logout
        binding.tvLogout.setOnClickListener(view -> {
            try {
                preferenceManager.clear();
                startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                finish();
                showToast("Logout Successfully.");
            }
            catch (Exception e)
            {
                Log.d("Sign-out: ",e.getMessage());
            }
        });
        loadUserDetails();
    }
    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }
    private void loadUserDetails() {
        binding.tvName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.tvAge.setText(preferenceManager.getString(Constants.KEY_TOTAL_AGE));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.ivProfile.setImageBitmap(bitmap);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}