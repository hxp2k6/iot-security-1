
/*
 * @(#)SAMLAttribute.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.sics.util.Indenter;

/**
 * A SAML attribute as defined in section 2.7.3.1 of the SAML 2.0 standard.
 * @author Ludwig Seitz
 *
 */
public class SAMLAttribute implements Cloneable {
    
    /**
     * A static variable for system independent newline characters.
     */
    private static final String nl = System.getProperty("line.separator");

    /**
     * The NameFormat string for representing XACML attributes.
     */
    public static final URI xacmlNameFormat 
    	= URI.create("urn:oasis:names:tc:SAML:2.0:attrname-format:uri");
    
	/**
	 * The namespace for the SAML XACML Attribute profile
	 */
	public static final String xacmlprofNS = 
		"urn:oasis:names:tc:SAML:2.0:profiles:attribute:XACML";
	
	/**
	 * The namespace prefix for the SAML XACML Attribute profile
	 * (arbitrarily defined here).
	 */
	public static final String xacmlprofNSPrefix = "xacmlprof";
    
	/**
	 * The identifier used by XAMCL for the string datatype.
	 */
    public static final String xmlStringId =
        "http://www.w3.org/2001/XMLSchema#string";
	
    /**
     * The name of the attribute. Translates to AttributeId in XACML.
     */
    private final String name;
    
    /**
     * A URI reference representing the classification of the attribute
     * name for purposes of interpreting it.
     */
    private final URI nameFormat;
    
    /**
     * A more human-readable form of the attribute's name.
     */
    private final String friendlyName;
    
    /**
     * Additional xml-attributes to cover the any-attribute part of
     * the spec.
     */
    private final Map<String, String> otherXMLAttrs;
    
    /**
     * The attribute values of the attribute. A list of 
     * <code>SAMLAttributeValue</code>s, which may be empty.
     */
    private final List<SAMLAttributeValue> attributeValues;
    
    /**
     * Create a SAML attribute from the components.
     * 
     * @param name  The name of the attribute.
     * @param nameFormat  The name format of the attribute, may be null.
     * @param friendlyName  The human-readable form of the attribute's name,
     *                      may be null.
     * @param otherXMLAttrs  Other XML attributes to be used with this
     * 				         SAMLAttribute. Maps xml-attribute-ids to 
     * 						 xml-attribute-values. Can be null.
     * @param attributeValues  The list of <code>SAMLAttributeValue</code>s,
     *                          may be null.
     */
    public SAMLAttribute(String name, URI nameFormat, String friendlyName,
                         Map<String, String> otherXMLAttrs, 
                         List<SAMLAttributeValue> attributeValues) {
        this.name = name;
        if (nameFormat != null) {
        	this.nameFormat = URI.create(nameFormat.toString());
        } else {
        	this.nameFormat = null;
        }
        this.friendlyName = friendlyName;   
        this.otherXMLAttrs = new HashMap<String,String>();
        if (otherXMLAttrs != null) {
        	this.otherXMLAttrs.putAll(otherXMLAttrs);
        }
        if (attributeValues == null) {
            this.attributeValues = Collections.emptyList();
        } else {
        	this.attributeValues = new ArrayList<SAMLAttributeValue>();
        	for (SAMLAttributeValue value : attributeValues) {
        		this.attributeValues.add(
        				new SAMLAttributeValue(value.getType(), 
        						value.getValue()));
        	}
        }

    }
    
    /**
     * Copy constructor. Private.
     * @param orig  the original to copy
     */
    private SAMLAttribute(SAMLAttribute orig) {
        this.name = orig.name;
        if (orig.nameFormat != null) {
        	this.nameFormat = URI.create(orig.nameFormat.toString());
        } else {
        	this.nameFormat = null;
        }
        this.friendlyName = orig.friendlyName;   
        this.otherXMLAttrs = new HashMap<String,String>();
        if (orig.otherXMLAttrs != null) {
        	this.otherXMLAttrs.putAll(orig.otherXMLAttrs);
        }
        if (orig.attributeValues == null) {
            this.attributeValues = Collections.emptyList();
        } else {
        	this.attributeValues = new ArrayList<SAMLAttributeValue>();
        	for (SAMLAttributeValue value : orig.attributeValues) {
        		this.attributeValues.add((SAMLAttributeValue)value.clone());
        	}
        }
    }
    
    /**
     * Creates a <code>SAMLAttribute</code> by parsing a node.
     *
     * @param root  The node to parse for the <code>SAMLAttribute</code>
     *
     * @return a new <code>SAMLAttribute</code> constructed by parsing
     *
     * @throws VerificationException  if the DOM node is invalid
     */
    public static SAMLAttribute getInstance(Node root) 
            throws VerificationException {

        // check if this really is an Attribute
        if (root.getNodeType() != Node.ELEMENT_NODE
                || !root.getLocalName().equals("Attribute")) {
            throw new VerificationException("Can't create an Attribute from a "
                    + root.getLocalName() + " element");
        }

        // prepare the necessary variables
        String name = null;
        URI nameFormat = null;       
        String friendlyName = null;
        HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
        List<SAMLAttributeValue> attributevalues 
        	= new ArrayList<SAMLAttributeValue>();

        NamedNodeMap attrs = root.getAttributes();

        for (int i=0; i<attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            if (attr.getLocalName().equals("Name")) {
                name = attr.getNodeValue();
            } else if (attr.getLocalName().equals("NameFormat")) {
                try {
                    nameFormat = new URI(attr.getNodeValue());
               } catch (URISyntaxException e) {
                   throw new VerificationException("Error parsing the optional" 
                           + "nameFormat xml-attribute", e);
                }
            } else if (attr.getLocalName().equals("FriendlyName")) {
                friendlyName = attr.getNodeValue();
            } else {
            	otherXMLAttrs.put(attr.getNodeName(), attr.getNodeValue());
            }            
        }
        //check if we got the required attribute Name
        if (name == null) {
            throw new VerificationException("Didn't find the required "
                                       + "xml-attribute 'Name'");
        }
        
        //parse the attribute values
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && node.getLocalName().equals("AttributeValue")) {
                attributevalues.add(SAMLAttributeValue.getInstance(node));
            }
        }
        return new SAMLAttribute(name, nameFormat, friendlyName, otherXMLAttrs,
                                 attributevalues);
    }  

    /**
     * @return the attributeValues
     */
    public List<SAMLAttributeValue> getAttributeValues() {
        return Collections.unmodifiableList(this.attributeValues);
    }

    /**
     * @return the friendlyName
     */
    public String getFriendlyName() {
        return this.friendlyName;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the nameFormat
     */
    public URI getNameFormat() {
        return this.nameFormat;
    }
    
    /**
     * @return the map of additional xml-attributes
     */
    public Map<String,String> getOtherXMLAttrs() {
        return Collections.unmodifiableMap(this.otherXMLAttrs);
    }
    
    /**
     * Get an the value of an additional xml-attribute by its
     * fully qualified name.
     * 
     * @param name  the name of the xml-attribute
     * 
     * @return the value of the specified xml-attribute or null if it doesn't
     * 			exist
     */
    public String getOtherXMLAttr(String name) {
    	return this.otherXMLAttrs.get(name);
    }
    
    @Override
	public String toString() {
    	return toString(new Indenter());
    }
    
    /**
     * @param in  An indenter for correct XML indentation
     * @return This attribute's XML representation.
     */
	public String toString(Indenter in) {
		String indent = in.makeString();
		in.in();
        String res = indent + "<saml:Attribute";
               
        if (this.nameFormat != null) {
            res += " NameFormat=\"" + this.nameFormat.toString()+ "\"";
        }
        if (this.friendlyName != null) {
            res += " FriendlyName=\"" + this.friendlyName + "\"";
        }
        res += " Name=\"" + this.name + "\"";
        if (!this.otherXMLAttrs.isEmpty()) {
        	for (Map.Entry<String, String> xmlAttr 
        			: this.otherXMLAttrs.entrySet()) {
        		res += " " + xmlAttr.getKey() + "=\"" 
        			+ xmlAttr.getValue() + "\"";
        	}
        }
        res += ">";
        Iterator<SAMLAttributeValue> iter = this.attributeValues.iterator();
        while(iter.hasNext()) {
            SAMLAttributeValue sav = iter.next();
            res += nl;
            res += in.makeString() + sav.toString();
        }
        res += nl + indent + "</saml:Attribute>";
        in.out();
        return res;
    }
	
    /**
     * @return  a deep-copy of this SAMLAttribute.
     */
    @Override
    public Object clone() {
    	return new SAMLAttribute(this);
    }

}
