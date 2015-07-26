package com.scopus.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by Daniel on 01/05/2015.
 */
public class AssertionSender extends AsyncTask<URI, Void, String> {
    //private String message="";
    //public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    public Context context;
    public String assertion;

    public AssertionSender(Context context, String assertion){

        this.context = context.getApplicationContext();
        this.assertion = assertion;
    }

    protected String doInBackground(URI... uris) {

        URI uri = uris[0];

        String message = "";

        System.out.println("ASYNCHRONOUS");

        CoapClient client = new CoapClient("coap://192.168.1.241:5683/watch");


        Request request = new Request(CoAP.Code.GET);
        request.setPayload("Plain text");
        OptionSet options = request.getOptions();
        options.addOption(new Option(42, this.assertion));
        request.setOptions(options);
        request.setURI("coap://192.168.1.241:5683/watch");
        try {
            String response = request.send().waitForResponse().getPayloadString();
            return response;
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return null;
    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    protected void onPostExecute(String message){
        //this.myActivity.startActivity(new Intent());
        System.err.println(message);
        //Intent intent = new Intent(this.context, DisplayMessageActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //context.startActivity(intent);
        //return message;
    }
}
