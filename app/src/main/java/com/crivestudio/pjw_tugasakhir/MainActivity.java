package com.crivestudio.pjw_tugasakhir;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import java.util.*;


public class MainActivity extends ActionBarActivity
{

    String apiKey = "AIzaSyA5csgc61wsvMf5QBumMTzEZLZ9pavLPlA";
    List<String> spinnerArray = null;
    ArrayAdapter<String> customAdapter = null;
    Spinner spinnerHasil;
    TextView debugTextView, prayerTextView;
    JSONArray results; //taruh di global, biar bisa diakses pas nganu :3
    Date dateNow;
    EditText inputTextEdit;
    View hasilws;
    View hasilws2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateNow = new Date();

        spinnerHasil = (Spinner)findViewById(R.id.SpinnerHasil);
        debugTextView = (TextView)findViewById(R.id.debugText);
        prayerTextView = (TextView)findViewById(R.id.prayerTimeTxt);
        inputTextEdit = (EditText)findViewById(R.id.inputText);
        spinnerArray = new ArrayList<>();
        hasilws = (View) findViewById(R.id.hasilws1);
        hasilws.setVisibility(View.INVISIBLE);
        hasilws2 = (View) findViewById(R.id.hasilws2);
        hasilws2.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public String readJSONFeed(String URL) {
        StringBuilder stringBuilder = new StringBuilder();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(URL);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
            } else {
                Log.d("JSON", "Failed to download file");
            }
        } catch (Exception e) {
            Log.d("readJSONFeed", e.getLocalizedMessage());
        }
        return stringBuilder.toString();
    }

    private class FindCityByGoogleMaps extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... urls) {
            return readJSONFeed(urls[0]);
        }


        protected void onPostExecute(String result)
        {
            try
            {
                spinnerArray.clear();
                
                JSONObject jsonObj = new JSONObject(result);
                results = jsonObj.getJSONArray("results");

                customAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, spinnerArray);
                customAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                for(int i=0; i<results.length(); i++)
                {
                    JSONObject r = results.getJSONObject(i);
                    spinnerArray.add(r.getString("formatted_address"));
                }
                spinnerHasil.setAdapter(customAdapter);
            }
            catch (Exception e)
            {
                Log.d("FindCityByGoogleMaps", e.getLocalizedMessage());
            }
        }
    }

    private class GetPrayerTime extends AsyncTask<String, Void, String>
    {

        protected String doInBackground(String... urls)
        {
            return readJSONFeed(urls[0]);
        }


        protected void onPostExecute(String result)
        {
            try
            {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject temp = jsonObj.getJSONObject(Integer.toString(dateNow.getDate()));

                prayerTextView.setText("Imsyak = " + temp.getString("Imsaak") + "\t\t" +
                                "Terbit = " + temp.getString("Sunrise") + "\n" +
                                "Dzuhur = " + temp.getString("Dhuhr") + "\t\t" +
                                "Ashar = " + temp.getString("Asr") + "\n" +
                                "Terbenam = " + temp.getString("Sunset") + "\t\t" +
                                "Maghrib = " + temp.getString("Maghrib") + "\n" +
                                "Isya' = " + temp.getString("Isha")
                );

            }
            catch (Exception e)
            {
                Log.d("GetPrayerTime", e.getLocalizedMessage());
            }
        }
    }


    private class GetTempatSekitar extends AsyncTask<String, Void, String>
    {

        protected String doInBackground(String... urls)
        {
            return readJSONFeed(urls[0]);
        }


        protected void onPostExecute(String result)
        {
            try
            {
                String hasil = "";

                JSONObject jsonObj = new JSONObject(result);
                JSONArray geonames = jsonObj.getJSONArray("geonames");

                for(int i=0; i<geonames.length(); i++)
                {
                    JSONObject r = geonames.getJSONObject(i);
                    hasil=hasil+Integer.toString(i+1)+". "+r.getString("title")+" ("+r.getString("lat")+", "+r.getString("lng")+")\n"+
                            r.getString("summary")+"\n\n\n";
                }
                debugTextView.setText(hasil);
            }
            catch (Exception e)
            {
                Log.d("GetTempatSekitar", e.getLocalizedMessage());
            }
        }
    }


    public void btnFindCity(View view)
    {
        hasilws.setVisibility(View.VISIBLE);

        String temp = "";
        temp = inputTextEdit.getText().toString();

        if(!connectionAvailable())
        {
            Toast.makeText(getApplicationContext(),"Periksa koneksi internet anda",Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(temp.matches(""))
            {
                Toast.makeText(getApplicationContext(),"Masikkan lokasi anda",Toast.LENGTH_SHORT).show();
            }
            else
            {
                new FindCityByGoogleMaps().execute(
                        "https://maps.googleapis.com/maps/api/geocode/json?address="+inputTextEdit.getText().toString()
                                +"&key=" + apiKey);
            }
        }



    }

    public void btnGetLain(View view)
    {

        spinnerHasil = (Spinner)findViewById(R.id.SpinnerHasil);

        if(spinnerHasil.getSelectedItemPosition()!=-1)
        {
            JSONObject r;
            try
            {
                r = results.getJSONObject(spinnerHasil.getSelectedItemPosition());
                JSONObject temp = r.getJSONObject("geometry");
                r = temp.getJSONObject("location");

                new GetPrayerTime().execute(
                        "http://praytime.info/getprayertimes.php?lat="+r.getString("lat")+"&lon="+r.getString("lng")+"&gmt=420&m="+
                                Integer.toString(dateNow.getMonth() + 1)+"&y="+Integer.toString(dateNow.getYear()+1900)
                        );

                new GetTempatSekitar().execute(
                        "http://api.geonames.org/findNearbyWikipediaJSON?formatted=true&lat="+r.getString("lat")+"&lng="+
                                r.getString("lng")+"&username=thoriq&style=full"
                );

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean connectionAvailable()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(MainActivity.this.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (null == ni) return false;
        return ni.isConnectedOrConnecting();
    }
}
