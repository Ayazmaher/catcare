package com.app.catcare.ayaz.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.Toast;

import com.app.catcare.ayaz.R;
import com.app.catcare.ayaz.databinding.ActivitySignInBinding;
import com.app.catcare.ayaz.utils.Constants;
import com.app.catcare.ayaz.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager=new PreferenceManager(getApplicationContext());

        binding.btnSignIn.setOnClickListener(view -> {
            if (isValidSignInDetails())
            {
                SigIn();
            }
        });
        binding.SignUpForNew.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            SignInActivity.this.startActivity(intent);
        });

        binding.ForgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, RecoverPasswordActivity.class);
            SignInActivity.this.startActivity(intent);
        });



        binding.checkboxPassword.setOnClickListener(v -> {
            if (binding.checkboxPassword.isChecked()) {
                binding.enterPasswordEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.checkboxPassword.setText(getString(R.string.hide_password));
            } else {
                binding.enterPasswordEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                binding.checkboxPassword.setText(getString(R.string.show_password));
            }
        });
    }
    private void SigIn()
    {

        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.getEmailEt.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.enterPasswordEt.getText().toString())
                .get()
                .addOnCompleteListener(task->{
                    if (task.isSuccessful()&&task.getResult()!=null
                            &&task.getResult().getDocuments().size()>0)
                    {
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_DOB,documentSnapshot.getString(Constants.KEY_DOB));
                        preferenceManager.putString(Constants.KEY_TOTAL_AGE,documentSnapshot.getString(Constants.KEY_TOTAL_AGE));
                        preferenceManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else {

                        showToast("Unable to Sign In");

                    }
                });
    }
    private Boolean isValidSignInDetails()
    {

        if (Objects.requireNonNull(binding.enterPasswordEt.getText()).toString().trim().isEmpty())
        {
            showToast("Enter Email");

            return false;
        }
        else if (!(Patterns.EMAIL_ADDRESS.matcher(binding.getEmailEt.getText().toString()).matches()))
        {
            showToast("Enter Valid Email");

            return false;

        }
        else if (binding.enterPasswordEt.getText().toString().trim().isEmpty())
        {
            showToast("Enter Password");

            return false;
        }
        else if (binding.enterPasswordEt.getText().toString().length() < 6)
        {
            showToast("Password length is shorter than 6 characters!");

            return false;

        }
        else {
            return true;
        }
    }
    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }
}