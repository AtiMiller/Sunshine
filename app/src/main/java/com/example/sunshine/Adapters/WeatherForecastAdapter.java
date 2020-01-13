package com.example.sunshine.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sunshine.Common.Common;
import com.example.sunshine.Model.WeatherForecastResult;
import com.example.sunshine.R;
import com.squareup.picasso.Picasso;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.MyViewHolder> {

    Context context;
    WeatherForecastResult weatherForecastResult;

    public WeatherForecastAdapter(Context context, WeatherForecastResult weatherForecastResult) {
        this.context = context;
        this.weatherForecastResult = weatherForecastResult;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.weather_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                .append(weatherForecastResult.list.get(position).weather.get(0).getIcon())
                .append(".png").toString()).into(holder.weather_image);

        holder.txt_date_time.setText(new StringBuilder(Common.convertUnixToDate(weatherForecastResult.list.get(position).dt)));
        holder.weatherDescriptionM.setText(new StringBuilder(weatherForecastResult.list.get(position).weather.get(0).getDescription()));
        holder.txt_temperature.setText(new StringBuilder(String.valueOf(weatherForecastResult.list.get(position).main.getTemp())).append("°C"));

    }

    @Override
    public int getItemCount() {
        return weatherForecastResult.list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txt_date_time, weatherDescriptionM, txt_temperature;
        ImageView weather_image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_date_time = itemView.findViewById(R.id.txt_data_time_forecast);
            weatherDescriptionM = itemView.findViewById(R.id.weatherDescription);
            txt_temperature = itemView.findViewById(R.id.txt_temperature_forecast);
            weather_image = itemView.findViewById(R.id.img_weather_forecast);
        }
    }
}
