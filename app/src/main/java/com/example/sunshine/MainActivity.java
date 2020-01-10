package com.example.sunshine;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    LottieAnimationView lottieWeather, lottieDress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lottieWeather = findViewById(R.id.weather_animation_view);
        lottieDress = findViewById(R.id.dress_animation_view);
        lottieWeather.setAnimation(R.raw.weather_clear_sky_night);
        lottieDress.setAnimation(R.raw.try1);
    }
}
