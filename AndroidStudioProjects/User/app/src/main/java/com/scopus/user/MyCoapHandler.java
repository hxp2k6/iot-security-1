package com.scopus.user;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

/**
 * Created by Marcio on 29/05/2015.
 */
public class MyCoapHandler  implements CoapHandler {

    private String content;
    @Override
    public void onLoad(CoapResponse response) {
        content = response.getResponseText();
        System.out.println("RESPONSE 3: " + content);
    }

    @Override
    public void onError() {
        System.err.println("FAILED");
    }

    public String getResponseMessage(){
        return content;
    }
}
