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
 *    Francesco Corazza - HTTP cross-proxy
 ******************************************************************************/
package org.eclipse.californium.proxy;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.network.Exchange.Origin;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.proxy.resources.ProxyCacheResource;
import org.eclipse.californium.proxy.resources.StatsResource;


/**
 * The class represent the container of the resources and the layers used by the
 * proxy. A URI of an HTTP request might look like this:
 * http://localhost:8080/proxy/coap://localhost:5683/example
 */
public class ProxyHttpServer {

	private final static Logger LOGGER = Logger.getLogger(ProxyHttpServer.class.getCanonicalName());
	
	private static final String PROXY_COAP_CLIENT = "proxy/coapClient";
	private static final String PROXY_HTTP_CLIENT = "proxy/httpClient";

	private final ProxyCacheResource cacheResource = new ProxyCacheResource(true);
	private final StatsResource statsResource = new StatsResource(cacheResource);
	
	private ProxyCoAPResolver proxyCoapResolver;
	private HttpStack httpStack;

	/**
	 * Instantiates a new proxy endpoint from the default ports.
	 * 
	 * @throws SocketException
	 *             the socket exception
	 */
	public ProxyHttpServer(CoapServer server) throws IOException {
		this(NetworkConfig.getStandard().getInt(NetworkConfig.Keys.HTTP_PORT));
	}

	/**
	 * Instantiates a new proxy endpoint.
	 * 
	 * @param httpPort
	 *            the http port
	 * @throws IOException
	 *             the socket exception
	 */
	public ProxyHttpServer(int httpPort) throws IOException {
	
		this.httpStack = new HttpStack(httpPort);
		this.httpStack.setRequestHandler(new RequestHandler() {
			public void handleRequest(Request request) {
				ProxyHttpServer.this.handleRequest(request);
			}
		});
	}

	public void handleRequest(final Request request) {
		
		LOGGER.info("ProxyEndpoint handles request "+request);
		
		Exchange exchange = new Exchange(request, Origin.REMOTE) {
			@Override public void sendResponse(Response response) {
				// Redirect the response to the HttpStack instead of a normal
				// CoAP endpoint.
				// TODO: When we change endpoint to be an interface, we can
				// redirect the responses a little more elegantly.
				try {
					request.setResponse(response);
					responseProduced(request, response);
					httpStack.doSendResponse(request, response);
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Exception while responding to Http request", e);
				}
			}
		};
		exchange.setRequest(request);
		
		Response response = null;
		// ignore the request if it is reset or acknowledge
		// check if the proxy-uri is defined
		if (request.getType() != Type.RST && request.getType() != Type.ACK 
				&& request.getOptions().hasProxyUri()) {
			// get the response from the cache
			response = cacheResource.getResponse(request);

				LOGGER.info("Cache returned "+response);

			// update statistics
			statsResource.updateStatistics(request, response != null);
		}

		// check if the response is present in the cache
		if (response != null) {
			// link the retrieved response with the request to set the
			// parameters request-specific (i.e., token, id, etc)
			exchange.sendResponse(response);
			return;
		} else {

			// edit the request to be correctly forwarded if the proxy-uri is
			// set
			if (request.getOptions().hasProxyUri()) {
				try {
					manageProxyUriRequest(request);
					LOGGER.info("after manageProxyUriRequest: "+request);

				} catch (URISyntaxException e) {
					LOGGER.warning(String.format("Proxy-uri malformed: %s", request.getOptions().getProxyUri()));

					exchange.sendResponse(new Response(ResponseCode.BAD_OPTION));
				}
			}

			// handle the request as usual
			proxyCoapResolver.forwardRequest(exchange);
			/*
			 * Martin:
			 * Originally, the request was delivered to the ProxyCoAP2Coap which was at the path
			 * proxy/coapClient or to proxy/httpClient
			 * This approach replaces this implicit fuzzy connection with an explicit
			 * and dynamically changeable one.
			 */
		}
	}

	/**
	 * Manage proxy uri request.
	 * 
	 * @param request
	 *            the request
	 * @throws URISyntaxException
	 *             the uRI syntax exception
	 */
	private void manageProxyUriRequest(Request request) throws URISyntaxException {
		// check which schema is requested
		URI proxyUri = new URI(request.getOptions().getProxyUri());

		// the local resource that will abstract the client part of the
		// proxy
		String clientPath;

		// switch between the schema requested
		if (proxyUri.getScheme() != null && proxyUri.getScheme().matches("^http.*")) {
			// the local resource related to the http client
			clientPath = PROXY_HTTP_CLIENT;
		} else {
			// the local resource related to the http client
			clientPath = PROXY_COAP_CLIENT;
		}

		LOGGER.info("Chose "+clientPath+" as clientPath");

		// set the path in the request to be forwarded correctly
		request.getOptions().setUriPath(clientPath);
		
	}

	protected void responseProduced(Request request, Response response) {
		// check if the proxy-uri is defined
		if (request.getOptions().hasProxyUri()) {
				LOGGER.info("Cache response");
			// insert the response in the cache
			cacheResource.cacheResponse(request, response);
		} else {
				LOGGER.info("Do not cache response");
		}
	}

	public ProxyCoAPResolver getProxyCoapResolver() {
		return proxyCoapResolver;
	}

	public void setProxyCoapResolver(ProxyCoAPResolver proxyCoapResolver) {
		this.proxyCoapResolver = proxyCoapResolver;
	}
	
}
