/*
 * @(#)SAMLSubjectConfirmationData.java
 *
 * Copyright 2011 Swedish Institute of Computer Science All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Swedish Institute of Computer Science or the names of 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. THE SWEDISH INSTITUE OF COMPUTER 
 * SCIENCE ("SICS") AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES 
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS 
 * SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SICS OR ITS LICENSORS BE 
 * LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED
 * AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SICS HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package se.sics.saml;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.sics.util.DateUtils;
import se.sics.util.Indenter;
import se.sics.util.XML2String;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * SubjectConfirmationData according to section 2.4.1.2 of the 
 * SAML 2.0 standard with no additional data.
 * 
 * @author Ludwig Seitz
 *
 */
public class SAMLSubjectConfirmationData implements Cloneable {
	
	/**
     * A static variable for system independent newline characters.
     */
    private static final String nl = System.getProperty("line.separator");
    
	/**
	 * Optional XML-attribute for validity period
	 */
	private final Date notBefore;
	
	/**
	 * Optional XML-attribute for validity period	
	 */
	private final Date notOnOrAfter;
	
	/**
	 * Optional XML-attribute for recipient
	 */
	private final URI recipient;
	
	/**
	 * Optional XML-attribute for SAML protocol message Id
	 */
	private final String inResponseTo;
	
	/**
	 * Optional XML-attribute for Address
	 */
	private final String address;
	   
	/**
	 * The key information for holder-of-key method.
	 */
	private final List<KeyInfo> keyInfos;
	
	
	/**
	 * Constructor.
	 * 
	 * @param notBefore   not valid before this date 
	 * @param notOnOrAfter  not valid on or after this date
	 * @param recipient  intended recipient of this SubjectConfirmationData
	 * @param inResponseTo  the Id of the SAML protocol message this is 
	 * 						responding to
	 * @param address  the network address from which an attesting entity can
	 * 						 present the assertion
	 * 
	 */
	public SAMLSubjectConfirmationData(Date notBefore, Date notOnOrAfter,
			URI recipient, String inResponseTo, String address) {
		this.notBefore = (notBefore == null) ? null : (Date)notBefore.clone();
	    this.notOnOrAfter 
	    	= (notOnOrAfter == null) ? null : (Date)notOnOrAfter.clone();
	    this.recipient 
	    	= (recipient == null) ? null :URI.create(recipient.toString());
	    this.inResponseTo = inResponseTo;
	    this.address = address;
	    this.keyInfos = Collections.emptyList();
	}
	
	/**
	 * Constructor for method = holder-of-key
	 * 
	 * @param notBefore   not valid before this date 
	 * @param notOnOrAfter  not valid on or after this date
	 * @param recipient  intended recipient of this SubjectConfirmationData
	 * @param inResponseTo  the Id of the SAML protocol message this is 
	 * 						responding to
	 * @param address  the network address from which an attesting entity can
	 * 						 present the assertion
	 * @param keyInfos  the information about the key that is held
	 * 
	 */
	public SAMLSubjectConfirmationData(Date notBefore, Date notOnOrAfter,
			URI recipient, String inResponseTo, String address,
			List<KeyInfo> keyInfos) {
		this.notBefore = (notBefore == null) ? null : (Date)notBefore.clone();
	    this.notOnOrAfter 
	    	= (notOnOrAfter == null) ? null : (Date)notOnOrAfter.clone();
	    this.recipient
	    	= (recipient == null) ? null :URI.create(recipient.toString());
	    this.inResponseTo = inResponseTo;
	    this.address = address;
	    this.keyInfos = new ArrayList<KeyInfo>(keyInfos);
	}
	
	/**
	 * @return  the date before which this is invalid
	 */
	public Date getNotBefore() {
		return this.notBefore;
	}

	/**
	 * 
	 * @return  the date after which this is invalid
	 */
	public Date getNotAfter() {
		return this.notOnOrAfter;
	}

	/**
	 * @return  the intended recipient of this SubjectConfirmationData
	 */
	public URI getRecipient() {
		return this.recipient;
	}

	/**
	 * @return  the Id of the SAML protocol message this is responding to
	 */
	public String getInResponseTo() {
		return this.inResponseTo;
	}

	/**
	 * @return  the network address from which an attesting entity can present
	 * 			the assertion
	 */
	public String getAddress() {
		return this.address;
	}
	
	/**
	 * @return  the list of KeyInfo associated to this SubjectConfirmationData
	 */
	public List<KeyInfo> getKeyInfos() {
		return Collections.unmodifiableList(this.keyInfos);
	}
	
	@Override
	public String toString() {
		return toString(new Indenter());
	}

	/**
	 * Create a string containing the XML representation of this object.
	 *
	 * @param in  An indenter for correct XML indentation
     * @return  The XML encoded representation of this 
     * 			SAMLSubjectConfirmationData
	 */
	public String toString(Indenter in) {
		String indent = in.makeString();
		String result = indent + "<saml:SubjectConfirmationData";
		if (this.notBefore != null) {
			result += " NotBefore=\"" + DateUtils.toString(this.notBefore)
				+ "\"";
		}
		if (this.notOnOrAfter != null) {
			result += " NotOnOrAfter=\"" 
				+ DateUtils.toString(this.notOnOrAfter) + "\"";
		}
		if (this.recipient != null) {
			result += " Recipient=\"" + this.recipient.toString() + "\"";
		}
		if (this.inResponseTo != null) {
			result += " InResponseTo=\"" + this.inResponseTo + "\"";
		}
		if (this.address != null) {
			result += " Address=\"" + this.address + "\"";
		}
		
		if (this.keyInfos.isEmpty()) {			
			result += "/>";
		} else {
			in.in();
			result += ">" + nl;
			Document doc = null;
			try {
				DocumentBuilderFactory dbf 
					= DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(false);	
				doc = dbf.newDocumentBuilder().newDocument();
			} catch (ParserConfigurationException e) {
				//This should never happen
				throw new RuntimeException("Unconfigured parser threw up", e);
			}

			// create Hook in DOM
			Element putithere = doc.createElement("Hookelement");
			doc.appendChild(putithere);
			DOMStructure parent = new DOMStructure(putithere);
			
			
			for (KeyInfo keyInfo : this.keyInfos) {
				try {
					keyInfo.marshal(parent, null);
				} catch (MarshalException e) {
					//This shouldn't happen evah!
					throw new RuntimeException("Error while marshaling", e);
				}
			}
			//Now we should have all the stupid KeyInfo XML-nodes
			NodeList children = putithere.getChildNodes();
			for (int i=0; i<children.getLength();i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					result += in.makeString() 
						+ XML2String.toString(child) + nl;
				}
			}
			in.out();
			result += indent + "</saml:SubjectConfirmationData>";
		}
		return result;
	}

	@Override
	public Object clone() {
		return new SAMLSubjectConfirmationData(
				this.notBefore, this.notOnOrAfter, this.recipient, 
				this.inResponseTo, this.address, this.keyInfos);
	}
	
	/**
	 * Create a SAMLSubjectConfirmationData object from an XML node
	 * 
	 * @param root  the XML node containing the SAMLSubjectConfirmationData
	 * 
	 * @return  the parsed SAMLSubjectConfirmationData object
	 * @throws VerificationException 
	 * @throws DOMException 
	 */
	public static SAMLSubjectConfirmationData getInstance(Node root)
			throws VerificationException {
		if (root.getNodeType() != Node.ELEMENT_NODE 
				&& !root.getLocalName().equals("SubjectConfirmationData")) {
			throw new VerificationException("Cannot create " 
					+ "SubjectConfirmationData form:" + root.getLocalName());
		}
		NamedNodeMap attrs = root.getAttributes();
		Node notBeforeAttr = attrs.getNamedItem("NotBefore");
		Date notBefore = null;
		if (notBeforeAttr != null) {			
			notBefore = DateUtils.parseInstant(notBeforeAttr.getNodeValue());
		}
		
		Node notOnOrAfterAttr = attrs.getNamedItem("NotOnOrAfter");
		Date notOnOrAfter = null;
		if (notOnOrAfterAttr != null) {
			notOnOrAfter = DateUtils.parseInstant(
					notOnOrAfterAttr.getNodeValue());
		}		
		
		Node recipientAttr = attrs.getNamedItem("Recipient");
		URI recipient = null;
		if (recipientAttr != null) {
			 try {
				recipient = new URI(recipientAttr.getNodeValue());
			} catch (URISyntaxException e) {
				throw new VerificationException("Cannot parse Recipient "
							+ "XML-attribute", e);
			}
		}
		
		Node inResponseToAttr = attrs.getNamedItem("InResponseTo");
		String inResponseTo = null;
		if (inResponseToAttr != null) {
			inResponseTo = inResponseToAttr.getNodeValue();
		}
		
		Node addressAttr = attrs.getNamedItem("Address");
		String address = null;
		if (addressAttr != null) {
			address = addressAttr.getNodeValue();
		}
		
		List<KeyInfo> keyInfos = new ArrayList<KeyInfo>();
		NodeList children = root.getChildNodes();
		for (int i=0; i<children.getLength();i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE
					&& child.getLocalName().equals("KeyInfo")) {
				//unmarshal KeyInfo
				KeyInfoFactory kif = KeyInfoFactory.getInstance();
				DOMStructure keyInfoNode = new DOMStructure(child);
				try {
					keyInfos.add(kif.unmarshalKeyInfo(keyInfoNode));
				} catch (MarshalException e) {
					throw new VerificationException(
							"Unable to unmarshal KeyInfo", e);
				}
			}
		}
		
		return new SAMLSubjectConfirmationData(
				notBefore, notOnOrAfter, recipient, inResponseTo, address,
				keyInfos);
	}	
}
