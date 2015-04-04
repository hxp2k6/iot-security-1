/*
 * @(#)AssertionRequest.java
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

import se.sics.saml.SAMLAttribute;
import se.sics.saml.SAMLNameID;


/**
 * This class encodes a user request to the assertion server, which retrieves
 * attribute assertions.
 * 
 * @author Ludwig Seitz
 *
 */
public class AssertionRequest {
    
    /**
     * The identifier of the subject of the requested attribute.
     */
    private SAMLNameID subject = null;
    
    /**
     * The identifier of the issuer of the requested attribute.
     */
    private SAMLNameID issuer = null;
    
    /**
     * The attribute that is requested. If the <code>AttributeValue</code>
     * of this member is <code>null</code> any <code>AttributeValue</code>
     * will be matched.
     */
    private SAMLAttribute requestedAttr = null;

    /**
     * Constructor. Builds an <code>AssertionRequest</code> out of its
     * components.
     * @param subject  The identifier of the subject for which the assertion
     * 				   is requested
     * @param issuer  The identifier of the issuer for the requested assertion   
     * @param requestedAttr  The attribute that is requested. If the 
     *                       <code>AttributeValue</code> of this 
     *                       parameter is <code>null</code> any 
     *                       <code>AttributeValue</code> will be matched
     */
    public AssertionRequest(SAMLNameID subject,
    			SAMLNameID issuer, SAMLAttribute requestedAttr) {
        this.subject = subject;
        this.issuer = issuer;
        this.requestedAttr = requestedAttr;
    }
    
    
    /**
     * @return   the identifier of the subject of the requested assertion
     */
    public SAMLNameID getSubject() {
        return this.subject;
    }
   
    /**
     * @return   the identifier of the issuer of the requested assertion
     */
    public SAMLNameID getIssuer() {
        return this.issuer;
    }
    
    /**
     * @return  the requested <code>SAMLAttribute</code>.
     */
    public SAMLAttribute getRequestedAttr() {
        return this.requestedAttr;
    }
}
