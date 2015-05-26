package com.scopus.user;

import android.os.AsyncTask;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Created by Daniel on 01/05/2015.
 */
public class AssertionSender extends AsyncTask<URI, Void, String> {
    protected String doInBackground(URI... uris) {
        String message;

        URI uri = uris[0];
        CoapClient client = new CoapClient("coap://192.168.1.43:5683/light");
        CoapResponse response = client.get();
        if (response != null) {
            message = response.getResponseText();
        }
        else {
            message = "Esse get ta null";
        }
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
}
