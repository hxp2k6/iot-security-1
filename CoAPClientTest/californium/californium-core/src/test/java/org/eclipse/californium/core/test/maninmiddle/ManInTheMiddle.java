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
package org.eclipse.californium.core.test.maninmiddle;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

import org.eclipse.californium.core.network.config.NetworkConfig;


/**
 * The man in the middle is between the server and client and monitors the
 * communication. It can drop a packet to simulate packet loss.
 */
public class ManInTheMiddle implements Runnable {

	private int clientPort;
	private int serverPort;
	
	private DatagramSocket socket;
	private DatagramPacket packet;
	
	private volatile boolean running = true;

	private int[] drops = new int[0];
	
	private int current = 0;
	
	// drop bursts longer than MAX_RETRANSMIT must be avoided
	private static final int MAX = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.MAX_RETRANSMIT);
	private int last = -3;
	private int burst = 1;

	public ManInTheMiddle(int clientPort, int serverPort) throws Exception {
		this.clientPort = clientPort;
		this.serverPort = serverPort;

		this.socket = new DatagramSocket();
		this.packet = new DatagramPacket(new byte[2000], 2000);
		
		new Thread(this).start();
	}

	public void reset() {
		current = 0;
		last = -3;
		burst = 1;
	}
	
	public void drop(int... numbers) {
		Arrays.sort(numbers);
		System.out.println("Man in the middle will drop packets "+Arrays.toString(numbers));
		drops = numbers;
	}
	
	public void run() {
		try {
			System.out.println("Start man in the middle");
			while (running) {
				socket.receive(packet);
				
				if (contains(drops, current) && (burst < MAX)) {
					if (last+1==current || last+2==current) burst++;
					System.out.println("Drop packet "+current+" (burst "+(burst)+")");
					last = current;
				
				} else {
					if (packet.getPort()==clientPort)
						packet.setPort(serverPort);
					else
						packet.setPort(clientPort);
				
					socket.send(packet);
					
					if (last+1!=current && last+2!=current) burst = 1;
					//System.out.println("Forwarding " + packet.getLength() + " "+current+" ("+last+" burst "+burst+")");
				}
				current++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		running = false;
		socket.close();
	}
	
	public int getPort() {
		return socket.getLocalPort();
	}
	
	private boolean contains(int[] array, int value) {
		for (int a:array)
			if (a == value)
				return true;
		return false;
	}
	
}
