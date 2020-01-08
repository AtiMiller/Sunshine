package com.example.sunshine;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sunshine.Adapters.WeatherForecastAdapter;
import com.example.sunshine.Common.Common;
import com.example.sunshine.Model.WeatherForecastResult;
import com.example.sunshine.Model.WeatherResult;
import com.example.sunshine.Retrofit.IOpenWeatherMap;
import com.example.sunshine.Retrofit.RetrofitClient;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastWeatherFragment extends Fragment {

    CompositeDisposable compositeDisposable;
    IOpenWeatherMap mService;

    TextView txt_city_name;
    RecyclerView forecast_recycler;


    static ForecastWeatherFragment instance;

    public static ForecastWeatherFragment getInstance(){
        if(instance == null)
            instance = new ForecastWeatherFragment();
        return instance;
    }


    public ForecastWeatherFragment() {
       compositeDisposable = new CompositeDisposable();
       Retrofit retrofit = RetrofitClient.getInstance();
       mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_forecast_weather, container, false);

        txt_city_name = rootView.findViewById(R.id.txt_city_name_forecast);
        forecast_recycler = rootView.findViewById(R.id.forecats_recycler);
        forecast_recycler.setHasFixedSize(true);
        forecast_recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        getForecastWeatherInfo();

        return rootView;
    }

    private void getForecastWeatherInfo() {

        compositeDisposable.add(mService.getWeatherForecastByLatLon(
                String.valueOf(Common.current_location.getLatitude()),
                String.valueOf(Common.current_location.getLongitude()),
                Common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherForecastResult>() {
                               @Override
                               public void accept(WeatherForecastResult weatherForecastResult) throws Exception {
                                   displayForecastWeather(weatherForecastResult);
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                Log.d("ERROR", throwable.getMessage());
                               }
                           }
                ));
    };

    private void displayForecastWeather(WeatherForecastResult weatherForecastResult) {
        txt_city_name.setText(new StringBuilder(weatherForecastResult.city.name));

        WeatherForecastAdapter adapter = new WeatherForecastAdapter(getContext(), weatherForecastResult);
        forecast_recycler.setAdapter(adapter);
    }

}
