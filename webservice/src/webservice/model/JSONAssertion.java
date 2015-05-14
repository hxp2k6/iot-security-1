package webservice.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.sics.saml.SAMLAttributeStatement;
import se.sics.saml.SAMLConditions;
import se.sics.saml.SAMLNameID;
import se.sics.saml.SAMLSigner;
import se.sics.saml.SAMLStatement;
import se.sics.saml.SAMLSubject;
import se.sics.saml.SignatureVerifier;
import se.sics.saml.VerificationException;
import se.sics.util.DateUtils;
import se.sics.util.IdGenerator;
import se.sics.util.Indenter;
import se.sics.util.XML2String;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;



/**
 * A signed SAML assertion.
 *
 * @author Erik Rissanen
 * @author Ludwig Seitz
 * @version 1.1
 * @since 1.1
 */
public class JSONAssertion {
	
	/**
     * A static variable for system independent newline characters.
     */
    private static final String nl = System.getProperty("line.separator");
    
	/**
	 * A logger for logging events.
	 */
    static Logger logger = LogManager.getLogger(JSONAssertion.class);

    /**
     * The identifier of the assertion
     */
    private final  String id;
    
    /**
     * The time and date when the assertion was issued
     */
    private final Date issueInstant;
        
    /**
     * The issuer of the assertion
     */
    private final SAMLNameID issuer;
    
    /**
     * The subject of the assertion, can be null if there is no subject
     */
    private final  SAMLSubject subject;
    
    /**
     * The conditions associated with this assertion
     */
    private final SAMLConditions conditions;
    
    /**
     * The statements of this assertion
     */
    private final List<SAMLStatement> statements;
    
    /**
     * The XMLDSIG ds:Signature XML-element, 
     * can be null if there is no signature.
     */
    private final Element signature;
    
    
    /**
     * Construct from the components and a signer. This generates a signature
     * with the help of the signer.
     * 
     * @param issuer  the issuer of the assertion
     * @param subject  the subject of the assertion, can be null
     * @param conditions  the conditions associated with this assertion,
     * 					  can be null
     * @param statements  the statements
     * @param signer  a signer for generating the signature, or null if
     * 					we don't want a signature
     * @throws SAXException 
     * @throws IOException 
     * @throws XMLSignatureException 
     * @throws MarshalException 
     * @throws InvalidAlgorithmParameterException 
     * @throws NoSuchAlgorithmException 
     * 
     */
    public JSONAssertion(SAMLNameID issuer, SAMLSubject subject,
    		SAMLConditions conditions, List<SAMLStatement> statements, 
    		SAMLSigner signer) 
    		throws IOException, SAXException, NoSuchAlgorithmException, 
    			   InvalidAlgorithmParameterException, MarshalException, 
    			   XMLSignatureException {
    	this.id = IdGenerator.createIdString();
    	this.issueInstant = new Date();
    	this.issuer = (SAMLNameID)issuer.clone();
    	if (subject != null) {
    		this.subject = (SAMLSubject)subject.clone();
    	} else {
    		this.subject = null;
    	}
    	this.conditions = new SAMLConditions(conditions.getNotBefore(), 
    			conditions.getNotOnOrAfter(), conditions.getConditions());
    	this.statements = new ArrayList<SAMLStatement>(statements);
    	if (signer != null) {
    		this.signature = signer.sign(this.issuer, this.subject, this.id,
    				this.issueInstant, this.conditions, this.statements);
    	} else {
    		this.signature = null;
    	}
    }
    
    
    /**
     * Constructor building from an XML node. This also verifies the
     * signature.
     * 
     * @param root  the xml root node of the assertion
     * @throws VerificationException 
     * @throws XMLSignatureException 
     * @throws MarshalException 
     */
    protected JSONAssertion(Node root) throws VerificationException, MarshalException, XMLSignatureException {
    	if (root.getNodeType() != Node.ELEMENT_NODE &&
    			!root.getLocalName().equals("Assertion")) {
    		throw new VerificationException("Cannot construct a "
    			+ "SignedSAMLAssertionfrom a " + root.getLocalName() 
    			+ " node" );
    	}
    	//Search for ID and IssueInstant
    	if (root.getAttributes().getNamedItem("ID") == null) {
    		throw new VerificationException("Mandatory xml-attribute"
    				+ " ID not found");
    	}
    	this.id = root.getAttributes().getNamedItem("ID").getNodeValue();
    	
    	if (root.getAttributes().getNamedItem("II") == null) {
    		throw new VerificationException("Mandatory xml-attribute"
    				+ " IssueInstant not found");
    	}
    	this.issueInstant = DateUtils.parseInstant(
    			root.getAttributes().getNamedItem(
    					"II").getNodeValue());
    	
    	//Search for issuer, subject and statements
    	NodeList children = root.getChildNodes();
    	Node issuerNode = null;
    	Node subjectNode = null;
    	Node signatureNode = null;
    	Node conditionsNode = null;
    	this.statements = new ArrayList<SAMLStatement>();
    	for (int i=0; i<children.getLength();i++) {
    		Node child = children.item(i);
    		if (child.getNodeType() == Node.ELEMENT_NODE) {
    			if (child.getLocalName().equals("Issuer")) {
    				issuerNode = child;
    			} else if (child.getLocalName().equals("Conditions")) {
    				conditionsNode = child;
    			}else if (child.getLocalName().equals("Signature")) {
    				signatureNode = child;
    			} else if (child.getLocalName().equals("Subject")) {
    				subjectNode = child;
    			} else if (child.getLocalName().equals(
    					"AttributeStatement")) {
    				this.statements.add(
    						SAMLAttributeStatement.getInstance(child));
    			} 
    			//TODO: In future maybe support other types of statements		
    		}    		
    	}
    	if (issuerNode == null) {
    		throw new VerificationException("Mandatory Issuer node not found");
    	}
    	this.issuer = SAMLNameID.getInstance(issuerNode);
    	
    	if (signatureNode != null) {
    		this.signature = (Element)signatureNode;
    		//SignatureVerifier.verifySAMLSignature(root.getOwnerDocument());
    	} else {
    		this.signature = null;
    	}
    	if (conditionsNode != null) {
    		this.conditions = SAMLConditions.getInstance(conditionsNode);
    		this.conditions.checkValidityIntervall();
    	} else {
    		this.conditions = null;
    	}
    	if (subjectNode != null) {
    		this.subject = SAMLSubject.getInstance(subjectNode);
    	} else {
    		this.subject = null;
    	}
    	
    }
    
    
    /**
     * Builds a SignedSAMLAssertion from an XML node.
     * 
     * @param root  the root node of the assertion
     * 
     * @return  a SignedSAMLAssertion object.
     * @throws VerificationException 
     * @throws XMLSignatureException 
     * @throws MarshalException 
     */
    public static JSONAssertion getInstance(Node root) 
    	throws VerificationException, MarshalException, XMLSignatureException {
    	return new JSONAssertion(root);
    }
        
    /**
     * Returns the issuer of the assertion. This is the issuer element of
     * the SAML assertion.
     *
     * @return the issuer of the assertion
     */
    public SAMLNameID getIssuer() {
        return this.issuer;
    }
    

    /**
     * Returns the id of the assertion. This is the ID attribute of
     * the SAML assertion.
     *
     * @return the id of the assertion
     */
    public String getId() {
        return this.id;
    }


    /**
     * Gets the date and time of issuing. This is the IssueInstant
     * attribute of the SAML assertion.
     *
     * @return the time instant when the assertion was issued
     */
    public Date getIssueInstant() {
        return (Date)this.issueInstant.clone();
    }

    /**
     * @return the subject of this assertion
     */
    public SAMLSubject getSubject() {
        return (SAMLSubject)this.subject.clone();
    }
    
    /**
     * @return  the conditions of this assertion
     */
    public SAMLConditions getConditions() {
    	return (SAMLConditions)this.conditions.clone();
    }
    
    /**
     * @return  the statements of this assertion
     */
    public List<SAMLStatement> getStatements() {
    	return Collections.unmodifiableList(this.statements);
    }
    
    /**
     * @return  the signature element of this assertion 
     */
    public Element getSignature() {
    	return this.signature;
    }
       
    /**
     * Check is the assertion is valid now according to the 
     * validity interval in the conditions.
     * 
     * If there is no Conditions element the Assertion is considered to be 
     * valid.
     * @throws VerificationException  if the assertion is not valid
     * 
     */
    public void isValid() throws VerificationException {
    	if (this.conditions != null) {
    		this.conditions.checkValidityIntervall();
    	}
    }
     
    /**
     * Check is the assertion is valid at the given date according to the 
     * validity interval in the conditions.
     * 
     * If there is no Conditions element the Assertion is considered to be 
     * valid.
     * 
     * @param date 
     * @throws VerificationException 
     */
    public void isValid(Date date) throws VerificationException {
    	if (this.conditions != null) {
    		this.conditions.checkValidityIntervall(date);
    	}
    }
		
	@Override
	public String toString() {
		return toString(new Indenter());
	}
	
    /**
     * @param in  An indenter for correct XML indentation
     * @return  the xml encoded string representation of this Assertion.
     */
    /*public String toString(Indenter in) {
    	in.in();
    	String issueInstantStr = DateUtils.toString(this.issueInstant);
    	String issuerStr = this.issuer.getName();
    	String assertion = "";
    	//String assertion = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        //assertion += nl + "<saml:Assertion Version=\"2.0\"" + nl;
        //assertion +=
        //    "  xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"" + nl;
        //assertion += "  xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"" + nl;
        //assertion +=
        //    "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + nl;
        assertion += "{" + nl + "  \"ID\":\"" + this.id + "\"," + nl;
        assertion += "  \"II\":\"" + issueInstantStr + "\"," + nl;
        assertion += "	\"IS\":\"" + issuerStr + "\"," + nl;
        assertion += "	\"SK\":\"" + "\"," + nl;
        assertion += "	\"ST\":{" + nl;
        assertion += "		\"OB\":{" + nl;
        //if (this.signature != null) {
        //	assertion += XML2String.toString(this.signature);
        //}
        if (this.conditions.getNotBefore() != null) {
        	assertion += "			\"NB\":" + DateUtils.toString(this.conditions.getNotBefore()) + "," + nl;
        }
        if (this.conditions.getNotOnOrAfter() != null) {
        	assertion += "			\"NA\":" + DateUtils.toString(this.conditions.getNotOnOrAfter()) + nl;
        }
        assertion += "		}," + nl;
        assertion += "		\"ACT\":" + "GET," + nl;
        assertion += "		\"RES\":" + "coap://ip" + nl;
        assertion += "	}" + nl;
        assertion += "}";
        //if (this.subject != null) {
        //	assertion += this.subject.toString(in) + nl;
        //}

        //for (SAMLStatement statement : this.statements) {
        //	assertion += statement.toString(in) + nl;
        //}
        //assertion += "</saml:Assertion>";   
        return assertion;
    }*/
    
    public JSONObject getJSON(){
    	JSONObject json = new JSONObject();
    	String issueInstantStr = DateUtils.toString(this.issueInstant);
    	String issuerStr = this.issuer.getName();
    	json.put("ID", this.id);
    	json.put("II", issueInstantStr);
    	json.put("IS", issuerStr);
    	json.put("SK", "");
    	
    	JSONObject jsonConditions = new JSONObject();
    	
        if (this.conditions.getNotBefore() != null) {
        	jsonConditions.put("NB", DateUtils.toString(this.conditions.getNotBefore()));
        }
        if (this.conditions.getNotOnOrAfter() != null) {
        	jsonConditions.put("NA", DateUtils.toString(this.conditions.getNotOnOrAfter()));
        }
        
        JSONObject jsonObligations = new JSONObject();
        jsonObligations.put("OB:", jsonConditions);
        json.put("ST", jsonObligations);
        json.put("ACT", "GET");
        json.put("RES", "coap://ip");
		return json;
    }
}

