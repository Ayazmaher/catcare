package com.app.catcare.ayaz.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.window.SplashScreen;

import com.app.catcare.ayaz.R;
import com.app.catcare.ayaz.databinding.ActivitySplashBinding;
import com.app.catcare.ayaz.utils.Constants;
import com.app.catcare.ayaz.utils.PreferenceManager;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    public static int SPLASH_TIME_OUT=2000;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager=new PreferenceManager(getApplicationContext());

        new Handler().postDelayed(() -> {

            if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
            {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
            else {
                startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                finish();
            }

        },SPLASH_TIME_OUT);
    }
}