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
 *    Martin Lanter - architect and re-implementation
 *    Dominique Im Obersteg - parsers and initial implementation
 *    Daniel Pauli - parsers and initial implementation
 *    Kai Hudalla - logging
 ******************************************************************************/
package org.eclipse.californium.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * This test tests that the message type of responses is correct.
 */
public class MessageTypeTest {

	private static final String SERVER_RESPONSE = "server responds hi";
	private static final String ACC_RESOURCE = "acc-res";
	private static final String NO_ACC_RESOURCE = "no-acc-res";
	
	private CoapServer server;
	private int serverPort;
	
	@Before
	public void setupServer() {
		try {
			System.out.println("\nStart "+getClass().getSimpleName());
			EndpointManager.clear();
			
			CoAPEndpoint endpoint = new CoAPEndpoint();
			
			server = new CoapServer();
			server.addEndpoint(endpoint);
			server.add(new CoapResource(ACC_RESOURCE) {
				public void handlePOST(CoapExchange exchange) {
					System.out.println("gotit");
					exchange.accept();
					exchange.respond(SERVER_RESPONSE);
				}
			});
			server.add(new CoapResource(NO_ACC_RESOURCE) {
				public void handlePOST(CoapExchange exchange) {
					exchange.respond(SERVER_RESPONSE);
				}
			});
			server.start();
			serverPort = endpoint.getAddress().getPort();
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	@After
	public void after() {
		server.destroy();
		System.out.println("End "+getClass().getSimpleName());
	}
	
	@Test
	public void testNonConfirmable() throws Exception {
		// send request
		Request req2acc = new Request(Code.POST);
		req2acc.setConfirmable(false);
		req2acc.setURI("localhost:"+serverPort+"/"+ACC_RESOURCE);
		req2acc.setPayload("client says hi".getBytes());
		req2acc.send();
		
		// receive response and check
		Response response = req2acc.waitForResponse(100);
		assertNotNull(response);
		assertEquals(response.getPayloadString(), SERVER_RESPONSE);
		assertEquals(response.getType(), Type.NON);
		
		Request req2noacc = new Request(Code.POST);
		req2noacc.setConfirmable(false);
		req2noacc.setURI("coap://localhost:"+serverPort+"/"+NO_ACC_RESOURCE);
		req2noacc.setPayload("client says hi".getBytes());
		req2noacc.send();
		
		// receive response and check
		response = req2noacc.waitForResponse(100);
		assertNotNull(response);
		assertEquals(response.getPayloadString(), SERVER_RESPONSE);
		assertEquals(response.getType(), Type.NON);
	}
	
	@Test
	public void testConfirmable() throws Exception {
		// send request
		Request req2acc = new Request(Code.POST);
		req2acc.setConfirmable(true);
		req2acc.setURI("localhost:"+serverPort+"/"+ACC_RESOURCE);
		req2acc.setPayload("client says hi".getBytes());
		req2acc.send();
		
		// receive response and check
		Response response = req2acc.waitForResponse(100);
		assertNotNull(response);
		assertEquals(response.getPayloadString(), SERVER_RESPONSE);
		assertEquals(response.getType(), Type.CON);
		
		Request req2noacc = new Request(Code.POST);
		req2noacc.setConfirmable(true);
		req2noacc.setURI("coap://localhost:"+serverPort+"/"+NO_ACC_RESOURCE);
		req2noacc.setPayload("client says hi".getBytes());
		req2noacc.send();
		
		// receive response and check
		response = req2noacc.waitForResponse(100);
		assertNotNull(response);
		assertEquals(response.getPayloadString(), SERVER_RESPONSE);
		assertEquals(response.getType(), Type.ACK);
	}
	
}
