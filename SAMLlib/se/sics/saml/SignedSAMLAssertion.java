
/*
 * @(#)SignedSAMLAssertion.java
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.sics.util.DateUtils;
import se.sics.util.IdGenerator;
import se.sics.util.Indenter;
import se.sics.util.XML2String;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;

/**
 * A signed SAML assertion.
 *
 * @author Erik Rissanen
 * @author Ludwig Seitz
 * @version 1.1
 * @since 1.1
 */
public class SignedSAMLAssertion {
	
	/**
     * A static variable for system independent newline characters.
     */
    private static final String nl = System.getProperty("line.separator");
    
	/**
	 * A logger for logging events.
	 */
    static Logger logger = LogManager.getLogger(SignedSAMLAssertion.class);

    /**
     * The identifier of the assertion
     */
    private final  String id;
    
    /**
     * The time and date when the assertion was issued
     */
    private final Date issueInstant;
        
    /**
     * The issuer of the assertion
     */
    private final SAMLNameID issuer;
    
    /**
     * The subject of the assertion, can be null if there is no subject
     */
    private final  SAMLSubject subject;
    
    /**
     * The conditions associated with this assertion
     */
    private final SAMLConditions conditions;
    
    /**
     * The statements of this assertion
     */
    private final List<SAMLStatement> statements;
    
    /**
     * The XMLDSIG ds:Signature XML-element, 
     * can be null if there is no signature.
     */
    private final Element signature;
    
    
    /**
     * Construct from the components and a signer. This generates a signature
     * with the help of the signer.
     * 
     * @param issuer  the issuer of the assertion
     * @param subject  the subject of the assertion, can be null
     * @param conditions  the conditions associated with this assertion,
     * 					  can be null
     * @param statements  the statements
     * @param signer  a signer for generating the signature, or null if
     * 					we don't want a signature
     * @throws SAXException 
     * @throws IOException 
     * @throws XMLSignatureException 
     * @throws MarshalException 
     * @throws InvalidAlgorithmParameterException 
     * @throws NoSuchAlgorithmException 
     * 
     */
    public SignedSAMLAssertion(SAMLNameID issuer, SAMLSubject subject,
    		SAMLConditions conditions, List<SAMLStatement> statements, 
    		SAMLSigner signer) 
    		throws IOException, SAXException, NoSuchAlgorithmException, 
    			   InvalidAlgorithmParameterException, MarshalException, 
    			   XMLSignatureException {
    	this.id = IdGenerator.createIdString();
    	this.issueInstant = new Date();
    	this.issuer = (SAMLNameID)issuer.clone();
    	if (subject != null) {
    		this.subject = (SAMLSubject)subject.clone();
    	} else {
    		this.subject = null;
    	}
    	this.conditions = new SAMLConditions(conditions.getNotBefore(), 
    			conditions.getNotOnOrAfter(), conditions.getConditions());
    	this.statements = new ArrayList<SAMLStatement>(statements);
    	if (signer != null) {
    		this.signature = signer.sign(this.issuer, this.subject, this.id,
    				this.issueInstant, this.conditions, this.statements);
    	} else {
    		this.signature = null;
    	}
    }
    
    
    /**
     * Constructor building from an XML node. This also verifies the
     * signature.
     * 
     * @param root  the xml root node of the assertion
     * @throws VerificationException 
     * @throws XMLSignatureException 
     * @throws MarshalException 
     */
    protected SignedSAMLAssertion(Node root) throws VerificationException, MarshalException, XMLSignatureException {
    	if (root.getNodeType() != Node.ELEMENT_NODE &&
    			!root.getLocalName().equals("Assertion")) {
    		throw new VerificationException("Cannot construct a "
    			+ "SignedSAMLAssertionfrom a " + root.getLocalName() 
    			+ " node" );
    	}
    	//Search for ID and IssueInstant
    	if (root.getAttributes().getNamedItem("ID") == null) {
    		throw new VerificationException("Mandatory xml-attribute"
    				+ " ID not found");
    	}
    	this.id = root.getAttributes().getNamedItem("ID").getNodeValue();
    	
    	if (root.getAttributes().getNamedItem("IssueInstant") == null) {
    		throw new VerificationException("Mandatory xml-attribute"
    				+ " IssueInstant not found");
    	}
    	this.issueInstant = DateUtils.parseInstant(
    			root.getAttributes().getNamedItem(
    					"IssueInstant").getNodeValue());
    	
    	//Search for issuer, subject and statements
    	NodeList children = root.getChildNodes();
    	Node issuerNode = null;
    	Node subjectNode = null;
    	Node signatureNode = null;
    	Node conditionsNode = null;
    	this.statements = new ArrayList<SAMLStatement>();
    	for (int i=0; i<children.getLength();i++) {
    		Node child = children.item(i);
    		if (child.getNodeType() == Node.ELEMENT_NODE) {
    			if (child.getLocalName().equals("Issuer")) {
    				issuerNode = child;
    			} else if (child.getLocalName().equals("Conditions")) {
    				conditionsNode = child;
    			}else if (child.getLocalName().equals("Signature")) {
    				signatureNode = child;
    			} else if (child.getLocalName().equals("Subject")) {
    				subjectNode = child;
    			} else if (child.getLocalName().equals(
    					"AttributeStatement")) {
    				this.statements.add(
    						SAMLAttributeStatement.getInstance(child));
    			} 
    			//TODO: In future maybe support other types of statements		
    		}    		
    	}
    	if (issuerNode == null) {
    		throw new VerificationException("Mandatory Issuer node not found");
    	}
    	this.issuer = SAMLNameID.getInstance(issuerNode);
    	
    	if (signatureNode != null) {
    		this.signature = (Element)signatureNode;
    		SignatureVerifier.verifySAMLSignature(root.getOwnerDocument());
    	} else {
    		this.signature = null;
    	}
    	if (conditionsNode != null) {
    		this.conditions = SAMLConditions.getInstance(conditionsNode);
    		this.conditions.checkValidityIntervall();
    	} else {
    		this.conditions = null;
    	}
    	if (subjectNode != null) {
    		this.subject = SAMLSubject.getInstance(subjectNode);
    	} else {
    		this.subject = null;
    	}
    	
    }
    
    
    /**
     * Builds a SignedSAMLAssertion from an XML node.
     * 
     * @param root  the root node of the assertion
     * 
     * @return  a SignedSAMLAssertion object.
     * @throws VerificationException 
     * @throws XMLSignatureException 
     * @throws MarshalException 
     */
    public static SignedSAMLAssertion getInstance(Node root) 
    	throws VerificationException, MarshalException, XMLSignatureException {
    	return new SignedSAMLAssertion(root);
    }
        
    /**
     * Returns the issuer of the assertion. This is the issuer element of
     * the SAML assertion.
     *
     * @return the issuer of the assertion
     */
    public SAMLNameID getIssuer() {
        return this.issuer;
    }
    

    /**
     * Returns the id of the assertion. This is the ID attribute of
     * the SAML assertion.
     *
     * @return the id of the assertion
     */
    public String getId() {
        return this.id;
    }


    /**
     * Gets the date and time of issuing. This is the IssueInstant
     * attribute of the SAML assertion.
     *
     * @return the time instant when the assertion was issued
     */
    public Date getIssueInstant() {
        return (Date)this.issueInstant.clone();
    }

    /**
     * @return the subject of this assertion
     */
    public SAMLSubject getSubject() {
        return (SAMLSubject)this.subject.clone();
    }
    
    /**
     * @return  the conditions of this assertion
     */
    public SAMLConditions getConditions() {
    	return (SAMLConditions)this.conditions.clone();
    }
    
    /**
     * @return  the statements of this assertion
     */
    public List<SAMLStatement> getStatements() {
    	return Collections.unmodifiableList(this.statements);
    }
    
    /**
     * @return  the signature element of this assertion 
     */
    public Element getSignature() {
    	return this.signature;
    }
       
    /**
     * Check is the assertion is valid now according to the 
     * validity interval in the conditions.
     * 
     * If there is no Conditions element the Assertion is considered to be 
     * valid.
     * @throws VerificationException  if the assertion is not valid
     * 
     */
    public void isValid() throws VerificationException {
    	if (this.conditions != null) {
    		this.conditions.checkValidityIntervall();
    	}
    }
     
    /**
     * Check is the assertion is valid at the given date according to the 
     * validity interval in the conditions.
     * 
     * If there is no Conditions element the Assertion is considered to be 
     * valid.
     * 
     * @param date 
     * @throws VerificationException 
     */
    public void isValid(Date date) throws VerificationException {
    	if (this.conditions != null) {
    		this.conditions.checkValidityIntervall(date);
    	}
    }
		
	@Override
	public String toString() {
		return toString(new Indenter());
	}
	
    /**
     * @param in  An indenter for correct XML indentation
     * @return  the xml encoded string representation of this Assertion.
     */
    public String toString(Indenter in) {
    	in.in();
    	String issueInstantStr = DateUtils.toString(this.issueInstant);
    	String issuerStr = this.issuer.toString(in).replaceAll("NameID",
    			"Issuer");
    	
    	String assertion = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        assertion += nl + "<saml:Assertion Version=\"2.0\"" + nl;
        assertion +=
            "  xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"" + nl;
        assertion += "  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" + nl;
        assertion +=
            "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + nl;
        assertion += "  ID=\"" + this.id + "\"" + nl;
        assertion += "  IssueInstant=\"" + issueInstantStr + "\">" + nl;
        assertion += issuerStr + nl;
        if (this.signature != null) {
        	assertion += XML2String.toString(this.signature);
        }
        if (this.conditions != null) {
        	assertion += this.conditions.toString(in) + nl;
        }
        if (this.subject != null) {
        	assertion += this.subject.toString(in) + nl;
        }

        for (SAMLStatement statement : this.statements) {
        	assertion += statement.toString(in) + nl;
        }
        assertion += "</saml:Assertion>";   
        return assertion;
    }
}
