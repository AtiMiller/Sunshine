package com.example.sunshine;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.sunshine.Common.Common;
import com.example.sunshine.Model.WeatherResult;
import com.example.sunshine.Retrofit.IOpenWeatherMap;
import com.example.sunshine.Retrofit.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    LottieAnimationView lottieWeather, lottieDress;
    MaterialButton moreInfoM, editDressCodeM;
    TextView currentTemperatureM, currentLocationM, dressCodeAdviceM;
    ChipGroup dressCodeOptionsM;
    RelativeLayout weatherConditionM, dressCodeM;
    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;
    
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
        weatherConditionM = findViewById(R.id.weatherConditionLay);
        dressCodeM = findViewById(R.id.dressCodeLay);

    }

    /* This method will get the latitude and the longitude for the weather queries */
    private void getLocation() {

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Please turn on the location.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            MainActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    }).setNegativeButton("Cancel", null).show();
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
        mService = retrofit.create(IOpenWeatherMap.class);

        compositeDisposable.add(mService.getWeatherByLatLon(latitude,longitude,
                        Common.APP_ID,
                        "metric")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<WeatherResult>() {
                            @Override
                            public void accept(WeatherResult weatherResult) throws Exception {
                                currentTemperatureM.setText(new StringBuilder(
                                        String.valueOf(weatherResult.getMain().getTemp())).append(" °C").toString());
                                currentLocationM.setText(weatherResult.getName());
                                String sunRise = Common.convertUnixToHour(weatherResult.getSys().getSunrise());
                                String sunSet = Common.convertUnixToHour(weatherResult.getSys().getSunrise());
                                String weatherDescription = weatherResult.getWeather().get(0).getMain();

                                DayOrNight(sunSet, sunRise);
                                setAnimation(weatherDescription);

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {

                            }
                        }));
    }

    /*This method will help us to find out if it's day time or night time*/
    private void DayOrNight(String sunSet, String sunRise){

        if(System.currentTimeMillis() > MsConverter(sunRise) &&
                System.currentTimeMillis() < MsConverter(sunSet)){
            isNight = false;
        }else if(System.currentTimeMillis() > MsConverter(sunSet)){
            isNight = true;
        }

    }

    /*This method will convert the epoch timestamp into milliseconds*/
    private long MsConverter(String sun){

        String[] tokens = sun.split(":");
        int secondsToMs = Integer.parseInt(tokens[2]) * 1000;
        int minutesToMs = Integer.parseInt(tokens[1]) * 60000;
        int hoursToMs = Integer.parseInt(tokens[0]) * 3600000;
        long sunMs = secondsToMs + minutesToMs + hoursToMs;

        return sunMs;
    }

    private void setBackground(){

    }


    /*This method will check what is the weather condition and
    * will use representative animations for it. For example: If it's raining, then raining animation.*/
    private void setAnimation(String weatherDescription){

        Toast.makeText(this, ""+weatherDescription, Toast.LENGTH_SHORT).show();

        if(isNight){
            switch (weatherDescription){
                case "clear":
                    lottieWeather.setAnimation(R.raw.weather_clear_sky_night);
                    break;
                case "clouds":
                    lottieWeather.setAnimation(R.raw.weather_cloudy_night);
                    break;
            }
        }else{
            switch (weatherDescription){
                case:
            }
        }

        lottieDress.setAnimation(R.raw.try1);
    }


}
