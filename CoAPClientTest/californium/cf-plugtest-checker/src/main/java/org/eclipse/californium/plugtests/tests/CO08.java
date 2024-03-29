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
package org.eclipse.californium.plugtests.tests;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.CoAP.Type;

import org.eclipse.californium.plugtests.PlugtestChecker.TestClientAbstract;

/**
 * TD_COAP_OBS_08: Server cleans the observers list when observed resource
 * content-format changes
 */
public class CO08 extends TestClientAbstract {

	public static final String RESOURCE_URI = "/obs";
	public final ResponseCode EXPECTED_RESPONSE_CODE = ResponseCode.CONTENT;
	public final ResponseCode EXPECTED_RESPONSE_CODE_1 = ResponseCode.CHANGED;
	public final ResponseCode EXPECTED_RESPONSE_CODE_2 = ResponseCode.NOT_ACCEPTABLE;

	public CO08(String serverURI) {
		super(CO08.class.getSimpleName());

		// create the request
		Request request = new Request(Code.GET, Type.CON);
		// request.setToken(TokenManager.getInstance().acquireToken(false));
		request.setObserve();
		// set the parameters and execute the request
		executeRequest(request, serverURI, RESOURCE_URI);

	}

	@Override
	protected synchronized void executeRequest(Request request, String serverURI, String resourceUri) {

		// defensive check for slash
		if (!serverURI.endsWith("/") && !resourceUri.startsWith("/")) {
			resourceUri = "/" + resourceUri;
		}

		URI uri = null;
		try {
			uri = new URI(serverURI + resourceUri);
		} catch (URISyntaxException use) {
			throw new IllegalArgumentException("Invalid URI: "
					+ use.getMessage());
		}

		request.setURI(uri);
		
		// for observing
		int observeLoop = 2;

		// print request info
		if (verbose) {
			System.out.println("Request for test " + this.testName
					+ " sent");
			Utils.prettyPrint(request);
		}

		// execute the request
		try {
			Response response = null;
			boolean success = true;

			request.send();

			System.out.println();
			System.out.println("**** TEST: " + testName + " ****");
			System.out.println("**** BEGIN CHECK ****");

			response = request.waitForResponse(10000);

			if (response != null) {
				success &= checkInt(EXPECTED_RESPONSE_CODE.value,
						response.getCode().value, "code");
				success &= checkType(Type.ACK, response.getType());
				success &= hasContentType(response);
				success &= hasToken(response);
				success &= hasObserve(response);
			}

			// receive multiple responses
			for (int l = 0; success && l < observeLoop; ++l) {
				response = request.waitForResponse(10000);

				// checking the response
				if (response != null) {
					System.out.println("Received notification " + l);

					// print response info
					if (verbose) {
						System.out.println("Response received");
						System.out.println("Time elapsed (ms): "
								+ response.getRTT());
						Utils.prettyPrint(response);
					}
					
					success &= checkResponse(request, response);

					if (!hasObserve(response)) {
						break;
					}
				}
			}

			// Delete the /obs resource of the server (either locally or by
			// having another CoAP client perform a DELETE request)
			System.out.println("+++++ Sending PUT +++++");
			Request asyncRequest = new Request(Code.PUT, Type.CON);
			asyncRequest.setURI(uri);
			asyncRequest.getOptions().setContentFormat((int) Math.random() * 0xFFFF + 1);
			asyncRequest.setPayload("Random");
			asyncRequest.addMessageObserver(new MessageObserverAdapter() {
				public void onResponse(Response response) {
					if (response != null) {
						checkInt(EXPECTED_RESPONSE_CODE_1.value,
								response.getCode().value, "code");
					}
				}
			});
			// enable response queue for synchronous I/O
			asyncRequest.send();

			long time = response.getOptions().getMaxAge() * 1000;

			response = request.waitForResponse(time + 1000);
			System.out.println("received " + response);

			if (response != null) {
				success &= checkInt(EXPECTED_RESPONSE_CODE_2.value, response.getCode().value, "code");
				success &= hasToken(response);
				success &= hasObserve(response, true);
			} else {
				System.out.println("FAIL: No " + EXPECTED_RESPONSE_CODE_2 + " received");
				success = false;
			}

			if (success) {
				System.out.println("**** TEST PASSED ****");
				addSummaryEntry(testName + ": PASSED");
			} else {
				System.out.println("**** TEST FAILED ****");
				addSummaryEntry(testName + ": --FAILED--");
			}

			tickOffTest();
			
		} catch (InterruptedException e) {
			System.err.println("Interupted during receive: " + e.getMessage());
			System.exit(-1);
		}
	}

	protected boolean checkResponse(Request request, Response response) {
		boolean success = true;

		success &= checkType(Type.CON, response.getType());
		success &= checkInt(EXPECTED_RESPONSE_CODE.value, response.getCode().value, "code");
		success &= checkToken(request.getToken(), response.getToken());
		success &= hasContentType(response);
		success &= hasNonEmptyPalyoad(response);
		success &= hasObserve(response);

		return success;
	}
}
