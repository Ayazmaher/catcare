package com.app.catcare.ayaz.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.app.catcare.ayaz.R;
import com.app.catcare.ayaz.databinding.ActivityRecoverPasswordBinding;
import com.app.catcare.ayaz.utils.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;


public class RecoverPasswordActivity extends AppCompatActivity {
    private ActivityRecoverPasswordBinding binding;
    private AlertDialog dialogShowPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRecoverPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ibBack.setOnClickListener(view -> {
            onBackPressed();
        });

        binding.btnRecoverPass.setOnClickListener(v -> {
            String getEmailReset=binding.getEmailEtRec.getText().toString().trim();
            try {
                if (!(Patterns.EMAIL_ADDRESS.matcher(getEmailReset).matches()))
                {
                    throw new Exception("Invalid Email");
                }
                else {
                    showPass();
                }
            }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPass()
    {

        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, Objects.requireNonNull(binding.getEmailEtRec.getText()).toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()&&task.getResult()!=null&&task.getResult().getDocuments().size()>0)
                    {
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        showPassDialog( documentSnapshot.getString(Constants.KEY_PASSWORD));

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Email is not registered!",Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void showPassDialog(String Value) {

        if (dialogShowPass==null)
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(RecoverPasswordActivity.this);
            View view= LayoutInflater.from(this).inflate(
                    R.layout.layout_pass_recover,
                    findViewById(R.id.layoutShowPassContainer)
            );
            builder.setView(view);
            dialogShowPass=builder.create();
            if (dialogShowPass.getWindow()!=null)
            {
                dialogShowPass.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            TextView show;
            show=view.findViewById(R.id.tvRecPass);
            show.setText(Value);
            view.findViewById(R.id.btnCloseRec).setOnClickListener(v -> {
                dialogShowPass.dismiss();
                finish();
            });

            view.findViewById(R.id.btnCopyPass).setOnClickListener(v -> {
                copyToClipboard(Value);
                dialogShowPass.dismiss();
            });
        }
        dialogShowPass.show();
    }

    private void copyToClipboard(String value) {
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", value);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Password is copied.", Toast.LENGTH_SHORT).show();
            finish();
        }
        catch (Exception e)
        {
            Log.d("COPY_CLIP_ERROR", Objects.requireNonNull(e.getMessage()));
        }
    }
}