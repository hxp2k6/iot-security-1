package se.sics.assertionServer.accessControl;

import com.sun.xacml.PDP;
import com.sun.xacml.PDPConfig;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.ctx.Attribute;
import com.sun.xacml.ctx.RequestCtx;
import com.sun.xacml.ctx.ResponseCtx;
import com.sun.xacml.ctx.Result;
import com.sun.xacml.ctx.Subject;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.support.finder.FilePolicyModule;

import se.sics.assertionServer.AttributeDefinition;
import se.sics.assertionServer.actions.AssertionServerAction;
import se.sics.assertionServer.databaseAccess.DBConnector;
import se.sics.saml.SAMLID;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * FIXME: Not tested!!!
 * 
 * XACML 2.0 access control for the AssertionServer using
 * SunXACML.
 * 
 * This class takes the attribute's name as the resource-id value and
 * adds the attribute's SOA and the DataType as other resource attributes.
 *
 * @author Ludwig Seitz
 *
 */
public class XacmlAccessControl implements AccessControlModule {

	/**
	 * The standard URI for listing a subject's id
	 */
	public static URI SUBJECT_ID =
		URI.create("urn:oasis:names:tc:xacml:1.0:subject:subject-id");
	
	/**
     * The standard URI for listing a resource's id
     */
    public static final URI RESOURCE_ID =
        URI.create("urn:oasis:names:tc:xacml:1.0:resource:resource-id");
    
    /**
     * Our private URI for listing an attribute's SOA.
     */
    public static final URI SOA = URI.create("SOA");
    
    /**
     * Our private URI for listing an attribute's datatype.
     */
    public static final URI DATATYPE = URI.create("DataType");
    
	/**
     * The standard URI for listing a resource's id
     */
    public static final URI ACTION_ID =
        URI.create("urn:oasis:names:tc:xacml:1.0:action:action-id");

    /**
     * The internal PDP making the decisions.
     */
    private PDP xacmlPDP;
    
    /**
     * Create a new XACML access control module for the AssertionServer.
     * 
     * @param policyDirectory  the directory where all policies are kept
     */
    public XacmlAccessControl(String policyDirectory) {
    	List<String> fileNames 
    		= getFilesInFolder(policyDirectory, ".xml");
    	PolicyFinder pf = new PolicyFinder();
    	FilePolicyModule pfm = new FilePolicyModule(fileNames);
		pf.setModules(Collections.singleton(pfm));
		pfm.init(pf);
		this.xacmlPDP = new PDP(new PDPConfig(null, 
        pf, null));
    }
    
	@Override
	public void init(DBConnector database) throws SQLException {
		//Do nothing, no database required
	}

	@Override
	public boolean isPermitted(SAMLID subject, AssertionServerAction action) {
		Attribute subjectAttr 
			= new Attribute(SUBJECT_ID, null, null, 
					new StringAttribute(subject.getName()));
		Subject subjectAttrs = new Subject(Collections.singleton(subjectAttr));
		
		ArrayList<Set<Attribute>> resources = new ArrayList<Set<Attribute>>();	
		for (AttributeDefinition ad : action.getAttrs()) {
			HashSet<Attribute> resource = new HashSet<Attribute>();
			resource.add(new Attribute(RESOURCE_ID, null, null,
					new StringAttribute(ad.getAttributeId())));
			resource.add(new Attribute(SOA, null, null, 
					new StringAttribute(ad.getSOA().getName())));
			resource.add(new Attribute(DATATYPE, null, null,
					new StringAttribute(ad.getDataType())));
			resources.add(resource);
		}
		
		Attribute actionAttr 
			= new Attribute(ACTION_ID, null, null, 
					new StringAttribute(action.getActionId()));
		
		for (Set<Attribute> resource : resources) {
			RequestCtx request = new RequestCtx(
					Collections.singleton(subjectAttrs), 
					resource, 
					Collections.singleton(actionAttr), 
					Collections.emptySet());
			ResponseCtx response = this.xacmlPDP.evaluate(request);
			Iterator<Result> results = response.getResults().iterator();
	        while (results.hasNext()) {
	        	Result result = results.next();
	        	if (result.getDecision() != Result.DECISION_PERMIT) {
	        		return false;
	        	}
	        }
		}
		
		//If we got here we had only Permit decisions.
		return true;
	}
	
	/**
	 * Get the files from a directory (optionally specifying the desired
	 * extension).
	 * 
	 * @param directory  the directory (full pathname)
	 * @param extension  the desired extension filter
	 * @return  the List of file names
	 */
	private List<String> getFilesInFolder(String directory, 
			final String extension) {
		File dir = new File(directory);
		String[] children = null;
		if (extension != null) {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(extension);
				}
			};
			children = dir.list(filter);
		} else {
			children = dir.list();
		}
		ArrayList<String> result = new ArrayList<String>();
		for (int i=0; i<children.length;i++) {
			result.add(directory + System.getProperty("file.separator") + children[i]);
		}
		return result;
	}
}
