package com.scopus.user;

import android.content.Context;
import android.content.Intent;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

/**
 * Created by Marcio on 29/05/2015.
 */
public class MyCoapHandler  implements CoapHandler {

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    public Context context;

    public MyCoapHandler(Context context){
        this.context = context;
    }

    private String message;
    @Override
    public void onLoad(CoapResponse response) {
        message = response.getResponseText();
        System.out.println("RESPONSE 3: " + message);
        Intent intent = new Intent(this.context, DisplayMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onError() {
        System.err.println("FAILED");
    }

    //public String getResponseMessage(){
        //return content;
    //}
}
