/*
 * @(#)SAMLSubjectConfirmation.java
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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.sics.util.Indenter;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SAML SubjectConfirmations. See section 2.4.1.1
 * of the SAML 2.0 standard.
 * 
 * @author Ludwig Seitz
 *
 */
public class SAMLSubjectConfirmation implements Cloneable {

    /**
     * A static variable for system independent newline characters.
     */
    private static final String nl = System.getProperty("line.separator");
	
    /**
     * Holder-of-key method identifier
     */
    public static final URI holder = 
    		URI.create("urn:oasis:names:tc:SAML:2.0:cm:holder-of-key");
    
    /**
     * Sender-vouches method identifier
     */
    public static final URI sender =
    		URI.create("urn:oasis:names:tc:SAML:2.0:cm:sender-vouches");
    
    /**
     * Bearer method identifier
     */
    public static final URI bearer =
    		URI.create("urn:oasis:names:tc:SAML:2.0:cm:bearer");
        
	/**
	 * The SubjectConfirmation method
	 */
    private final URI method;
    
    /**
     * The SubjectIds
     */
    private final List<SAMLID> subjectIds;
    
    /**
     * The subject confirmation data. Raw XML here.
     */
    private final List<SAMLSubjectConfirmationData> subjectConfirmationData;
    
    
    /**
     * Create the SubjectConfirmation from its components.
     * 
     * @param method  the confirmation method 
     * @param subjectIds  the subject identifiers confirmed
     * @param data  the confirmation data
     * 
     */
    public SAMLSubjectConfirmation(URI method, List<SAMLID> subjectIds,
    		List<SAMLSubjectConfirmationData> data) {
        this.method = URI.create(method.toString());
        
        if (subjectIds != null) {
        	this.subjectIds = new ArrayList<SAMLID>();      
        		for (SAMLID id : subjectIds) {
        			this.subjectIds.add((SAMLID)id.clone());
        		}
        } else {
        	this.subjectIds = Collections.emptyList();
        }
        
        if (data != null) {
        	this.subjectConfirmationData 
        		= new ArrayList<SAMLSubjectConfirmationData>();
        	for (SAMLSubjectConfirmationData confData : data) {
        		this.subjectConfirmationData.add(
        				(SAMLSubjectConfirmationData)confData.clone());
        				
        	}
        } else {
        	this.subjectConfirmationData = Collections.emptyList();
        }
    }
	
	@Override
	public String toString() {
		return toString(new Indenter());
	}
	
    /**
     * Create a string containing the XML representation of this object.
     * 
     * @param in  An indenter for correct XML indentation
     * @return  The xml encoded contents of this SubjectConfirmation
     */
    public String toString(Indenter in) {
        String indent = in.makeString();
        in.in();
    	String subConf = indent + "<saml:SubjectConfirmation Method=\""
    	 	+ this.method.toString() + "\">" + nl;
    	for (SAMLID id : this.subjectIds) {
    		subConf += id.toString(in) + nl;
    	}
    	for (SAMLSubjectConfirmationData confData 
    			: this.subjectConfirmationData) {		
    		subConf += confData.toString(in) + nl;
    	}
    	subConf += indent + "</saml:SubjectConfirmation>";
    	in.out();
        return subConf;   	
    }
    
    /**
     * @return  A copy of this object.
     */
    @Override
    public Object clone() {
        return new SAMLSubjectConfirmation(
        		this.method, this.subjectIds, this.subjectConfirmationData);  
    }

    /**
     * Create a SAMLSubjectCofnirmation from a node object.
     * 
     * @param root  The xml node containing the SubjectConfirmation.
     * 
     * @return  The SAMLSubjectConfirmation encoded by the node.
     *
     * @throws VerificationException 
     */
    public static SAMLSubjectConfirmation getInstance(Node root) 
            throws VerificationException {
        // check that this is really a SubjectConfirmation
        if (root.getNodeType() != Node.ELEMENT_NODE
                || !root.getLocalName().equals("SubjectConfirmation")) {
            throw new VerificationException(
                    "Can't create a SubjectConfirmation"
                    + "from a " + root.getLocalName() + " element");
        }
        //get the method
        NamedNodeMap attrs = root.getAttributes();
        if (attrs == null) {
        	throw new VerificationException(
        			"Missing mandatory 'Method' xml-attribute");
        }
        Node methodNode = attrs.getNamedItem("Method");
        if (methodNode == null) {
        	throw new VerificationException(
			"Missing mandatory 'Method' xml-attribute");
        }
        URI method = null;
        try {
        	method = new URI (methodNode.getNodeValue());
        } catch (URISyntaxException e) {
        	throw new VerificationException(
        			"Error in message xml-attribute", e);
        }
        List<SAMLID> ids = new ArrayList<SAMLID>();
        List<SAMLSubjectConfirmationData> data 
        	= new ArrayList<SAMLSubjectConfirmationData>();
        NodeList children = root.getChildNodes();
        for (int i=0; i<children.getLength();i++) {
        	Node child = children.item(i);
        	if (child.getNodeType() == Node.ELEMENT_NODE) {
        		if (child.getLocalName().equals("SubjectConfirmationData")) {
        			data.add(SAMLSubjectConfirmationData.getInstance(child));
        		} else {
        			//Try to parse SAMLID
        			SAMLID samlid = SAMLIDFactory.parseSAMLIDNode(child);
        			if (samlid != null) {
        				ids.add(samlid);
        			}
        		}
        	
        	}        	
        }
        return new SAMLSubjectConfirmation(method, ids, data);
    }

    /**
     * @return  the subject confirmation method
     */
	public URI getMethod() {
		return this.method;
	}

	/**
	 * @return  the subject identifiers, if any
	 */
	public List<SAMLID> getSubjectIds() {
		return Collections.unmodifiableList(this.subjectIds);
	}

	/**
	 * @return  the subject confirmation data, if any
	 */
	public List<SAMLSubjectConfirmationData> getSubjectConfirmationData() {
		return Collections.unmodifiableList(this.subjectConfirmationData);
	}
 
}
