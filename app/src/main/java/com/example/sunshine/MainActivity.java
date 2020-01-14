package com.example.sunshine;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.sunshine.Common.Common;
import com.example.sunshine.Retrofit.OpenWeatherMapAPI;
import com.example.sunshine.Retrofit.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    LottieAnimationView lottieWeather, lottieDress;
    MaterialButton moreInfoM, editDressCodeM;
    TextView currentTemperatureM, currentLocationM, dressCodeAdviceM;
    ChipGroup dressCodeOptionsM;
    ConstraintLayout background;
    CompositeDisposable compositeDisposable;

    //This declaration is for the Interface
    private OpenWeatherMapAPI openWeatherMapAPI;


    
    private static  final int REQUEST_LOCATION=1;
    private LocationManager locationManager;
    private String longitude, latitude;
//    private String sunRise, sunSet;
    private boolean isNight = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        findUI();
        getLocation();

        moreInfoM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, WeatherActivity.class));
            }
        });


    }

    /*This method will find all the UI items: TextView, LottieAnimationView and so on.
    (findViewByIds)*/
    private void findUI() {

        lottieDress = findViewById(R.id.dressAnimationView);
        lottieWeather = findViewById(R.id.weatherAnimationView);
        moreInfoM = findViewById(R.id.moreInfo);
        editDressCodeM = findViewById(R.id.editDressCode);
        currentTemperatureM = findViewById(R.id.currentTemperature);
        currentLocationM = findViewById(R.id.currentLocation);
        dressCodeAdviceM = findViewById(R.id.dressCodeAdvice);
        dressCodeOptionsM = findViewById(R.id.dressCodeOptions);
        background = findViewById(R.id.background);

    }

    /* This method will get the latitude and the longitude for the weather queries */
    private void getLocation() {

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Please turn on the location.")
                    .setPositiveButton("OK", (paramDialogInterface, paramInt) ->
                            MainActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Cancel", null).show();
        }else {

            if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }else{
                Location locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Location locationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                if (locationGps != null){
                    double lat = locationGps.getLatitude();
                    double lon = locationGps.getLongitude();

                    latitude = String.valueOf(lat);
                    longitude = String.valueOf(lon);

                } else if(locationNetwork != null){

                    double lat = locationNetwork.getLatitude();
                    double lon = locationNetwork.getLongitude();

                    latitude = String.valueOf(lat);
                    longitude = String.valueOf(lon);

                }else {
                    double lat = locationPassive.getLatitude();
                    double lon = locationPassive.getLongitude();

                    latitude = String.valueOf(lat);
                    longitude = String.valueOf(lon);

                }

            }
            getWeatherInfo();
        }
    }

    /*This method will get the weather information from the OpenWeather website
     *and will show it on the UI*/
    private void getWeatherInfo() {

        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        openWeatherMapAPI = retrofit.create(OpenWeatherMapAPI.class);

        compositeDisposable.add(openWeatherMapAPI.getWeatherByLatLon(latitude,longitude,
                        Common.APP_ID, "metric")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(weatherResult -> {
                            currentTemperatureM.setText(new StringBuilder(
                                    String.valueOf(weatherResult.getMain().getTemp())).append(" °C").toString());
                            currentLocationM.setText(weatherResult.getName());
                            String sunRise = Common.convertUnixToHour(weatherResult.getSys().getSunrise());
                            String sunSet = Common.convertUnixToHour(weatherResult.getSys().getSunset());
                            String weatherDescription = weatherResult.getWeather().get(0).getMain();

                            DayOrNight(sunSet, sunRise);
                            setAnimation(weatherDescription);
                            setBackground(weatherDescription);
                            addChips(weatherDescription);

                        }, throwable -> throwable.printStackTrace()));
    }

    /*This method will help us to find out if it's day time or night time*/
    private void DayOrNight(String sunSet, String sunRise){


        long yourmilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); //Play with the parameters to get different results
        String formattedActualDate = sdf.format(yourmilliseconds);

        String t = String.valueOf(MsConverter(formattedActualDate));

        String text = "Current: "+  t + "Rise: " + MsConverter(sunRise)
                + "Set: " + MsConverter(sunSet);

        if(MsConverter(formattedActualDate) > MsConverter(sunSet)){
            isNight = true;
        }else if(System.currentTimeMillis() > MsConverter(sunRise)){
            isNight = false;
        }
        dressCodeAdviceM.setTextColor(Color.BLACK);
        dressCodeAdviceM.setText(text);
    }

    /*This method will convert the epoch timestamp into milliseconds*/
    private long MsConverter(String sun){

        String[] tokens = sun.split(":");
        int secondsToMs = Integer.parseInt(tokens[2]) * 1000;
        int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
        int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;

        return (long) (secondsToMs + minutesToMs + hoursToMs);
    }

    /*This method will ensure that the background design is synced with the weather condition*/
    private void setBackground(String weatherDescription){


        if(isNight){
            switch (weatherDescription.toLowerCase()){
                case "clear":
                    background.setBackgroundResource(R.drawable.gradient_clear_sky_night);
                    break;
                case "clouds":
                    background.setBackgroundResource(R.drawable.gradient_couldy_night);
                    break;
                case "mist":
                    background.setBackgroundResource(R.drawable.gradient_mist_night);
                    break;
                case "fog":
                    background.setBackgroundResource(R.drawable.gradient_foggy_night);
                    break;
                case "snow":
                    background.setBackgroundResource(R.drawable.gradient_snow_night);
                    break;
                case "rain":
                    background.setBackgroundResource(R.drawable.gradient_rainy_night);
                    break;
                case "drizzle":
                    background.setBackgroundResource(R.drawable.gradient_party_cloudy_night);
                    break;
                case "thunderstorm":
                    background.setBackgroundResource(R.drawable.gradient_thunder_night);
                    break;
            }
        }else{
            switch (weatherDescription.toLowerCase()){
                case "clear":
                    background.setBackgroundResource(R.drawable.gradient_sunny);
                    break;
                case "clouds":
                    background.setBackgroundResource(R.drawable.gradient_cloudy);
                    break;
                case "mist":
                    background.setBackgroundResource(R.drawable.gradient_mist);
                    break;
                case "fog":
                    background.setBackgroundResource(R.drawable.grafient_foggy);
                    break;
                case "snow":
                    background.setBackgroundResource(R.drawable.gradient_snow_sun);
                    break;
                case "Rain":
                    background.setBackgroundResource(R.drawable.gradient_storm);
                    break;
                case "drizzle":
                    background.setBackgroundResource(R.drawable.gradient_party_cloudy);
                    break;
                case "thunderstorm":
                    background.setBackgroundResource(R.drawable.gradient_thunder);
                    break;
            }
        }
    }

    /*This method will check what is the weather condition and
    * will use representative animations for it. For example: If it's raining, then raining animation.*/
    private void setAnimation(String weatherDescription){

        if(isNight){
            switch (weatherDescription.toLowerCase()){
                case "clear":
                    lottieWeather.setAnimation(R.raw.weather_clear_sky_night);
                    break;
                case "clouds":
                    lottieWeather.setAnimation(R.raw.weather_cloudy_night);
                    break;
                case "snow":
                    lottieWeather.setAnimation(R.raw.weather_snow_night);
                    break;
                case "rain":
                    lottieWeather.setAnimation(R.raw.weather_rainy_night);
                    break;
            }
        }else{
            switch (weatherDescription){
                case "clear":
                    lottieWeather.setAnimation(R.raw.weather_sunny);
                    break;
                case "clouds":
                    lottieWeather.setAnimation(R.raw.weather_cloudy);
                    break;
                case "mist":
                    lottieWeather.setAnimation(R.raw.weather_mist);
                    break;
                case "fog":
                    lottieWeather.setAnimation(R.raw.weather_foggy);
                    break;
                case "snow":
                    lottieWeather.setAnimation(R.raw.weather_snow);
                    break;
                case "Rain":
                    lottieWeather.setAnimation(R.raw.weather_rainy_night);
                    break;
                case "drizzle":
                    lottieWeather.setAnimation(R.raw.weather_partly_shower);
                    break;
                case "thunderstorm":
                    lottieWeather.setAnimation(R.raw.weather_thunder);
                    break;
            }
        }

        lottieDress.setAnimation(R.raw.dress_code);
    }

    private void addChips(String weatherDescription){

        if(isNight){
            switch (weatherDescription.toLowerCase()){
                case "clear":
                    lottieWeather.setAnimation(R.raw.weather_clear_sky_night);
                    break;
                case "clouds":
                    lottieWeather.setAnimation(R.raw.weather_cloudy_night);
                    break;
                case "snow":
                    lottieWeather.setAnimation(R.raw.weather_snow_night);
                    break;
                case "rain":
                    lottieWeather.setAnimation(R.raw.weather_rainy_night);
                    break;
            }
        }else{
            switch (weatherDescription){
                case "clear":
                    lottieWeather.setAnimation(R.raw.weather_sunny);
                    break;
                case "clouds":
                    lottieWeather.setAnimation(R.raw.weather_cloudy);
                    break;
                case "mist":
                    lottieWeather.setAnimation(R.raw.weather_mist);
                    break;
                case "fog":
                    lottieWeather.setAnimation(R.raw.weather_foggy);
                    break;
                case "snow":
                    lottieWeather.setAnimation(R.raw.weather_snow);
                    break;
                case "Rain":
                    lottieWeather.setAnimation(R.raw.weather_rainy_night);
                    break;
                case "drizzle":
                    lottieWeather.setAnimation(R.raw.weather_partly_shower);
                    break;
                case "thunderstorm":
                    lottieWeather.setAnimation(R.raw.weather_thunder);
                    break;
            }
        }

        String[] cloth = {"Raincoat", "Umbrella", "Blazer", "Jeans","Sneakers","Boots"};


        for(String cloths : cloth){
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            Chip chipM = (Chip)inflater.inflate(R.layout.dress_code_chip, null, false);
            chipM.setOnCloseIconClickListener(v -> dressCodeOptionsM.removeView(v));
            chipM.setText(cloths);

//            dressCodeOptionsM.addView(setUpChip(chipM));
                    dressCodeOptionsM.addView(chipM);

        }



//        for(int i=0; i<dressCodeOptionsM.getChildCount();i++){
//            Chip chip = (Chip) dressCodeOptionsM.getChildAt(i);
//            if(chip.isChecked()){
//                if(i < dressCodeOptionsM.getChildCount()-1){
//
//                }
//            }
//        }



    }

//    private Chip setUpChip(Chip chip){
//
//
//    }
}
