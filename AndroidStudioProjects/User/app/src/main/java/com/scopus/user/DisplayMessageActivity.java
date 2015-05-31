package com.scopus.user;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.Toast;

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;


public class DisplayMessageActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        // Get the message from the intent
        Intent intent = getIntent();
        String message = intent.getStringExtra(MyCoapHandler.EXTRA_MESSAGE);

/*******************************************************************************
 * Copyright (c) 2012, Institute for Pervasive Computing, ETH Zurich.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Californium (Cf) CoAP framework.
 ******************************************************************************/


            /*
             * Application entry point.
             * */


       /* URI uri = null; // URI parameter of the request


        // input URI from command line arguments
        try {
            uri = new URI("coap://129.132.15.80/");
        } catch (URISyntaxException e) {
            System.err.println("Invalid URI: " + e.getMessage());
            System.exit(-1);
        }

        // create new request
        Request request = Request.newGet();
        // specify URI of target endpoint
        request.setURI(uri);

        request.send();

        // receive response
        try {
            Response response = request.waitForResponse(1000);

            if (response != null) {
                // response received, output a pretty-print
                System.out.println(response);
            } else {
                System.out.println("No response received.");
            }

        } catch (InterruptedException e) {
            System.err.println("Receiving of response interrupted: " + e.getMessage());
            System.exit(-1);
        }*/


        // Create the text view
        //TextView textView = new TextView(this);
        //textView.setTextSize(40);
        //textView.setText(message);
        // Set the text view as the activity layout

        final TextView textView = (TextView) findViewById(R.id.text_view);
        if (textView != null) {
            textView.setText(message);
                //setContentView(textView);
        } else if (textView == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "Eh null toast", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Hello toast!", Toast.LENGTH_SHORT);
            toast.show();
        }
            //textView.setTextSize(40);
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_display_message, menu);
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

    public void onFinishInflate() {

    }
}
