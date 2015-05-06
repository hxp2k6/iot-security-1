
/*
 * @(#)SAMLSigner.java
 *
 * Copyright 2005-2006 Swedish Institute of Computer Science All Rights Reserved.
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

import org.apache.http.auth.AuthenticationException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.ObjectContainer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.Transforms;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import se.sics.util.DateUtils;
import se.sics.util.Indenter;
import se.sics.util.XMLInputParser;

import java.io.IOException;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import java.util.Date;
import java.util.List;

/**
 * Signs SAML assertions.
 *
 * @author Erik Rissanen
 * @author Ludwig Seitz
 * @version 2
 */
public class SAMLSigner {
	
	/**
	 * Initialize the library
	 */
	static {
	      org.apache.xml.security.Init.init();
	   }
		
	/**
     * A static variable for system independent newline characters.
     */
    private static final String nl = System.getProperty("line.separator");
    
	/**
	 * The XML input parser
	 */
    private XMLInputParser parser = null;
    
    /**
     * The private key of this signer
     */
    private PrivateKey privateKey = null;
    
    /**
     * The certificate of this signer
     */
    private X509Certificate cert = null;
    
    /**
     * Constructs a new signer.
     * 
     * @param parser the xml parser to use in the signature process
     * @param privateKey the key with which to sign
     * @param cert the certificate of the key
     * @throws AuthenticationException the format of the subject of
     * the X.509 certificate does not correspond to the AssertionServer 
     * profile
     */
    public SAMLSigner(XMLInputParser parser, PrivateKey privateKey,
                      X509Certificate cert) {
        this.parser = parser;
        this.privateKey = privateKey;
        this.cert = cert;
    }


    /**
     * Creates a signature for a SAML assertion. This constructs the XML of 
     * a SAML assertion from the parameters and signs it. The key
     * with which to sign was selected by the constructor alias
     * parameter.
     * @param issuer  the issuer that should go in this assertion
     * @param subject  the subject of this assertion
     * @param id  the identifier of this assertion
     * @param issueInstant  the issue instant of this assertion
     * @param conditions  the conditions under which this assertion is valid
     * @param statements  the statements of this assertion
     * @return  the signature xml-Element
     * 
     * @throws IOException there was an error writing the result to
     * the output stream
     * @throws SAXException there was an error in the XML to be signed
     * @throws InvalidAlgorithmParameterException 
     * @throws NoSuchAlgorithmException 
     * @throws XMLSecurityException 
     * @throws XMLSignatureException 
     * @throws MarshalException 
     */
    public Element sign(SAMLNameID issuer, SAMLSubject subject, String id,
    		Date issueInstant, SAMLConditions conditions,
    		List<SAMLStatement> statements)
    		throws IOException, SAXException, NoSuchAlgorithmException, 
    			InvalidAlgorithmParameterException, XMLSecurityException {
    	Indenter in = new Indenter();
    	in.in();
        String issueInstantStr = DateUtils.toString(issueInstant);
        String issuerStr = issuer.toString(in).replaceAll("NameID",
        		"Issuer");
        
        String assertion = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        assertion += nl + "<saml:Assertion Version=\"2.0\"" + nl;
        assertion +=
            "  xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"" + nl;
        assertion += "  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" + nl;
        assertion +=
            "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + nl;
        assertion += "  ID=\"" + id + "\"" + nl;
        assertion += "  IssueInstant=\"" + issueInstantStr + "\">" + nl;
        assertion += issuerStr + nl;
        assertion += conditions.toString(in) + nl;
        assertion += subject.toString(in) + nl;
        for (SAMLStatement statement : statements) {
        	assertion += statement.toString(in) + nl;
        }
        assertion += "</saml:Assertion>";

        Document assertionDoc = this.parser.parseDocument(assertion);

        signSAMLDocument(assertionDoc, this.cert, this.privateKey);

        Element docEl = assertionDoc.getDocumentElement();
        
        //Find the signature
        for (int i=0; i<docEl.getChildNodes().getLength(); i++) {
        	Node child = docEl.getChildNodes().item(i);
        	if (child.getNodeType() == Node.ELEMENT_NODE 
        			&& child.getLocalName().equals("Signature")) {
        		return (Element)child;
        	}
        }
        //That shouldn't happen, otherwise we have silently not generated
        //a signature
        return null;       
    }

    /**
     * Create a signature and add it in the document after
     * the issuer element.
     * 
     * @param doc  the original document
     * @param cert  the certificate for this signer
     * @param privateKey  the private key for this signer
     * @return  the signature element
     * 
     * @throws InvalidAlgorithmParameterException 
     * @throws NoSuchAlgorithmException 
     * @throws XMLSecurityException 
     * @throws XMLSignatureException 
     * @throws MarshalException 
     * 
     */
    static private Element signSAMLDocument(Document doc, X509Certificate cert,
    		PrivateKey privateKey) 
    	throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, 
    	XMLSecurityException {
    	
    	//XXX: No base URI (second parameter). What does this mean?
    	XMLSignature sig = new XMLSignature(doc, "",
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);
    	
    	//Find the right place for the Signature element
    	Element subject = null;
    	Element root = doc.getDocumentElement();
    	for (int i=0; i<root.getChildNodes().getLength(); i++) {
    		Node node = root.getChildNodes().item(i);
    		if (node.getNodeType() == Node.ELEMENT_NODE && 
    				node.getLocalName().equals("Subject")) {
    			subject = (Element)node;
    			break;
    		}
    	}    	
    	doc.getDocumentElement().insertBefore(sig.getElement(), subject);
    	
    	ObjectContainer obj = new ObjectContainer(doc);
    	String Id = "TheFirstObject";
    	obj.setId(Id);
        sig.appendObject(obj);
        Transforms transforms = new Transforms(doc);

        transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
        sig.addDocument("#" + Id, transforms);
              
        sig.addKeyInfo(cert);
        sig.addKeyInfo(cert.getPublicKey());
        sig.sign(privateKey);
        
        return sig.getElement();
    }
}
