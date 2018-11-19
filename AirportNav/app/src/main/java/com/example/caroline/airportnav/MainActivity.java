package com.example.caroline.airportnav;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.caroline.airportnav.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText flightNumber;
    private TextView flightDetails;
    private String number;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        flightNumber = (EditText) findViewById(R.id.flight_number);
        flightDetails = (TextView) findViewById(R.id.flight_details);


        flightNumber.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_DONE){
                    number = v.getText().toString();
                    getFlightDetails(number);
                    flightDetails.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });




    }

    private void getFlightDetails(String number) {
        URL timetableURL = NetworkUtils.buildUrlForTimeTable();
        new FetchTimeTableTask().execute(timetableURL);
    }

    public class FetchTimeTableTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            String githubSearchResults = null;
            try {
                githubSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return githubSearchResults;
        }

        @Override
        protected void onPostExecute(String timetableSearchResults) {
            String flight_details = "";
            if (timetableSearchResults != null && !timetableSearchResults.equals("")) {
                try{
                    JSONArray timetable = new JSONArray(timetableSearchResults);
                    for(int i =0;i<timetable.length();i++){
                        Log.d("HEy!", "onPostExecute: timetable retrieves, our flight is"+number);
                        JSONObject table = timetable.getJSONObject(i);
                        JSONObject flight = table.getJSONObject("flight");
                        String flight_number = flight.getString("number");
                        Log.d("HEy2 !", "onPostExecute: flight retrieved"+flight_number);
                        if(flight_number.equals(number)){
                            Log.d("HEy 3!", "onPostExecute: flight matched");
                            JSONObject departure = table.getJSONObject("departure");

                            flight_details = "Terminal: "+departure.getString("terminal")+"\n Gate:"+departure.getString("gate")+"\n Time:"+departure.getString("scheduledTime");
                            break;
                        }else{
                            continue;
                        }

                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
                flightDetails.setText(flight_details);
            } else {
                flightDetails.setText("Error fetching results");
            }
        }
    }

}
