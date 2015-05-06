
/*
 * @(#)SAMLAttributeValue.java
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

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Implements the SAML attribute value according to section 2.7.3.1.1
 * of the SAML 2.0 standard.
 * 
 * @author Ludwig Seitz
 *
 */
public class SAMLAttributeValue implements Cloneable {

    /**
     * The optional type declaration.
     */
    private URI type = null;
    
    /**
     * The value
     */
    private String value;

    /**
     * Constructor. Builds a SAMLAttributeValue from the components.
     * 
     * @param type   the datatype of the attribute value, can be null
     * @param value  the attribute value in string representation
     */
    public SAMLAttributeValue(URI type, String value) {
    	if (type != null) {
    		this.type = URI.create(type.toString());
    	}
        this.value = value;
    }
    
    
    /**
     * @return the type
     */
    public URI getType() {
        return this.type;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * @return this attribute value's XML representation.
     */
    @Override
    public String toString() {
        String res = "<saml:AttributeValue";
        if (this.type != null) {
            res += " xsi:type=\"" + this.type.toString() + "\"";
        }
        res += ">" + this.value + "</saml:AttributeValue>";
       return res;
    }

    /**
     * Creates a <code>SAMLAttributeValue</code> by parsing a node.
     *
     * @param root  The node to parse for the <code>SAMLAttributeValue</code>
     *
     * @return a new <code>SAMLAttributeValue</code> constructed by parsing
     *
     * @throws VerificationException if the DOM node is invalid
     */
    public static SAMLAttributeValue getInstance(Node root) 
            throws VerificationException {
        // check if this really is an AttributeValue
        if (root.getNodeType() != Node.ELEMENT_NODE
                || !root.getLocalName().equals("AttributeValue")) {
            throw new VerificationException("Can't create an AttributeValue "
                    + "from a " + root.getLocalName() + " element");
        }
        
        // prepare the necessary variables
        URI type = null;
        String value = null;
        
        NamedNodeMap attrs = root.getAttributes();
        try {
            Node typeAttr = attrs.getNamedItem("xsi:type");
            if (typeAttr != null) {
                type = new URI(typeAttr.getNodeValue());
            }
        } catch (URISyntaxException e) {
            // shouldn't happen, but just in case...
            throw new VerificationException("Error parsing optional xsi:type"
                                       + " xml-attribute", e);
        }
        
        Node textnode = root.getFirstChild();
        if (textnode == null) {
            throw new VerificationException("Can't parse an AttributeValue" 
                    + " node without value");
        }
        value = textnode.getNodeValue();
        
        return new SAMLAttributeValue(type, value);
    }
    
    /**
     * @return  a deep-copy of this SAMLAttributeValue.
     */
    @Override
    public Object clone() {
    	try {
    		SAMLAttributeValue clone = (SAMLAttributeValue)super.clone();
    		if (this.type != null) {
    			clone.type = URI.create(this.type.toString());
    		}
    		clone.value = this.value;
    		return clone;
    	} catch (CloneNotSupportedException e1) {//this should never happen
            throw new RuntimeException("Couldn't clone SAMLAttributeValue");
        }
    }
}
