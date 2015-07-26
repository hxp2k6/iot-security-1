
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

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.examples.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.Request;
//import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.EndpointManager;


public class GETClient {

	/*
	 * Application entry point.
	 * 
	 */	
	public static void main(String args[]) throws InterruptedException {
		
		URI uri = null; // URI parameter of the request
		
			
			// input URI from command line arguments
			try {
				uri = new URI("coap://192.168.1.43:5683/light");
			} catch (URISyntaxException e) {
				System.err.println("Invalid URI: " + e.getMessage());
				System.exit(-1);
			}
			
			CoapClient client = new CoapClient(uri);
			
			

			//CoapResponse response = client.get();
			Request request = new Request(Code.GET);
			if(request == null){
				System.err.println("addr null");
			}
			request.setURI("coap://192.168.1.43:5683/light");
			InetSocketAddress addr = new InetSocketAddress("192.168.1.43", 5683);
			if(addr == null){
				System.err.println("addr null");
			}
			CoAPEndpoint endpoint = new CoAPEndpoint(addr);
			if(endpoint == null){
				System.err.println("addr null");
			}
			request.send(endpoint);
			
			//EndpointManager.getEndpointManager().setDefaultEndpoint(null);
			Response res = request.waitForResponse();
			CoapResponse response = new CoapResponse(res);
			
			if (response!=null) {
				
				System.out.println(response.getCode());
				System.out.println(response.getOptions());
				System.out.println(response.getResponseText());
				
				System.out.println("\nADVANCED\n");
				// access advanced API with access to more details through .advanced()
				//System.out.println(Utils.prettyPrint(response));
				
			} else {
				System.out.println("No response received.");
			}
			
			// display help
			System.out.println("Californium (Cf) GET Client");
			System.out.println("(c) 2014, Institute for Pervasive Computing, ETH Zurich");
			System.out.println();
			System.out.println("Usage: " + GETClient.class.getSimpleName() + " URI");
			System.out.println("  URI: The CoAP URI of the remote resource to GET");
		}
	}
