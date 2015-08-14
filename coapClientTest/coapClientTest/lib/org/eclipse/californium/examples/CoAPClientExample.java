
/*******************************************************************************
 * Copyright (c) 2014 Institute for Pervasive Computing, ETH Zurich and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Matthias Kovatsch - creator and main architect
 *    Martin Lanter - architect and initial implementation
 ******************************************************************************/
package org.eclipse.californium.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;

public class CoAPClientExample {

	public static void main(String[] args) {
		
		try {
			URI uri = new URI("coap://192.168.1.241:5683/watch");
		} catch (URISyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}


		
		CoapClient client = new CoapClient("coap://192.168.1.241:5683/watch");

		System.out.println("SYNCHRONOUS");
		
		// synchronous
		//String content1 = client.get().getResponseText();
		//System.out.println("RESPONSE 1: " + content1);
		
		//CoapResponse resp2 = client.post("payload", MediaTypeRegistry.TEXT_PLAIN);
		//System.out.println("RESPONSE 2 CODE: " + resp2.getCode());
		
		// asynchronous
		
		System.out.println("ASYNCHRONOUS (press enter to continue)");
		
		/*client.get(new CoapHandler() {
			@Override public void onLoad(CoapResponse response) {
				String content = response.getResponseText();
				System.out.println("RESPONSE 3: " + content);
			}
			
			@Override public void onError() {
				System.err.println("FAILED");
			}
		});*/
		


		/*Request request = Request.newGet();
		OptionSet options = request.getOptions();
		options.addOption(new Option(42, "Asssertion"));
		request.setOptions(options);
		CoapClient client2 = new CoapClient();
		CoapResponse response = client2.asynchronous(request.setURI(uri), new CoapHandler() {
			@Override public void onLoad(CoapResponse response) {
				String content = response.getResponseText();
				System.out.println("RESPONSE 3: " + content);
			}
			
			@Override public void onError() {
				System.err.println("FAILED");
			}
		});*/
		Request request = new Request(Code.GET);
		request.setPayload("Plain text");
		OptionSet options = request.getOptions();
		options.addOption(new Option(42, "{\"II\":\"2015-08-09T20:20:33Z\",\"ST\":{\"ACT\":\"GET\","
				+ "\"RES\":\"coap://192.168.1.241/watch\",\"OB\":{\"NA\":\"2015-08-10T20:20:32Z\","
				+ "\"NB\":\"2015-08-09T20:20:32Z\"}},\"ID\":\"ID_3412034b-b082-40bf-b3a1-defa82a8ccb4\","
				+ "\"SK\":\"key\",\"IS\":\"testSOA\"}"));
		request.setOptions(options);
		request.setURI("coap://192.168.1.241:5683/watch");
		try {
			String response = request.send().waitForResponse().getPayloadString();
			System.out.println(response);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		//String content = response.getResponseText();


		
		/*client.post(new CoapHandler() {
			@Override public void onLoad(CoapResponse response) {
				String content = response.getResponseText();
				System.out.println("RESPONSE 4: " + content);
			}
			
			@Override public void onError() {
				System.err.println("FAILED");
			}
		}, "abcdefg", 0);*/
	}
}

