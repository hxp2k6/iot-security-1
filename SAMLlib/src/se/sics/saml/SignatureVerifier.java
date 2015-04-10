
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

//import org.apache.log4j.Logger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import se.sics.util.Indenter;
import se.sics.util.X509KeySelector;
import se.sics.util.XMLInputParser;

import java.security.cert.X509Certificate;

import java.util.Iterator;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.ParserConfigurationException;


/**
 * Verifies the digital signatures of SAML assertions.
 *
 * @author Ludwig Seitz
 */
public class SignatureVerifier {
	
	/**
	 * The event logger
	 */
    static Logger logger = LogManager.getLogger(SignatureVerifier.class);


    /**
     * Verifies the digital signature of a SAML assertion.
     *
     * @param samlAssertion the assertion to verify
     * 
     * @throws VerificationException  if the signature isn't valid.
     * @throws MarshalException 
     * @throws XMLSignatureException 
     * 
     */    
    public static void verifySAMLSignature(Document samlAssertion) 
    		throws VerificationException, MarshalException, XMLSignatureException {   	
    	// Find Signature element.
    	NodeList nl = samlAssertion.getElementsByTagNameNS(
    			XMLSignature.XMLNS, "Signature");
    	if (nl.getLength() == 0) {
    	    throw new VerificationException("Cannot find Signature element");
    	}
    	
    	XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");


    	// Create a DOMValidateContext and specify a KeySelector
    	// and document context.
    	DOMValidateContext valContext = new DOMValidateContext
    	    (new X509KeySelector(), nl.item(0));

    	// Unmarshal the XMLSignature.
    	XMLSignature signature = fac.unmarshalXMLSignature(valContext);

    	// Validate the XMLSignature.
    	boolean coreValidity = signature.validate(valContext);
    	
    	// Check core validation status.
    	if (coreValidity == false) {
    	    logger.debug("Signature failed core validation");
    	    boolean sv = signature.getSignatureValue().validate(valContext);
    	    logger.debug("signature validation status: " + sv);
    	    // Check the validation status of each Reference.
    	    Iterator<Reference> i 
    	    	= signature.getSignedInfo().getReferences().iterator();
    	    for (int j=0; i.hasNext(); j++) {
    	    	boolean refValid = i.next().validate(valContext);
    	    	logger.debug("ref["+j+"] validity status: " + refValid);
    	    }
    	    throw new XMLSignatureException("Signature failed core validation");
    	}
		logger.debug("Signature passed core validation");
    }   
    
   /**
    * Verifies the digital signature of a SAML assertion.
    *
    * @param samlAssertion object the assertion to verify
    * @throws ParserConfigurationException 
    * @throws SAXException 
    * @throws VerificationException  if the signature isn't valid.
    * @throws MarshalException 
    * @throws XMLSignatureException 
    * 
    */      
    public static void verifySAMLSignature(SignedSAMLAssertion samlAssertion) 
    		throws ParserConfigurationException, SAXException, 
    		VerificationException, MarshalException, XMLSignatureException {
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
     * @throws VerificationException 
     */
    public static X509Certificate getCertFromDSig(Document doc) 
    		throws	VerificationException {
    	NodeList nl = doc.getElementsByTagNameNS(
    			XMLSignature.XMLNS, "Signature");
    	if (nl.getLength() == 0) {
    	    throw new VerificationException("Cannot find Signature element");
    	}
    	XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

    	// Create a DOMValidateContext and specify a KeySelector
    	// and document context.
    	DOMValidateContext valContext = new DOMValidateContext
    	    (new X509KeySelector(), nl.item(0));

    	// Unmarshal the XMLSignature.
    	XMLSignature signature;
		try {
			signature = fac.unmarshalXMLSignature(valContext);
		} catch (MarshalException e) {
			throw new VerificationException(e);
		}
    	
	    KeyInfo ki = signature.getKeyInfo();
	    if (ki == null) {
	    	throw new VerificationException("Did not find KeyInfo");
	    }
	   
	    for (Object content : ki.getContent()) {
	    	if (content instanceof X509Data) {
	    		X509Data data = (X509Data)content;
	    		for (Object foo : data.getContent()) {
	    			if (foo instanceof X509Certificate) {
	    				return (X509Certificate)foo;
	    			}
	    		}
	    	}
	    }
	    throw new VerificationException("No certificate found in signature");
    }  
    
    /**
     * Utility function that retrieves the certificate from the XML signature.

     * @param assertion  the SAML assertion containing a XML signature
     * @return  the certificate of the singer
     * @throws ParserConfigurationException 
     * @throws SAXException 
     * @throws VerificationException 

     */
    public static X509Certificate getCertFromDSig(
    		SignedSAMLAssertion assertion)
    		throws ParserConfigurationException, SAXException, 
    		VerificationException {
    	XMLInputParser parser = new XMLInputParser(null, null);
    	Document doc = parser.parseDocument(assertion.toString(new Indenter()));
    	return getCertFromDSig(doc);
    }
}
