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
import java.net.URL;

/**
 * Created by Daniel on 01/05/2015.
 */
public class AssertionSender extends AsyncTask<URI, Void, String> {
    //private String message="";
    //public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    public Context context;

    public AssertionSender(Context context){
        this.context = context.getApplicationContext();
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

        String message = "";

        /*Request request = new Request(CoAP.Code.GET);
        request.setURI("coap://129.132.15.80/");
        request.send();
        try {
            Response response = request.waitForResponse();
            message += response.toString();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/


        CoapClient client = new CoapClient("coap://129.132.15.80/");


        System.out.println("ASYNCHRONOUS");



        MyCoapHandler handler = new MyCoapHandler(context);
        client.get(handler);
        //message += handler.getResponseMessage();
        System.err.println("--------------------------------------------------------------------");
        System.err.println(message);


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
        //this.myActivity.startActivity(new Intent());
        System.err.println(message);
        //Intent intent = new Intent(this.context, DisplayMessageActivity.class);
        //intent.putExtra(EXTRA_MESSAGE, message);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //context.startActivity(intent);
        //return message;
    }
}
