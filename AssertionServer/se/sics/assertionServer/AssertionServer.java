
/*
 * @(#)SimpleAssertionServer.java
 *
 * Copyright 2011 Swedish Institute of Computer Science All Rights Reserved.
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

//import org.apache.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.sics.assertionServer.AssertionServerSchemaConfiguration.ConfigurationException;
import se.sics.assertionServer.accessControl.AccessControlModule;
import se.sics.assertionServer.actions.AssertionServerAction;
import se.sics.assertionServer.auth.AuthenticationModule;
import se.sics.assertionServer.databaseAccess.DBConnector;
import se.sics.saml.SAMLAttribute;
import se.sics.saml.SAMLAttributeStatement;
import se.sics.saml.SAMLAttributeValue;
import se.sics.saml.SAMLCondition;
import se.sics.saml.SAMLConditions;
import se.sics.saml.SAMLID;
import se.sics.saml.SAMLSigner;
import se.sics.saml.SAMLStatement;
import se.sics.saml.SAMLSubject;
import se.sics.saml.SignedSAMLAssertion;
import se.sics.util.XMLInputParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

/**
 * The base class of the assertion server. Provides attribute management
 * and retrieval functions.
 * 
 * @author Ludwig Seitz
 *
 */
public class AssertionServer {
	
	/**
	 * A logger to log events
	 */
	static Logger logger = LogManager.getLogger(AssertionServer.class);
	
	/**
	 * The database connector used to store attribute information
	 */
	private DBConnector database;
	
	/**
	 * The access control module
	 */
	private AccessControlModule pdp;
		
	/**
	 * The authentication module
	 */
	private AuthenticationModule auth;
	
	/**
	 * The currently authenticated user
	 */
	private SAMLID user;
	
	/**
     * XML input parser. Needed for SAML attribute assertions.
     */
    private XMLInputParser parser;
    
    /**
     * SAML signer.  Needed for SAML attribute assertions.
     */
    private SAMLSigner signer;
    
    /**
     * Default validity in days for the assertions generated by this
     * AssertionServer.
     */
    private int defaultValidity;
    
	/**
	 * Constructor. Takes a database connector and an access control module.
	 * 
	 * @param database  the connector to the database storing the attribute
	 * 					information
	 * @param pdp       the access control module that determines whether
	 * 					an request to the AssertionServer is granted
	 * @param auth      the authentication module that is used to authenticate
	 * 					users
	 * @param privateKeyFile  the file containing the private key used by
	 * 				          this assertion server to sign assertions
	 * @param keyPassword  the password for the private key file
	 * @param rootCerts  the certificate(s) of the CA(s) that signed the 
	 * 					 assertion servers certificate
	 * @param defaultValidity  the default validity in days for Assertions
	 * 						    issued by this AssertionServer
	 * @throws KeyStoreException 
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws UnrecoverableKeyException 
	 * @throws ConfigurationException
	 * @throws ParserConfigurationException 
	 */
	public AssertionServer(DBConnector database, 
			AccessControlModule pdp, AuthenticationModule auth, 
			String privateKeyFile, String keyPassword, 
			List<X509Certificate> rootCerts, int defaultValidity) 
				throws KeyStoreException, NoSuchAlgorithmException, 
				CertificateException, IOException, UnrecoverableKeyException, 
				ConfigurationException, ParserConfigurationException {
		this.database = database;
		this.pdp = pdp;
		this.auth = auth;
		
        // Load the private key of the assertion server from the key stores
        File pkcs12File1 = new File(privateKeyFile);    
        KeyStore ks1;
        X509Certificate cert1 = null;
        PrivateKey privateKey1 = null;
  
        ks1 = KeyStore.getInstance("PKCS12");
        FileInputStream keyFile1 = new FileInputStream(pkcs12File1);
        ks1.load(keyFile1,
        		keyPassword.toCharArray());
        keyFile1.close();
        Enumeration<String> e = ks1.aliases();
        String alias1 = e.nextElement();
        cert1 = (X509Certificate) ks1.getCertificate(alias1);

        privateKey1 
        	= (PrivateKey)ks1.getKey(alias1, keyPassword.toCharArray());            

        
        // Load the schemas into the xml parser
        AssertionServerSchemaConfiguration sconf 
        	= new AssertionServerSchemaConfiguration();
        InputStream[] schemas = sconf.getAsserionServerSchemas();
        Map<String, String> entityMap = sconf.getEntityMap();
        this.parser = new XMLInputParser(schemas, entityMap);

		this.signer = new SAMLSigner(this.parser, privateKey1, cert1);
		this.defaultValidity = defaultValidity;
		
		logger.info("Startup complete");
	}
	
	/**
	 * Connects a user to this assertion server by performing authentication.
	 * 
	 * @param user  the user's identifier
	 * @param authToken  the authentication token used to authenticate the user
	 * @return  the result of the connection request
	 *  
	 */
	public RequestResult connectUser(SAMLID user, Object authToken) {
		try {
			if (this.auth.doAuth(user.getName(), authToken, this.database)) {
				this.user = user;
				logger.info("Connecting user: " + user.getName());
				return new RequestResult();
			}
		} catch (SQLException e) {
			logger.error("Error while connecting user: " + e.getMessage());
			return new RequestResult(e.getMessage());
		}
		logger.info("Authentication failed for user: " + user.getName());
		return new RequestResult("User authentication failed");		
	}
	
	/**
	 * Disconnects the current user from this assertion server. 
	 */
	public void disconnectUser() {
		logger.info("Disconnecting user: " + this.user.getName());
		this.user = null;
	}
	
	/**
	 * Create a new attribute definition.
	 * 
	 * @param newAttribute  the attribute definition
	 * 
	 * @return a success or failure message
	 */
	public RequestResult createAttribute(AttributeDefinition newAttribute) {
		//1. Check if Attribute already exists
		String soaStr = newAttribute.getSOA().getName();
		String attrId = newAttribute.getAttributeId().toString();
		String dataType = newAttribute.getDataType().toString();
		ResultSet existing;
		try {
			existing = this.database.select(null, soaStr, attrId, dataType);
			if (existing.next()) {
				logger.debug("Attempt to create existing attribute:" 
						+ soaStr + ":" + attrId + "(" + dataType + ")");
				return new RequestResult("Attempt to re-create existing"
						+ " attribute definition: SOA = " + soaStr
						+ " AttrId = " + attrId	+ " DataType = " + dataType);
			}
		} catch (SQLException e) {
			logger.error("Error while creating attribute " +
					"(step 1): " + e.getMessage());
			return new RequestResult(e.getMessage());
		}
		
		//2. Check permissions
		Set<AttributeDefinition> target = new HashSet<AttributeDefinition>();
		target.add(newAttribute);
		if (!this.pdp.isPermitted(this.user, new AssertionServerAction(
				target, AssertionServerAction.CREATE, null))) {
			logger.debug("Permission denied to create attribute: " + soaStr 
					+ ":" + attrId + "("+ dataType + ")");
			return new RequestResult("Permission to create attribute denied");
		}
			
		//3. Update database
		try {
			this.database.insertAttrDef(newAttribute);
		} catch (SQLException e) {
			logger.error("Error while creating attribute (step 3): " 
					+ e.getMessage());
			return new RequestResult(e.getMessage());
		}
		logger.info("Creating attribute: " + soaStr + ":" + attrId + "(" 
				+ dataType + ")");
		return new RequestResult();
	}
	
	/**
	 * Delete an attribute definition and all associated attributes.
	 * 
	 * @param attribute  the attribute definition
	 * 
	 * @return a success or failure message
	 */
	public RequestResult deleteAttribute(AttributeDefinition attribute) {
		//1. Check that attribute definition exists
		try {
			ResultSet rs = this.database.selectDefs(
					attribute.getSOA().getName(), attribute.getAttributeId(),
					attribute.getDataType());
			if (!rs.next()) {
				logger.debug("Tried to delete non-existing attribute definition: "
						+ attribute.getSOA().getName() + ":" 
						+ attribute.getAttributeId().toString()
						+ "(" + attribute.getDataType().toString() + ")");
				return new RequestResult("Attribute Definition does not exist");
			}
		} catch (SQLException e) {
			logger.error("Error while retrieving attribute definition: " 
					+ e.getMessage());
			return new RequestResult(e.getMessage());
		}
		
		//2. Check permissions
		Set<AttributeDefinition> target = new HashSet<AttributeDefinition>();
		target.add(attribute);
		if (!this.pdp.isPermitted(this.user, new AssertionServerAction(
				target, AssertionServerAction.DELETE, null))) {
			logger.debug("Permission denied to delete attribute: "
					+ attribute.getSOA().getName() + ":" 
					+ attribute.getAttributeId().toString()
					+ "(" + attribute.getDataType().toString() + ")");
			return new RequestResult("Permission to delete attribute denied");
		}

		//3. Update database
		try {
			this.database.deleteAttrDef(attribute);
		} catch (SQLException e) {
			logger.error("Error while deleting attribute: " + e.getMessage());
			return new RequestResult(e.getMessage());
		}	
		logger.info("Deleting attribute: " + attribute.getSOA().getName() 
				+ ":" + attribute.getAttributeId().toString()
				+ "(" + attribute.getDataType().toString() + ")"); 
				return new RequestResult();
		
			
	}
	
	/**
	 * Add a new attribute value. The attribute definition must exist.
	 * 
	 * @param subject   the identifier of the subject the attribute describes
	 * @param soa       the source of authority for the attribute
	 * @param attribute  the new attribute value
	 * @return   a success or failure message
	 */
	public RequestResult addAttributeValue(SAMLID subject,
			SAMLID soa, SAMLAttribute attribute) {

		String soaStr = soa.getName();
		String attrName = attribute.getName();
		String dataType = attribute.getOtherXMLAttr("xacmlprof:DataType");
		if (dataType == null) {
			dataType = SAMLAttribute.xmlStringId;
		}
		if (attribute.getAttributeValues().isEmpty()) {
			logger.error("Empty value parameter found while adding a new" 
					+ " attribute value");
			return new RequestResult("Cannot add value without a value");
		}
		if (attribute.getAttributeValues().size()!= 1) {
			logger.error("More than one value parameter found while adding a " 
					+ "new attribute value");
			return new RequestResult(
					"Cannot add several values in one add attribute value"
					+ " command");
		}
		//Get the single attribute value of this SAMLAttribute
		String value = attribute.getAttributeValues().get(0).getValue();
		
		
		//1. Check permissions
		Set<AttributeDefinition> target = new HashSet<AttributeDefinition>();
		target.add(new AttributeDefinition(soa, 
				attrName, dataType, null));
		if (!this.pdp.isPermitted(this.user, new AssertionServerAction(
				target, AssertionServerAction.ADD, subject))) {
			logger.debug("Permission denied to add attribute value: "
					+ soaStr + ":"	+ attrName + "(" + dataType 
					+ ") = '" + value + "' to subject: " 
					+ subject.getName());
			return new RequestResult(
					"Permission to add attribute value denied");
		}
		
		//2. Check attribute type is defined and that the value is allowed
		ResultSet existing;
		try {
			existing = this.database.selectDefs(soaStr, attrName,
					dataType);
			if (!existing.next()) {
				logger.debug("Attempt to add value to non-existing attribute: "
						+ soaStr + ":"	+ attrName + "(" + dataType + ") = '" 
						+ value + "' to subject: " 
						+ subject.getName());
				return new RequestResult("Attempt to add value to "
						+ "non-existing attribute: SOA = " + soaStr 
						+ " AttrId = " + attrName	+ " DataType = " 
						+ dataType);
			}
			boolean allowed = false;
			do {
				String allowedValue 
					= existing.getString(DBConnector.allowedValue);
				if (allowedValue == null || allowedValue.equals(value) ) {
					allowed = true;
					break;
				}
			} while (existing.next());
			if (!allowed) {
				logger.debug("Attempt to add illegal value to attribute: "
						+ soaStr + ":"	+ attrName + "(" + dataType + ") = '" 
						+ value + "' to subject: " + subject.getName());
				return new RequestResult("Illegal value for attribute: "
						+ value);
			}
		} catch (SQLException e) {
			logger.error("Error while adding attribute value (step 2): " 
					+ e.getMessage());
			return new RequestResult(e.getMessage());
		}
			
		//3. Update the database 
		try {
			this.database.insertAttr(subject, soa, attribute);
		} catch (SQLException e) {
			logger.error("Error while adding attribute value (step 3): " 
					+ e.getMessage());
			return new RequestResult(e.getMessage());
		}
		logger.info("Adding value for attribute: " 
				+ soaStr + ":"	+ attrName + "(" + dataType + ") = '" + value
				+ "' to subject: " + subject.getName());
		return new RequestResult();
	}
	
	/**
	 * Remove an attribute value.
	 * 
	 * @param subject   the identifier of the subject the attribute describes
	 * @param soa       the source of authority for the attribute
	 * @param attribute the new attribute value
	 * @return   a success or failure message
	 */
	public RequestResult removeAttributeValue(SAMLID subject, SAMLID soa,
			SAMLAttribute attribute) {
		
		String soaStr = soa.getName();
		String attrName = attribute.getName();
		String dataType = attribute.getOtherXMLAttr("xacmlprof:DataType");
		if (dataType == null) {
			dataType = SAMLAttribute.xmlStringId;
		}
		if (attribute.getAttributeValues().isEmpty()) {
			logger.error("Empty value parameter found while removing an" 
					+ " existing attribute value");
			return new RequestResult("Cannot remove an unspecified value");
		}
		if (attribute.getAttributeValues().size()!= 1) {
			logger.error("More than one value parameter found while removing" 
					+ " an existing attribute value");
			return new RequestResult(
					"Cannot remove several values in one remove attribute"
					+ " command");
		}
		//Get the single attribute value of this SAMLAttribute
		String value = attribute.getAttributeValues().get(0).getValue();
		
		
		//1. Check permissions
		Set<AttributeDefinition> target = new HashSet<AttributeDefinition>();
		target.add(new AttributeDefinition(soa, attrName, dataType, null));
		if (!this.pdp.isPermitted(this.user, new AssertionServerAction(
				target, AssertionServerAction.DELETE, subject))) {
			logger.debug("Permission denied to remove attribute value: "
					+ soaStr + ":"	+ attrName + "(" 
					+ dataType + ") = '" 
					+ value
					+ "' from subject: " + subject.getName());
			return new RequestResult(
					"Permission to delete attribute value denied");
		}
		//2. Update the database
		try {
			this.database.deleteAttr(subject, soa, attribute);
		} catch (SQLException e) {
			logger.error("Error while removing attribute value (step 2): " 
					+ e.getMessage());
			return new RequestResult(e.getMessage());
		}
		
		logger.info("Removing attribute value: "
				+ soaStr + ":"	+ attrName + "(" 
				+ dataType + ") = '" 
				+ value + "' from subject: " 
				+ subject.getName());
		return new RequestResult();
	}
	
	/**
	 * Update an attribute value.
	 * 
	 * @param subject  the identifier of the subject the attribute describes
	 * @param soa      the source of authority for the attribute
	 * @param oldAttr  the old attribute
	 * @param newAttrValue  the new attribute value
	 * @return   a success or failure message
	 */
	public RequestResult updateAttributeValue(SAMLID subject, SAMLID soa,			
			SAMLAttribute oldAttr, String newAttrValue) {
	
		String soaStr = soa.getName();
		String dataType =  oldAttr.getOtherXMLAttr("xacmlprof:DataType");
		if (dataType == null) {
			dataType = SAMLAttribute.xmlStringId;
		}
		if (oldAttr.getAttributeValues().size()!= 1) {
			logger.error("More than one value parameter found while updating" 
					+ " an existing attribute value");
			return new RequestResult(
					"Cannot update several values in one update attribute"
					+ " command");
		}
		
		//Get the single attribute value of this SAMLAttribute
		String oldValue = oldAttr.getAttributeValues().get(0).getValue();
		
		//1. Check that the old attribute exists
		ResultSet existing;
		try {
			existing = this.database.select(subject.getName(), soaStr, 
					oldAttr.getName(), dataType);
			boolean found = false;
			while (existing.next()) {
				String value = existing.getString(DBConnector.attrValue);
				if (value.equals(oldValue)) {
					found = true;
					break;
				}
			}
			if (!found) {
				logger.debug("Trying to update inexistant attribute value: "
						+ soaStr + ":"
						+ oldAttr.getName() + "("
						+ dataType + ") = '"
						+ oldValue + "' -> '"
						+ newAttrValue + "'");
				return new RequestResult("Trying to update inexistant "
						+ "attribute value");
			}
		} catch (SQLException e) {
			logger.error("Error while updating attribute value (step 1): " 
					+ e.getMessage());
			return new RequestResult(e.getMessage());
		}
		
		//2. Check permissions
		Set<AttributeDefinition> target = new HashSet<AttributeDefinition>();
		target.add(new AttributeDefinition(soa, oldAttr.getName(), dataType, 
				null));
		if (!this.pdp.isPermitted(this.user, new AssertionServerAction(
				target, AssertionServerAction.UPDATE, subject))) {
			logger.debug("Permission denied to update attribute value: "
					+ soaStr + ":"
					+ oldAttr.getName() + "("
					+ dataType + ") = '"
					+ oldValue + "' -> '"
					+ newAttrValue + "'");
			return new RequestResult(
					"Permission to update attribute value denied");
		}
		
		//3. Check that the new value is allowed
		ResultSet allowedValues;
		try {
			allowedValues = this.database.selectDefs(soaStr, 
					oldAttr.getName(), dataType);
			boolean allowed = false;
			while (allowedValues.next()) {
				String allowedValue 
					= allowedValues.getString(DBConnector.allowedValue);
				if (allowedValue == null 
						|| allowedValue.equals(newAttrValue) ) {
					allowed = true;
					break;
				}
			}
			if (!allowed) {
				logger.debug("Update attempt to illegal value: "
						+ soaStr + ":"
						+ oldAttr.getName() + "("
						+ dataType + ") = '"
						+ oldValue + "' -> '"
						+ newAttrValue + "'");
				return new RequestResult("Illegal value for attribute: "
						+ newAttrValue);
			}
		} catch (SQLException e) {
			logger.error("Error while updating attribute value (step 3): " 
					+ e.getMessage());
			return new RequestResult(e.getMessage());
		}
		
		//4. Update the database
		try {
			this.database.updateAttr(subject, soa, oldAttr, newAttrValue);
		} catch (SQLException e) {
			logger.error("Error while updating attribute value (step 4): " 
					+ e.getMessage());
			return new RequestResult(e.getMessage());
		}
		
		logger.info("Updating attribute value: "
				+ soaStr + ":"
				+ oldAttr.getName() + "("
				+ dataType + ") = '"
				+ oldValue + "' -> '"
				+ newAttrValue + "'");
		return new RequestResult();
	}

	/**
	 * Get attribute assertions for a subject.
	 * 
	 * @param request  the assertion request
	 * @return  the result of the request
	 */
	public RequestResult getAssertions(AssertionRequest request) {
		//1. Check permissions
		SAMLAttribute attr = request.getRequestedAttr();
		Set<AttributeDefinition> target = new HashSet<AttributeDefinition>();
		String dataType = attr.getOtherXMLAttr("xacmlprof:DataType");
		if (dataType == null) {
			dataType = SAMLAttribute.xmlStringId;
		}
		if (attr.getAttributeValues().size() > 1) {
			logger.error("More than one value parameter found while querying" 
					+ " for an assertion");
			return new RequestResult(
					"Cannot search several values in one getAssertion command");
		}
		
		//Get the single attribute value of this SAMLAttribute if any
		String value = null;
		if (!attr.getAttributeValues().isEmpty()) {
			value = attr.getAttributeValues().get(0).getValue();
		}
		
		target.add(new AttributeDefinition(request.getIssuer(),
				attr.getName(), dataType, null));
		if (!this.pdp.isPermitted(this.user, new AssertionServerAction(
				target, AssertionServerAction.QUERY, 
				request.getSubject()))) {
			logger.debug("Permission denied to query attribute value: "
					+ request.getIssuer() + ":"
					+ attr.getName() + "("
					+ dataType + ") = '"
					+ ((value==null) ? "any value" : value) + "'");
			return new RequestResult(
					"Permission to query attribute value denied");
		}
		
		//2. Query database
		HashSet<String> values = new HashSet<String>();
		try {
			ResultSet existing = this.database.select(
					request.getSubject().getName(),
					request.getIssuer().getName(), 
					attr.getName(), 
					dataType);
			while (existing.next()) {
				String someValue 
					= existing.getString(DBConnector.attrValue);
				if (value == null ||
						value.equals(someValue)) {
					values.add(someValue);
				}
			}
		} catch (SQLException e) {
			logger.error("Error while querying attribute value (step 2): " 
					+ e.getMessage());
			return new RequestResult(e.getMessage());
		}
		
		
		//3. Create assertion
		List<SAMLAttributeValue> attrValues 
			= new ArrayList<SAMLAttributeValue>();
		for (String attrValue : values) {
			attrValues.add(new SAMLAttributeValue(null, attrValue));
		}
		SAMLAttribute newAttr = new SAMLAttribute(
				attr.getName(), attr.getNameFormat(), 
				attr.getFriendlyName(), attr.getOtherXMLAttrs(), 
				attrValues);
		List<SAMLAttribute> attrs = new ArrayList<SAMLAttribute>();
		attrs.add(newAttr);
		SAMLAttributeStatement statement = new SAMLAttributeStatement(attrs);
		SAMLSubject subject = new SAMLSubject(request.getSubject());
		List<SAMLStatement> statements = new ArrayList<SAMLStatement>();
		statements.add(statement);
		Date now = new Date();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, this.defaultValidity);
		Date notOnOrAfter = cal.getTime();
		List<SAMLCondition> conditions = Collections.emptyList();
		SAMLConditions conditionsE = new SAMLConditions(now,
				notOnOrAfter, conditions);
		SignedSAMLAssertion assertion = null;
		try {
			assertion = new SignedSAMLAssertion(request.getIssuer(), subject, 
					conditionsE, statements, this.signer);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new RequestResult("Error while creating assertions " 
					+"(step 3): " + e.getMessage());
		}
        List<SignedSAMLAssertion> assertions 
        	= new ArrayList<SignedSAMLAssertion>();
        assertions.add(assertion);
		return new RequestResult(assertions);
	}
}