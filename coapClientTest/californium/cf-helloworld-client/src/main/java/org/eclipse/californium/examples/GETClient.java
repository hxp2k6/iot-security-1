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
 ******************************************************************************/
package org.eclipse.californium.examples;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;


public class GETClient {

	/*
	 * Application entry point.
	 * 
	 */	
	public static void main(String args[]) throws InterruptedException {
		
		URI uri = null; // URI parameter of the request
		URI pingURI = null;
		
		//if (args.length > 0) {
			
			// input URI from command line arguments
			try {
				uri = new URI("coap://192.168.1.43/light");
				//uri = new URI("coap://vs0.inf.ethz.ch/");
			} catch (URISyntaxException e) {
				System.err.println("Invalid URI: " + e.getMessage());
				System.exit(-1);
			}
			
			try{
				pingURI = new URI("coap://192.168.1.43/light");
				//pingURI  = new URI("coap://vs0.inf.ethz.ch/");
			}
			catch(URISyntaxException e){
				
			}
			CoapClient pingClient = new CoapClient(pingURI);
			
			CoapClient client = new CoapClient(uri);
			if(pingClient.ping() == false){
				System.err.println("Not Ping");
			}
			else{
				System.err.println("Ping");
			}

			//CoapResponse response = client.get();
			/*Request request = new Request(Code.GET);
			request.setURI("coap://vs0.inf.ethz.ch/");
			request.send();
			Response response = request.waitForResponse();*/
			


			
			/*if (response!=null) {
				System.out.println(response.getCode());
				System.out.println(response.getOptions());
				//System.out.println(response.getResponseText());
				
				System.out.println("\nADVANCED\n");
				// access advanced API with access to more details through .advanced()
				System.out.println(Utils.prettyPrint(response));
				System.out.println("Response received.");
				
			} else {
				System.out.println("No response received.");
			}*/
			
		//} else {
			// display help
			System.out.println("Californium (Cf) GET Client");
			System.out.println("(c) 2014, Institute for Pervasive Computing, ETH Zurich");
			System.out.println();
			System.out.println("Usage: " + GETClient.class.getSimpleName() + " URI");
			System.out.println("  URI: The CoAP URI of the remote resource to GET");
		//}
	}

}