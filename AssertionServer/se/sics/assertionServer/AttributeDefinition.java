
/*
 * @(#)AttributeDefinition.java
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

package se.sics.assertionServer;

import se.sics.saml.SAMLID;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * This class is a parameter container for attribute definitions.
 * It is used to create, delete and search for attributes in the 
 * Assertion Server.
 * 
 * @author Ludwig Seitz
 *
 */
public class AttributeDefinition {
    
	/**
     * The source of authority of the attribute.
     */
    private final SAMLID soa;
    
    /**
     * The identifier of the attribute. 
     */
    private final String attrId;
  
    /**
     * The data type of the attribute.
     */
    private final String dataType;
    
    /**
     * A set of allowed <code>AttributeValue</code>s.
     */
    private final Set<String> allowedValues;
   
    /**
    * Constructor.
    * 
    * @param soa  The identifier representing the source of authority of 
    * 				the attribute
    * @param attrId  The identifier of the attribute
    * @param dataType  The data type of the attribute
    * @param allowedValues  The string representation for all allowed values 
    * 						for this attribute. If this is <code>null</code>, 
    * 						all values that are of the correct data type are
    * 						considered allowed.
    */
    public AttributeDefinition(SAMLID soa, String attrId, String dataType, 
    		Set<String> allowedValues) {
    	if (soa == null || attrId == null || dataType == null) {
    		throw new IllegalArgumentException("SOA, AttributeId and DataType" 
    				+ " must be non-null in an AttributeDefinition");
    	}
        this.soa = soa;
        this.attrId = attrId;
        this.dataType = dataType;
        if (allowedValues != null) {
        	this.allowedValues = Collections.unmodifiableSet(
                    new HashSet<String>(allowedValues));
        } else {
           this.allowedValues = Collections.emptySet();
        }
    }

    /**
     * @return  An identifier describing the source of
     * 			authority for the attribute.
     */
    public SAMLID getSOA() {
        return this.soa;
    }
    
    /**
     * @return  The attribute identifier.
     */    
    public String getAttributeId() {
        return this.attrId;
    }
    
    /**
     * @return  The data type of the attribute
     */
    public String getDataType() {
    	return this.dataType;
    }
    
    /** 
     * @return the string representations of the
     * 			 allowed values for this attribute
     */
    public Set<String> getAllowedValues() {
        return this.allowedValues;
    }
}