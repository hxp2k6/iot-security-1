package com.scopus.user;

import android.app.Activity;
import android.os.AsyncTask;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Created by Daniel on 01/05/2015.
 */
public class AssertionSender extends AsyncTask<URI, Void, String> {
    //private String message="";

    public DisplayMessageActivity displayMessageActivity;

    public AssertionSender(DisplayMessageActivity activity){
        this.displayMessageActivity = activity;
    }

    protected String doInBackground(URI... uris) {

        URI uri = uris[0];
        /*CoapClient client = new CoapClient("coap://129.132.15.80/");
        CoapResponse response = client.get();
        if (response != null) {
            message = response.getResponseText();
        }
        else {
            message = "Esse get ta null";
        }

        if (response!=null) {
            message += "Foi";
            System.out.println(response.getCode());
            System.out.println(response.getOptions());
            System.out.println(response.getResponseText());

            System.out.println("\nADVANCED\n");
            // access advanced API with access to more details through .advanced()
            System.out.println(Utils.prettyPrint(response));

        } else {
            message += "Null response";
            System.out.println("No response received.");
        }*/

        CoapClient client = new CoapClient("coap://129.132.15.80/");
        String message = "";

        System.out.println("ASYNCHRONOUS");

        MyCoapHandler handler = new MyCoapHandler();
        client.get(handler);
        message += handler.getResponseMessage();
        /*client.get(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                System.out.println("RESPONSE 3: " + content);
            }

            @Override
            public void onError() {
                System.err.println("FAILED");
            }
        });*/
        return message;
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
        this.displayMessageActivity.setText(message);
        //return message;
    }
}
