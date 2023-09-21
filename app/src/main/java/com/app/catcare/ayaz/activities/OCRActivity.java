package com.app.catcare.ayaz.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.app.catcare.ayaz.R;
import com.app.catcare.ayaz.databinding.ActivityOcractivityBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.jsibbold.zoomage.ZoomageView;

import java.io.IOException;

public class OCRActivity extends AppCompatActivity {
    private ActivityOcractivityBinding binding;
    private Uri imageUri;
    private Bitmap imageBitmap;
    private int selectedScript=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityOcractivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ibBack.setOnClickListener(view->{
            getOnBackPressedDispatcher().onBackPressed();
        });


        //Camera
        binding.tvCamera.setOnClickListener(view -> {
            try {
                ImagePicker.with(this)
                        .crop()
                        .maxResultSize(1080,1080)
                        .cameraOnly()	//User can only capture image using Camera
                        .start();
            }
            catch (Exception e)
            {
                Log.e("ErrorOCR1: ", e.getMessage());
            }
        });
        //Gallery
        binding.tvGallery.setOnClickListener(view -> {
            try {
                ImagePicker.with(this)
                        .crop()
                        .maxResultSize(1080,1080)
                        .galleryOnly()	//User can only capture image using Gallery
                        .start();
            }
            catch (Exception e)
            {
                Log.e("ErrorOCR1: ", e.getMessage());
            }
        });

        //Close Text Box
        binding.closeTexBox.setOnClickListener(view->{
            binding.etResults.getText().clear();
            showViews();
        });

        //Copy to clipboard
        binding.ibCopy.setOnClickListener(view->{
            copyToClipboard(binding.etResults.getText().toString());
        });

        //share to others
        binding.ibShare.setOnClickListener(view ->{
            shareToOthers(binding.etResults.getText().toString());
        });

        //edit text
        binding.ibEdit.setOnClickListener(view -> {
            binding.etResults.setEnabled(true);
        });

        //Image Preview
        binding.ibPreview.setOnClickListener(view -> {
            ImagePreview();
        });

        //Text Maximize
        binding.ibMaximize.setOnClickListener(view -> {
            maximizeTextView(binding.etResults.getText().toString());
        });



    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showProgress();
        if (resultCode== Activity.RESULT_OK){
            assert data != null;
            imageUri=data.getData();
//            binding.ivThumbOcr.setImageURI(imageUri);
            try {
                 imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                 loadScript(imageBitmap);
            } catch (IOException e) {
                hideProgress();
                throw new RuntimeException(e);
            }

        }
        else if (resultCode == ImagePicker.RESULT_ERROR) {
            showToast( ImagePicker.getError(data));
            hideProgress();
        } else {
            showToast("Task Cancelled");
            hideProgress();
        }
    }



    private void loadScript(Bitmap bitmap){
        if (selectedScript==0){
            TextRecognizer recognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            recognizeText(recognizer,imageFromBitmap(bitmap));
        }
    }
    private InputImage imageFromBitmap(Bitmap bitmap){
        return InputImage.fromBitmap(bitmap, 0);
    }
    private void recognizeText(TextRecognizer recognizer, InputImage image) {
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                if (visionText.getText().equals(""))
                                {
                                    showToast("Text Not Found.!");
                                    hideProgress();
                                }
                                else {
                                    // Task completed successfully
                                    Log.e("Result OCR", visionText.getText());
                                    binding.etResults.setText(visionText.getText());
                                    binding.etResults.setMovementMethod(new ScrollingMovementMethod());
                                    hideView();
                                }
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        Log.e("Failed",e.getMessage());
                                        showToast("Text Not Found.!");
                                        hideProgress();
                                    }
                                });
    }

    private void hideView() {
        hideProgress();
        //Hide Main views
        binding.ivThumbOcr.setVisibility(View.GONE);
        binding.tvCamera.setVisibility(View.GONE);
        binding.tvGallery.setVisibility(View.GONE);
        binding.viewCenter.setVisibility(View.GONE);


        //Show Result Views
        binding.etResults.setVisibility(View.VISIBLE);
        binding.closeTexBox.setVisibility(View.VISIBLE);
        binding.ibCopy.setVisibility(View.VISIBLE);
        binding.ibShare.setVisibility(View.VISIBLE);
        binding.ibMaximize.setVisibility(View.VISIBLE);
        binding.ibPreview.setVisibility(View.VISIBLE);
//        binding.ibEdit.setVisibility(View.VISIBLE);
    }
    private void showViews() {
        //Show Main views
        binding.ivThumbOcr.setVisibility(View.VISIBLE);
        binding.tvCamera.setVisibility(View.VISIBLE);
        binding.tvGallery.setVisibility(View.VISIBLE);
        binding.viewCenter.setVisibility(View.VISIBLE);


        //Hide Result Views
        binding.etResults.setVisibility(View.GONE);
        binding.closeTexBox.setVisibility(View.GONE);
        binding.ibCopy.setVisibility(View.GONE);
        binding.ibShare.setVisibility(View.GONE);
        binding.ibMaximize.setVisibility(View.GONE);
        binding.ibPreview.setVisibility(View.GONE);
        binding.ibEdit.setVisibility(View.GONE);
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void copyToClipboard(String text){
        if (text.isEmpty())
        {
            showToast("Text not found");
        }
        else {
            try {
                if(android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    showToast("Text Copied");
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setText(text);
                } else {
                    showToast("Text Copied");
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
                    clipboard.setPrimaryClip(clip);
                }
            }
            catch (Exception e)
            {
                Log.d("Exception",e.getMessage());
            }
        }
    }
    private void shareToOthers(String text){
        if (text.isEmpty())
        {
            showToast("Text not found");
        }
        else {
            try {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                /*The type of the content is text, obviously.*/
                intent.setType("text/plain");
                /*Applying information Subject and Body.*/
                intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(intent, getString(R.string.share_using)));
            }
            catch (Exception e)
            {
                Log.e("Error",e.getMessage());
            }
        }

    }
    private void maximizeTextView(String text){
        try {
            final Dialog dialog = new Dialog(this, android.R.style.ThemeOverlay_Material_ActionBar);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.layout_maximize_text);
            // set custom height and width
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView textView=dialog.findViewById(R.id.tvResultsTP);
            ImageButton backButton = dialog.findViewById(R.id.ibBackTP);
            if (!text.isEmpty())
            {
                textView.setMovementMethod(new ScrollingMovementMethod());
                textView.setText(text);
            }

            backButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
        catch (Exception e){
            Log.d("Error",e.getMessage());
        }
    }

    private void ImagePreview(){
        try {
            final Dialog dialog = new Dialog(this, android.R.style.ThemeOverlay_Material_ActionBar);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.layout_image_preview);
            // set custom height and width
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ZoomageView image= dialog.findViewById(R.id.myZoomageView);
            ImageButton backButton = dialog.findViewById(R.id.ibBackIP);

            if (!Uri.EMPTY.equals(imageUri)){
                image.setImageURI(imageUri);
            }
            backButton.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
        catch (Exception e){
            Log.d("Error",e.getMessage());
        }

    }
    private void showProgress() {
        binding.ivProgress.setVisibility(View.VISIBLE);
        binding.ivProgress.setIndeterminate(true);
    }

    private void hideProgress(){
        binding.ivProgress.setVisibility(View.GONE);
        binding.ivProgress.setIndeterminate(false);
    }

}