/*
 * @(#)XML2String.java
 * 
 * Copyright (c) 2011, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 *    * Neither the name of the Swedish Institute of Computer Science
 *      nor the names of its contributors may be used to endorse or
 *      promote products derived from this software without specific
 *      prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package se.sics.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Write XML to a String. Why is this so goddamn complicated? Who designed
 * this pile of d**g called DOM?
 * 
 * @author Ludwig Seitz
 *
 */
public class XML2String {
	
	/**
	 * Write a whole document to a string.
	 * 
	 * @param doc  the document
	 * @return  a string representing the document
	 */
	public static String toString(Document doc) {
		StringWriter sw = new StringWriter();
		try {
			Transformer serializer 
			= TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(
					OutputKeys.OMIT_XML_DECLARATION, "no");
			serializer.transform(new DOMSource(doc.getDocumentElement()), 
					new StreamResult(sw));
		} catch (TransformerException e) {
			throw new RuntimeException(
					"'This never happens' error happened"); 
		}     
		return sw.toString();
	}
	
	/**
	 * Write an XML node to a string.
	 * 
	 * @param node  the node
	 * @return  a string representing the element
	 */
	public static String toString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer serializer 
			= TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(
					OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(node), 
					new StreamResult(sw));
		} catch (TransformerException e) {
			throw new RuntimeException(
					"'This never happens' error happened"); 
		}     
		return sw.toString();
	}
}
