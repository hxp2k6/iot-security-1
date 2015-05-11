package webservice.model;

import se.sics.saml.SignedSAMLAssertion;

import java.util.List;
import java.util.Collections;

/**
 * This class contains either a success message or a description of why a
 * request has failed.
 
 * @author Ludwig Seitz
 *
 */
public class JSONRequestResult {

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
    private List<JSONAssertion> queryResults;
    
    /**
     * A constructor for a request that failed.
     * 
     * @param failureReasons  the text describing why the request failed.
     */
    public JSONRequestResult(String failureReasons) {
        this.result = RESULT_FAILURE;
        this.failureReasons = failureReasons;
        this.queryResults = null;
    }
    
    /**
     * A constructor for a request that succeeded and that does not
     * return results.
     */
    public JSONRequestResult() {
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
    public JSONRequestResult(List<JSONAssertion> queryResults) {
        this.result = RESULT_SUCCESS;
        this.failureReasons = null;
        this.queryResults = queryResults;
    }
    
    /**
     * Get the request results
     * 
     * @return  the request results <code>List</code>
     */
    public List<JSONAssertion> getResults() {
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

