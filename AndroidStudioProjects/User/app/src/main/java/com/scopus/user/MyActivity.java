package com.scopus.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class MyActivity extends ActionBarActivity {

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
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

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        //EditText editText = (EditText) findViewById(R.id.edit_message);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void makeRequest(View view) throws IOException, JSONException {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        //EditText editText = (EditText) findViewById(R.id.edit_message);
        //String message = editText.getText().toString();


        URL url = new URL("http://192.168.1.48:8080/webservice/rest/assertion/");

        String assertion = null;
        try {
            assertion = new AssertionGetter().execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //Cria o JSON
        /*JSONObject json = new JSONObject();
        String issueInstantStr = "2015-05-12T10:20:04Z";
        String issuerStr = "testSOA";
        try{
            json.put("ID", "BRcitizen");
            json.put("II", issueInstantStr);
            json.put("IS", issuerStr);
            json.put("SK", "");

            JSONObject jsonConditions = new JSONObject();

            jsonConditions.put("NB", "2015-05-12T10:20:04Z");
            jsonConditions.put("NA", "2015-05-13T10:20:04Z");
            JSONObject jsonObligations = new JSONObject();
            jsonObligations.put("OB:", jsonConditions);
            json.put("ST", jsonObligations);
            json.put("ACT", "GET");
            json.put("RES", "coap://ip");
        }
        catch (JSONException e){
            System.out.println("hue");
        }*/

        //Parse JSON
        JSONObject json = new JSONObject(assertion);
        String message = "";
        try {
            message = "ID " + json.get("ID") + "/n";
            message += "II " + json.get("II") + "/n";
            message += "SK " + json.get("SK") + "/n";
            message += "NA " + json.get("NA") + "/n";
            message += "NB " + json.get("NB") + "/n";
        }
        catch(JSONException e){
            System.out.println("hue2");
        }

        //String message = AssertionGetter.get();

                intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);

    }




}
