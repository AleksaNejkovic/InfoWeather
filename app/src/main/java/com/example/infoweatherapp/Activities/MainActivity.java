package com.example.infoweatherapp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.infoweatherapp.Class.Weather;
import com.example.infoweatherapp.R;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean geolocationQueryAllowed = false;

    private LayoutInflater layoutInflater;
    private List<Weather> weatherList;
    private TextView address;
    private TextView countryAndCity;
    private Integer curHour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Date now = new Date();
        curHour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(now));

        address = (TextView) findViewById(R.id.address);
        countryAndCity = (TextView) findViewById(R.id.countryAndCity);

        String msg = null;
        if (curHour >= 1 && curHour <= 12) {
            msg = "Dobro jutro!";
            Toast toast = Toast.makeText(getApplicationContext(), "Dobro jutro, dan je.", Toast.LENGTH_SHORT);
            toast.show();
        } else if (curHour > 12 && curHour <= 18) {
            msg = "Dobar dan!";
            Toast toast = Toast.makeText(getApplicationContext(), "Dobar dan, dan je", Toast.LENGTH_SHORT);
            toast.show();
        } else if (curHour > 18 && curHour <= 21) {
            msg = "Dobro veče!";
            Toast toast = Toast.makeText(getApplicationContext(), "Dobro veče, noć je", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            msg = "Laku noć!";
            Toast toast = Toast.makeText(getApplicationContext(), "Laku noć, noć je", Toast.LENGTH_SHORT);
            toast.show();
        }

        setTimeOfDayData(now, msg);

        getCurrentLocationWithWeatherData();
    }

    private void setTimeOfDayData(Date now, String msg) {
        TextView userMessage = findViewById(R.id.userMessage);
        TextView currentTime = findViewById(R.id.currentTime);
        TextView currentDate = findViewById(R.id.currentDate);
        TextView partOfTheDay = findViewById(R.id.partOfTheDay);
        LinearLayout linearLayout = findViewById(R.id.mainLayout);
        ListView weatherBackground = findViewById(R.id.weather_list);

        String curTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now);
        String curDate = new SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault()).format(now);
        currentTime.setText(curTime);
        currentDate.setText(curDate);
        userMessage.setText(msg);

        int bgColor = ContextCompat.getColor(this, isDay() ? R.color.dayMode : R.color.nightMode);
        int txtColor = getResources().getColor(isDay() ? R.color.nightMode : R.color.white);

        partOfTheDay.setText(isDay() ? "Dan" : "Noć");
        linearLayout.setBackgroundColor(bgColor);
        currentDate.setTextColor(txtColor);
        currentTime.setTextColor(txtColor);
        partOfTheDay.setTextColor(txtColor);
        userMessage.setTextColor(txtColor);
        address.setTextColor(txtColor);
        countryAndCity.setTextColor(txtColor);
        weatherBackground.setBackgroundColor(bgColor);
        weatherBackground.setDivider(new ColorDrawable(Color.parseColor(isDay() ? "#0B112E" : "#FFFFFF")));
        weatherBackground.setDividerHeight(1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE
            && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                geolocationQueryAllowed = true;
        }
    }

    private void getCurrentLocationWithWeatherData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
        else{
            geolocationQueryAllowed=true;
        }
        if (!geolocationQueryAllowed)
            return;

        LocationServices
            .getFusedLocationProviderClient(this)
            .getLastLocation()
            .addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        try {
                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            List<Address> locations = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (locations.size() > 0)
                                loadData(locations.get(0));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    }

    private void loadData(Address location) {
        countryAndCity.setText(location.getCountryName() + "\n" + location.getLocality());
        address.setText(location.getAddressLine(0));

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "https://api.weatherapi.com/v1/forecast.json?key=f9aef5e97c044ef795d135112210602&q="+location.getLocality()+"&days=1&lang=sr";
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                SimpleDateFormat formatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                try {
                    weatherList = new ArrayList<>();
                    JSONArray forecastDay = response.getJSONObject("forecast").getJSONArray("forecastday");
                    for (int i = 0; i < forecastDay.length(); i++) {

                        JSONArray hourlyData = forecastDay.getJSONObject(i).getJSONArray("hour");
                        for (int j = 0; j < hourlyData.length(); j++) {
                            Date timeDate = formatDateTime.parse(hourlyData.getJSONObject(j).getString("time"));
                            Double temp_c = hourlyData.getJSONObject(j).getDouble("temp_c");
                            JSONObject condition = hourlyData.getJSONObject(j).getJSONObject("condition");
                            String text = condition.getString("text");
                            String icon = condition.getString("icon");
                            weatherList.add(new Weather(timeDate, text, temp_c, icon));
                        }
                    }
                    displayWeatherData();
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonRequest);
    }

    private void displayWeatherData() {
        WeatherListAdapter adapter = new WeatherListAdapter(getApplicationContext(), weatherList);
        ListView listView = findViewById(R.id.weather_list);
        listView.setAdapter(adapter);
    }

    private boolean isDay() { return curHour >= 1 && curHour < 18; }

    class WeatherListAdapter extends ArrayAdapter<Weather> {
        List<Weather> weatherList;

        WeatherListAdapter(Context context, List<Weather> weatherList) {
            super(context, R.layout.row_in_weather_lists, R.id.timeNow, weatherList);
            this.weatherList = weatherList;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = layoutInflater.inflate(R.layout.row_in_weather_lists, parent, false);
            TextView timeNow = row.findViewById(R.id.timeNow);
            TextView dateNow = row.findViewById(R.id.dateNow);
            TextView maxTemp = row.findViewById(R.id.maxTemp);
            ImageView weatherIcon = row.findViewById(R.id.weatherIcon);
            TextView weatherDesc = row.findViewById(R.id.weatherDesc);

            Weather i = weatherList.get(position);

            SimpleDateFormat fmd = new SimpleDateFormat("dd.MM.yy");
            SimpleDateFormat fmt = new SimpleDateFormat("HH");


            dateNow.setText(fmd.format(i.getDateTime()));
            timeNow.setText(fmt.format(i.getDateTime()) + "h");
            Picasso.get().load("https:" + i.getIcon()).resize(50, 50).centerCrop().into(weatherIcon);
            maxTemp.setText(String.valueOf(i.getTemperature()) + " °C");
            weatherDesc.setText(i.getIconPhrase());

            if (curHour >= 5 && curHour < 18) {
                dateNow.setTextColor(getResources().getColor(R.color.black));
                maxTemp.setTextColor(getResources().getColor(R.color.black));
            } else {
                dateNow.setTextColor(getResources().getColor(R.color.white));
                maxTemp.setTextColor(getResources().getColor(R.color.white));
            }

            return row;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}