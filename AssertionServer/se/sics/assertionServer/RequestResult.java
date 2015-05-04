
/*
 * @(#)RequestResult.java
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

import se.sics.saml.SignedSAMLAssertion;

import java.util.List;
import java.util.Collections;

/**
 * This class contains either a success message or a description of why a
 * request has failed.
 
 * @author Ludwig Seitz
 *
 */
public class RequestResult {

    /**
     * pre-defined success value
     */
    static boolean RESULT_SUCCESS = true;
    
    /**
     * pre-defined failure value
     */
    static boolean RESULT_FAILURE = false;

    /**
     * This contains the result of the request. A value <i>RESULT_SUCCESS</i> 
     * indicates that the request was successfully processed, a value 
     * <i>RESULT_FAILURE</i> indicates that the request was not successfully 
     * processed.
     */
    private boolean result;
    
    /**
     * This text explains the reason for failure if the request failed. It is
     * empty if the request succeeded.
     */
    private String failureReasons;
    
    /**
     * This list contains the result of a request that returns
     * several results. Typically a query for Assertions
     */
    private List<SignedSAMLAssertion> queryResults;
    
    /**
     * A constructor for a request that failed.
     * 
     * @param failureReasons  the text describing why the request failed.
     */
    public RequestResult(String failureReasons) {
        this.result = RESULT_FAILURE;
        this.failureReasons = failureReasons;
        this.queryResults = null;
    }
    
    /**
     * A constructor for a request that succeeded and that does not
     * return results.
     */
    public RequestResult() {
        this.result = RESULT_SUCCESS;
        this.failureReasons = null;
        this.queryResults = null;
    }

    /**
     * A constructor for a request that succeeded and that does return
     * a <code>List</code> of results.
     * 
     * @param queryResults  The results of the request.
     */
    public RequestResult(List<SignedSAMLAssertion> queryResults) {
        this.result = RESULT_SUCCESS;
        this.failureReasons = null;
        this.queryResults = queryResults;
    }
    
    /**
     * Get the request results
     * 
     * @return  the request results <code>List</code>
     */
    public List<SignedSAMLAssertion> getResults() {
        return Collections.unmodifiableList(this.queryResults);
    }
    
    /**
     * Return true if the request succeeded and false if it didn't.
     * 
     * @return  a <code>boolean</code>.
     */
    public boolean success() {
        return this.result;
    }
    
    /**
     * Returns the reasons why the request failed (or null if it succeeded).
     * 
     * @return  A <code>String</code> containing a failure description.
     */
    public String getFailureReasons() {
        return this.failureReasons;
    }
    
    
}
