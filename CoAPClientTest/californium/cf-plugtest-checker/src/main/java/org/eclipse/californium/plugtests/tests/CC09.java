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

import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.CoAP.Type;

import org.eclipse.californium.plugtests.PlugtestChecker.TestClientAbstract;

/**
 * TD_COAP_CORE_09: Perform GET transaction with separate response (CON
 * mode, no piggyback)
 */
public class CC09 extends TestClientAbstract {

	public static final String RESOURCE_URI = "/separate";
	public final ResponseCode EXPECTED_RESPONSE_CODE = ResponseCode.CONTENT;

	public CC09(String serverURI) {
		super(CC09.class.getSimpleName());

		// create the request
		Request request = new Request(Code.GET);
		request.setConfirmable(true);
		// set the parameters and execute the request
		executeRequest(request, serverURI, RESOURCE_URI);
	}

	protected boolean checkResponse(Request request, Response response) {
		boolean success = true;

		success &= checkType(Type.CON, response.getType());
		success &= checkInt(EXPECTED_RESPONSE_CODE.value,
				response.getCode().value, "code");
		success &= hasContentType(response);
		success &= hasNonEmptyPalyoad(response);

		return success;
	}
}
