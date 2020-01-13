package com.example.sunshine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

//    String requested for permissions
    public String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        animation();
        checkNetworkConnection();
    }

    //This method represents the animation on the SplashScreen
    public void animation(){
        LottieAnimationView animationView = findViewById(R.id.splash_animation);
        animationView.setAnimation(R.raw.splash_animation);
    }

   /* This method checks if the device has network connection or not.
    If there is no network connection, than a Toast will pop up.
    Otherwise the App will continue and start the MainActivity*/
    public void checkNetworkConnection(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            checkPermissions();
//            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }else{
            Toast.makeText(this, "Please check your connection.", Toast.LENGTH_SHORT).show();
            Handler hand = new Handler();
            hand.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            },3000);

        }
    }

    //This method is checking if accessing the location is permitted on the device
    private void checkPermissions(){

        if(!hasPermissions(this, permissions)){
            ActivityCompat.requestPermissions(this,permissions, 10);
        }else if (hasPermissions(this, permissions)){
            Handler hand = new Handler();
            hand.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
            },2000);
        }

    }

    private boolean hasPermissions(SplashActivity splashActivity, String[] permissions) {
        if (splashActivity != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(splashActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 10){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Handler hand = new Handler();
                hand.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                }, 2000);
            }else{
                Toast.makeText(this, "Please allow the device location.", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
