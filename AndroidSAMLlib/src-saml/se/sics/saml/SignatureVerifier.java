
/*
 * @(#)SignatureVerifier.java
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

import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.sics.util.Indenter;
import se.sics.util.XMLInputParser;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.xml.parsers.ParserConfigurationException;


/**
 * Verifies the digital signatures of SAML assertions.
 *
 * @author Ludwig Seitz
 */
public class SignatureVerifier {
	
	// This is important!
    static {
    	org.apache.xml.security.Init.init();
    }
	
    /**
     * Verifies the digital signature of a SAML assertion.
     *
     * @param samlAssertion the assertion to verify
     * 
     * @throws VerificationException  if the signature isn't valid.
     * @throws XMLSecurityException 
     * @throws MarshalException 
     * @throws XMLSignatureException 
     * 
     */    
    public static void verifySAMLSignature(Document samlAssertion) 
    		throws VerificationException, XMLSignatureException, 
    		XMLSecurityException {   	
    	// Find Signature element.
    	NodeList nl = samlAssertion.getElementsByTagNameNS(
    			Constants.SignatureSpecNS, "Signature");
    	if (nl.getLength() == 0) {
    	    throw new VerificationException("Cannot find Signature element");
    	}
    	
    	Element sigElement = (Element) nl.item(0);
	    XMLSignature signature = new XMLSignature(sigElement, "");
    	
	    KeyInfo ki = signature.getKeyInfo();
	    if (ki == null) {
	    	throw new VerificationException("Did not find KeyInfo");
	    }
	    
	    X509Certificate cert = signature.getKeyInfo().getX509Certificate();
	    if (cert == null) {
	    	PublicKey pk = signature.getKeyInfo().getPublicKey();
	    	if (pk == null) {
	    		throw new VerificationException(
		    		"Did not find Certificate or Public Key");
	    	}
	    	if (!signature.checkSignatureValue(pk)) {
	    		throw new VerificationException("Signature invalid");
	    	}
	    } else {
	    	if (!signature.checkSignatureValue(cert)) {
	    		throw new VerificationException("Signature invalid");
	    	}
	    }
    }

  
    
   /**
    * Verifies the digital signature of a SAML assertion.
    *
    * @param samlAssertion object the assertion to verify
    * @throws ParserConfigurationException 
    * @throws SAXException 
    * @throws VerificationException  if the signature isn't valid.
 * @throws XMLSecurityException 
    * @throws MarshalException 
    * 
    */      
    public static void verifySAMLSignature(SignedSAMLAssertion samlAssertion) 
    		throws ParserConfigurationException, SAXException, 
    		VerificationException, XMLSecurityException {
    	XMLInputParser parser = new XMLInputParser(null, null);
    	Document doc = parser.parseDocument(
    			samlAssertion.toString(new Indenter()));
    	verifySAMLSignature(doc);
    }
    
    /**
     * Utility function that retrieves the certificate from the XML signature.
     * 
     * @param doc  the SAML assertion containing a XML signature
     * @return  the certificate of the singer
     * @throws XMLSecurityException 
     * @throws XMLSignatureException 
     * @throws VerificationException 
     */
    public static X509Certificate getCertFromDSig(Document doc) 
    		throws XMLSignatureException, XMLSecurityException, 
    		VerificationException {
    	NodeList nl = doc.getElementsByTagNameNS(
    			Constants.SignatureSpecNS, "Signature");
    	if (nl.getLength() == 0) {
    	    throw new VerificationException("Cannot find Signature element");
    	}
    	
    	Element sigElement = (Element) nl.item(0);
	    XMLSignature signature = new XMLSignature(sigElement, "");
    	
	    KeyInfo ki = signature.getKeyInfo();
	    if (ki == null) {
	    	throw new VerificationException("Did not find KeyInfo");
	    }
	    
	    return signature.getKeyInfo().getX509Certificate();
    }  
    
    /**
     * Utility function that retrieves the certificate from the XML signature.
     * @param assertion 
     * 
     * @param doc  the SAML assertion containing a XML signature
     * @return  the certificate of the singer
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws VerificationException 
     * @throws XMLSecurityException 
     * @throws XMLSignatureException 
     */
    public static X509Certificate getCertFromDSig(SignedSAMLAssertion assertion) 
    		throws ParserConfigurationException, SAXException, 
    		XMLSignatureException, XMLSecurityException, VerificationException {
    	XMLInputParser parser = new XMLInputParser(null, null);
    	Document doc = parser.parseDocument(assertion.toString(new Indenter()));
    	return getCertFromDSig(doc);
    }
}
