package com.app.catcare.ayaz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.app.catcare.ayaz.databinding.ActivityIotBinding;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class IOTActivity extends AppCompatActivity {
    private ActivityIotBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityIotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getUpdateFromServer();


        //LED ONE
        binding.btnLed1.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
            {
                ledOnOff("btn1",true);
            }
            else {
                ledOnOff("btn1",false);
            }
        });

        //LED TWO

        binding.btnLed2.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b)
            {
                ledOnOff("btn2",true);
            }
            else {
                ledOnOff("btn2",false);
            }
        });



        binding.ibBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }



    private void ledOnOff(String buttonName, boolean status) {
        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference reference=database.getReference("cateCare");
        reference.child(buttonName).setValue(status).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                if (task.isSuccessful())
                {
                    if (status)
                    {
                        showToast("[ON]");
                    }
                    else {
                        showToast("[OFF]");
                    }
                }
                else
                {
                    showToast("Something went wrong...!");
                }

            }
            else
            {
                showToast("User not registered!");
            }
        });
    }

    private void getUpdateFromServer() {

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference reference=database.getReference("cateCare");

        // Read from the database
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                //FIRE
                if (Objects.equals(dataSnapshot.child("fire").getValue(), true)){
                    binding.tvFireStatus.setText("YES");
                }
                else {
                    binding.tvFireStatus.setText("NO");
                }

                //MOTION
                if (Objects.equals(dataSnapshot.child("motion").getValue(), true)){
                    binding.tvMotionStatus.setText("YES");
                }
                else {
                    binding.tvMotionStatus.setText("NO");
                }

                //LPG
                if (Objects.equals(dataSnapshot.child("lpg").getValue(), true)){
                    binding.tvGasStatus.setText("YES");
                }
                else {
                    binding.tvGasStatus.setText("NO");
                }

                //LED 1
                if (Objects.equals(dataSnapshot.child("btn1").getValue(), true)){
                    binding.btnLed1.setChecked(true);
                }
                else {
                    binding.btnLed1.setChecked(false);
                }

                //LED 2
                if (Objects.equals(dataSnapshot.child("btn2").getValue(), true)){
                    binding.btnLed2.setChecked(true);
                }
                else {
                    binding.btnLed2.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("FirebaseRead:", "Failed to read value.", error.toException());
            }
        });

    }
    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }



}