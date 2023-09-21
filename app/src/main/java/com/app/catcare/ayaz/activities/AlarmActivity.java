package com.app.catcare.ayaz.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.app.catcare.ayaz.R;
import com.app.catcare.ayaz.databinding.ActivityAlarmBinding;
import com.bumptech.glide.Glide;


public class AlarmActivity extends AppCompatActivity{
    private ActivityAlarmBinding binding;
        private static AlarmActivity inst;

        MediaPlayer mediaPlayer;

        public static AlarmActivity instance() {
            return inst;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding=ActivityAlarmBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());


            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.notification);
            mediaPlayer.start();

            if(getIntent().getExtras() != null) {
                binding.title.setText(getIntent().getStringExtra("TITLE"));
                binding.description.setText(getIntent().getStringExtra("DESC"));
                binding.timeAndData.setText(getIntent().getStringExtra("DATE") + ", " + getIntent().getStringExtra("TIME"));
            }

            Glide.with(getApplicationContext()).load(R.drawable.alert).into(binding.imageView);
            binding.closeButton.setOnClickListener(view -> openTaskActivity());

            binding.ibBack.setOnClickListener(view ->
            {
               openTaskActivity();
            });
        }

    private void openTaskActivity() {
        Intent myIntent = new Intent(AlarmActivity.this, AddTaskActivity.class);
        AlarmActivity.this.startActivity(myIntent);
        finish();
    }

    @Override
        protected void onDestroy() {
            super.onDestroy();
            mediaPlayer.release();
        }
}
