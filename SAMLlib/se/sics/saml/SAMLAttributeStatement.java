
/*
 * @(#)SAMLAttributeStatement.java
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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.sics.util.Indenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This represents a SAML AttributeStatement (see section 2.7.3 of the 
 * SAML 2.0 of standard).
 * 
 * 
 * @author Ludwig Seitz
 *
 */
public class SAMLAttributeStatement implements SAMLStatement, Cloneable {
	
	/**
     * A static variable for system independent newline characters.
     */
    private static final String nl = System.getProperty("line.separator");
    
    /**
     * The SAML attribute that is asserted
     */
    private final List<SAMLAttribute> attributes;

    /**
     * Constructs from a list of attributes.
     *
     * @param attrs  the attributes that are asserted.
     */
    public SAMLAttributeStatement(List<SAMLAttribute> attrs) {
    	if (attrs == null) {
            this.attributes = Collections.emptyList();
        } else {
        	this.attributes = new ArrayList<SAMLAttribute>(attrs);
        }
    }
    
    /**
     * Copy constructor.
     * @param orig  the original to be copied
     */
    private SAMLAttributeStatement(SAMLAttributeStatement orig) {
    	this.attributes = new ArrayList<SAMLAttribute>();
    	for (SAMLAttribute a : orig.attributes) {
    		this.attributes.add((SAMLAttribute)a.clone());
    	}
    }
    
    /**
     * Creates a <code>SAMLAttributeStatement</code> by parsing a node.
     *
     * @param root  The node to parse for the 
     * 				<code>SAMLAttributeStatement</code>
     *
     * @return a new <code>SAMLAttributeStatement</code> constructed by parsing
     *
     * @throws VerificationException  if the DOM node is invalid
     */
    public static SAMLAttributeStatement getInstance(Node root) 
            throws VerificationException {
    	 // check if this really is an Attribute
        if (root.getNodeType() != Node.ELEMENT_NODE
                || !root.getLocalName().equals("AttributeStatement")) {
            throw new VerificationException(
            		"Can't create an AttributeStatement from a "
            		+ root.getLocalName() + " xml-node");
        }

        // prepare the necessary variables
        List<SAMLAttribute> attributes 
        	= new ArrayList<SAMLAttribute>();
        
        //parse the attributes
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && node.getLocalName().equals("Attribute")) {
                attributes.add(SAMLAttribute.getInstance(node));
            }
        }
        return new SAMLAttributeStatement(attributes);
    }
    /**
     * Returns the set of attributes that are asserted.
     *
     * @return the attributes that are asserted
     */

    public List<SAMLAttribute> getAttributes() {
        return Collections.unmodifiableList(this.attributes);
    }

    @Override
	public String toString() {
    	return toString(new Indenter());
    }
    
	@Override
	public String toString(Indenter in) {
        String indent = in.makeString();
        in.in();
        String res = indent + "<saml:AttributeStatement>" + nl;
        for (SAMLAttribute attr : this.attributes) {
        	res += attr.toString(in);
        }
        res += nl + indent + "</saml:AttributeStatement>";
        in.out();
        return res;
    }
	
	/**
     * @return  a deep-copy of this SAMLAttributeStatement
     */
	@Override
	public Object clone() {
		return new SAMLAttributeStatement(this);
	}
}
