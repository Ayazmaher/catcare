package com.app.catcare.ayaz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.app.catcare.ayaz.R;
import com.app.catcare.ayaz.databinding.ActivityJourneyPlannerBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class JourneyPlannerActivity extends AppCompatActivity {
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private int webViewPreviousState;
    private final int PAGE_STARTED = 0x1;
    private final int PAGE_REDIRECTED = 0x2;
    private String directionUrl;

    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";


    private ActivityJourneyPlannerBinding binding;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityJourneyPlannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Marshmallow+ Permission APIs
        callForMarshMallow();


        if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        binding.webView.setInitialScale(1);
        binding.webView.getSettings().setLoadWithOverviewMode(true);
        binding.webView.getSettings().setUseWideViewPort(true);
        binding.webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        binding.webView.setScrollbarFadingEnabled(false);

        binding.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        binding.webView.getSettings().setBuiltInZoomControls(true);
        binding.webView.setWebViewClient(new GeoWebViewClient());
        // Below required for geolocation
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setGeolocationEnabled(true);
        binding.webView.setWebChromeClient(new GeoWebChromeClient());
        binding.webView.getSettings().setDomStorageEnabled(true);


        binding.webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        binding.webView.getSettings().setDatabaseEnabled(true);
        binding.webView.getSettings().setDomStorageEnabled(true);

        binding.webView.getSettings().setGeolocationDatabasePath(getFilesDir().getPath());

        binding.webView.loadUrl("https://www.google.com/maps");

    }
    public class GeoWebChromeClient extends android.webkit.WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin,
                                                       final GeolocationPermissions.Callback callback) {
            // Always grant permission since the app itself requires location
            // permission and the user has therefore already granted it
            callback.invoke(origin, true, false);
        }
    }


    public class GeoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("https://maps.app.goo.gl/"))
            {
                callForGoogleAppDialog(url);
            }
            else {
                view.loadUrl(url);
            }
            Log.d("MAP",url);
            Log.d("MAP","ORG: "+view.getOriginalUrl());
            Log.d("MAP","URL: "+view.getUrl());
            return true;
        }

        Dialog loadingDialog = new Dialog(JourneyPlannerActivity.this);
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            webViewPreviousState = PAGE_STARTED;
            if (loadingDialog == null || !loadingDialog.isShowing())
                loadingDialog = ProgressDialog.show(JourneyPlannerActivity.this, "",
                        "Loading Please Wait", true, true,
                        dialog -> {

                        });
            loadingDialog.setCancelable(false);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            if (isConnected()) {
                final Snackbar snackBar = Snackbar.make(binding.rootView, "onReceivedError : " + error.getDescription(), Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction("Reload", view1 -> binding.webView.loadUrl("javascript:window.location.reload( true )"));
                snackBar.show();

            } else {
                final Snackbar snackBar = Snackbar.make(binding.rootView, "No Internet Connection ", Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction("Enable Data", view12 -> {
                    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), 0);
                    binding.webView.loadUrl("javascript:window.location.reload( true )");
                    snackBar.dismiss();
                });
                snackBar.show();
            }
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (isConnected()) {
                final Snackbar snackBar = Snackbar.make(binding.rootView, "HttpError : " + errorResponse.getReasonPhrase(), Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction("Reload", view1 -> binding.webView.loadUrl("javascript:window.location.reload( true )"));
                snackBar.show();
            } else {
                final Snackbar snackBar = Snackbar.make(binding.rootView, "No Internet Connection ", Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction("Enable Data", view12 -> {
                    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), 0);
                    binding.webView.loadUrl("javascript:window.location.reload( true )");
                    snackBar.dismiss();
                });
                snackBar.show();
            }
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (webViewPreviousState == PAGE_STARTED) {
                if (null != loadingDialog) {
                    loadingDialog.dismiss();
                    loadingDialog = null;
                }
            }
        }
    }

    private void callForGoogleAppDialog(String url) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.layout_direction_map);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.findViewById(R.id.btnOpenAppGoogle).setOnClickListener(view1->{

            try {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Log.d("Error to load on App", e.getMessage());
                Toast.makeText(JourneyPlannerActivity.this, "APP NOT FOUND", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });
        dialog.show();
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != cm) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return (info != null && info.isConnected());
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

                ) {
                    Toast.makeText(JourneyPlannerActivity.this, "All Permission GRANTED !! Thank You :)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(JourneyPlannerActivity.this, "One or More Permissions are DENIED Exiting App :(", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    private void callForMarshMallow() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList))
            permissionsNeeded.add("Show Location");
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                StringBuilder message = new StringBuilder("App need access to " + permissionsNeeded.get(0));
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message.append(", ").append(permissionsNeeded.get(i));
                showMessageOKCancel(message.toString(),
                        (dialog, which) -> requestPermissions(permissionsList.toArray(new String[0]),
                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS));
                return;
            }
            requestPermissions(permissionsList.toArray(new String[0]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(JourneyPlannerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    private boolean addPermission(List<String> permissionsList) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            return shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}