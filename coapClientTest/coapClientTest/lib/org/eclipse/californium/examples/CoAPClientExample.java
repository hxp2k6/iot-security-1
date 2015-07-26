
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
		options.addOption(new Option(42, "12345678910111213141516171819202122232425262728293031323334353637383940414243444546474849"
					+ "5051525354555657585960616263646566676869707172737475767778798081828384858687888990"
					+ "919293949596979899100101102103104105106107108109110111112113114115116117118119120121122123124125126127128129130"
					+ "131132133134135136137138139140141142143144145146147148149150151152153154155156157158159160"));
		request.setOptions(options);
		request.setURI("coap://192.168.1.241:5683/watch");
		try {
			String response = request.send().waitForResponse().getPayloadString();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		//String content = response.getResponseText();


		
		client.post(new CoapHandler() {
			@Override public void onLoad(CoapResponse response) {
				String content = response.getResponseText();
				System.out.println("RESPONSE 4: " + content);
			}
			
			@Override public void onError() {
				System.err.println("FAILED");
			}
		}, "abcdefg", 0);
		
		// wait for user
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try { br.readLine(); } catch (IOException e) { }
		
		// observe

		System.out.println("OBSERVE (press enter to exit)");
		
		CoapObserveRelation relation = client.observe(
				new CoapHandler() {
					@Override public void onLoad(CoapResponse response) {
						String content = response.getResponseText();
						System.out.println("NOTIFICATION: " + content);
					}
					
					@Override public void onError() {
						System.err.println("OBSERVING FAILED (press enter to exit)");
					}
				});
		
		// wait for user
		try { br.readLine(); } catch (IOException e) { }
		
		System.out.println("CANCELLATION");
		
		relation.proactiveCancel();
	}
}


