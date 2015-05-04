
/*
 * @(#)SAMLNameID.java
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

import se.sics.util.Indenter;

/**
 * Implements the SAML NameID element. See section 2.2.2 of the SAML 2.0
 * standard.
 * 
 * @author Ludwig Seitz
 *
 */
public class SAMLNameID implements SAMLID, Cloneable {
    
    /**
     * boolean value for identifying the NameQualifier type.
     */
    public static final boolean NameQualifier = true;
    
    /**
     * boolean value for identifying the SPNameQualifier type.
     */
    public static final boolean SPNameQualifier = false;
    
    /**
     * The value of the NameID.
     */
    private String name;
    
    /**
     * The optional Qualifier.
     */
    private String qualifier;
    
    /**
     * Is the qualifier a NameQualifier or not
     * (then it's a SPNameQualifier).
     */
    private boolean qualifierType;
    
    /**
     * The optional Format.
     */
    private URI format;
    
    /**
     * The optional SPProvidedID.
     */
    private String spProvidedID;
     
    /**
     * Simple constructor for an ID with just a name.
     * 
     * @param name  The name this ID provides.
     */
    public SAMLNameID(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The name provided by an ID"
                    + " can not be null");
        }
        this.name = name;
    }
    
    /**
     * Full constructor with all optional parameters.
     * 
     * @param name  The name this ID provides.
     * @param qualifierType  true means NameQualifier, false means
     *                        SPNameQualifier. Discarded if qualifier
     *                        is null.
     * @param qualifier  The NameQualifier or SPNameQualifier, can be null.
     * @param format  The Format, can be null.
     * @param spProvidedID  The SPProvidedID, can be null.
     */
    public SAMLNameID(String name, boolean qualifierType, String qualifier,
                   URI format, String spProvidedID) {
        this.name = name;
        if (name == null) {
            throw new IllegalArgumentException("The name provided by an ID"
                    + " can not be null");
        }
        this.qualifierType = qualifierType;
        this.qualifier = qualifier;
        this.format = format;
        this.spProvidedID = spProvidedID;
    }
    
    /**
     * @return the Format or null.
     */
    public URI getFormat() {
        return this.format;
    }

    /**
     * @return the name represented by this ID.
     */
    @Override
	public String getName() {
        return this.name;
    }

    /**
     * @return the NameQualifier or SPNameQualifier or null.
     */
    public String getQualifier() {
        return this.qualifier;
    }

    /**
     * @return the qualifierType (true == NameQualifier, 
     *          false == SPNameQualifier).
     */
    public boolean isQualifierType() {
        return this.qualifierType;
    }

    /**
     * @return the SPProvidedID
     */
    public String getSpProvidedID() {
        return this.spProvidedID;
    }
	
	@Override
	public String toString() {
		return toString(new Indenter());
	}
    
    /**
 	 * @param in  An indenter for correct XML indentation
     * @return  The xml representation of this ID.
     */
    @Override
	public String toString(Indenter in) {
        String result = in.makeString() + "<saml:NameID";
        if (this.qualifier != null) {
            if (this.qualifierType) {
                result += " NameQualifier=\"" + this.qualifier + "\"";
            } else {
                result += " SPNameQualifier=\"" + this.qualifier + "\"";
            }
        }
        if (this.format != null) {
            result += " Format=\"" + this.format.toString() + "\"";
        }
        if (this.spProvidedID != null) {
            result += " SPProvidedID=\"" + this.spProvidedID + "\"";
        }
        result += ">" + this.name + "</saml:NameID>";
           return result;
    }

    /**
    * Create a NameID from a node object.
    * 
    * @param root  The xml node containing the NameID.
    * 
    * @return  The NameID encoded by the node.
    *
    * @throws VerificationException 
    */
    public static SAMLNameID getInstance(Node root) 
            throws VerificationException {
        // check that this is really a NameID
        if (root.getNodeType() != Node.ELEMENT_NODE) {
            throw new VerificationException("Can't create a NameID from a "
                   + "non-element node");
        }
        
        // prepare the necessary variables
        String name = null;
        String qualifier = null;
        boolean qualifierType = true;
        URI format = null;
        String spProvidedID = null;

        NamedNodeMap attrs = root.getAttributes();

        for (int i=0; i<attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            if (attr.getLocalName().equals("NameQualifier")) {
                qualifierType = NameQualifier;
                qualifier = attr.getNodeValue();
            } else if (attr.getLocalName().equals("SPNameQualifier")) {
                qualifierType = SPNameQualifier;
                qualifier = attr.getNodeValue();
            } else if (attr.getLocalName().equals("Format")) {
                try {
                    format = new URI(attr.getNodeValue());
               } catch (URISyntaxException e) {
                   throw new VerificationException("Error parsing the optional" 
                           + "Format xml-attribute", e);
               }
            } else if (attr.getLocalName().equals("SPProvidedID")) {
                spProvidedID = attr.getNodeValue();
            } 
        }
        
        //get the name
        Node textnode = root.getFirstChild();
        if (textnode == null) {
            throw new VerificationException("Can't parse an NameID" 
                    + " node without value");
        }
        name = textnode.getNodeValue();

        return new SAMLNameID(name, qualifierType, qualifier, 
                          format, spProvidedID);        
    }
        
    /**
     * @return  A copy of this NameID.
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {//this should never happen
            throw new RuntimeException("Couldn't clone NameID");
            
        }
    }
   
    @Override
	public boolean equals(Object otherId) {
    	if (otherId instanceof SAMLNameID) {
    		SAMLNameID otherNameId = (SAMLNameID)otherId;

    		if (!this.name.equals(otherNameId.name)) {
    			return false;
    		}
    		
    		if (this.format != null) {
    			if (!this.format.equals(otherNameId.format)) {
    				return false;
    			}
    		} else if (otherNameId.format != null) {
    			return false;
    		}
    		
    		if (this.qualifier != null) {
    			if (!this.qualifier.equals(otherNameId.qualifier)) {
    				return false;
    			}
    		} else if (otherNameId.qualifier != null) {
    			return false;
    		}
    		
    		if (this.qualifierType != otherNameId.qualifierType) {
    			return false;
    		}
    		
    		if (this.spProvidedID != null) {
    			if (!this.spProvidedID.equals(otherNameId.spProvidedID)) {
    				return false;
    			}
    		} else if (otherNameId.spProvidedID != null) {
    			return false;
    		}
    		return true;
    	}
    	return false;
    }

	@Override
	public int hashCode() {
		return this.name.hashCode() 
			+ (this.format!=null ? this.format.hashCode() : 0)
			+ (this.qualifier!=null ? this.qualifier.hashCode() : 0)
			+ (this.qualifierType ? 1 :0)
			+ (this.spProvidedID!=null ? this.spProvidedID.hashCode() :0);
	}
    
}