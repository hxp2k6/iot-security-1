package se.sics.assertionServer.tests;

import org.xml.sax.SAXException;

import se.sics.assertionServer.AssertionRequest;
import se.sics.assertionServer.AssertionServer;
import se.sics.assertionServer.AttributeDefinition;
import se.sics.assertionServer.RequestResult;
import se.sics.assertionServer.accessControl.AccessControlModule;
import se.sics.assertionServer.accessControl.XacmlAccessControl;
import se.sics.assertionServer.auth.PasswordAuth;
import se.sics.assertionServer.databaseAccess.DBConnector;
import se.sics.assertionServer.databaseAccess.SQLConnector;

import se.sics.saml.SAMLAttribute;
import se.sics.saml.SAMLAttributeValue;
import se.sics.saml.SAMLNameID;
import se.sics.saml.SignatureVerifier;
import se.sics.saml.SignedSAMLAssertion;
import se.sics.saml.VerificationException;

import se.sics.util.XMLInputParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Basic tests for the AssertionServer
 * 
 * Note: You need a MySQL database installed and configured as by the default 
 * of the <code>SQLConnector</code> class to run this tests.
 * 
 * @author Ludwig Seitz
 *
 */
public class SimpleTests extends TestCase {

	/**
	 * A parser for parsing XML.
	 */
	@SuppressWarnings("unused")
	private static XMLInputParser parser;
    
	/**
	 * The assertion server instance
	 */
	private static AssertionServer as;
	
	/**
	 * The database connector
	 */
	private static DBConnector db;
	
	/**
	 * The authentication module
	 */
	private static PasswordAuth authN;
	
	/**
	 * The access control module
	 */
	private static AccessControlModule authZ;
	
	/**
	 * Setup only once
	 */
	private static boolean setupDone = false;
	
	/**
	 * Tear down only at end
	 */
	private static int testMethodsLeft = 0;

	// count all test methods
	static {
		for (Method method : SimpleTests.class.getMethods()) {
			if (method.getName().startsWith("test")) {
				testMethodsLeft++;
			}
		}
	}

	@Override
	protected void setUp() throws Exception {
		if (!setupDone) {
			parser = new XMLInputParser(null, null);
			db =  new SQLConnector(null, null, null);

			authZ = new XacmlAccessControl(
					"/home/ludwig/workspace/AssertionServer2/resources/"
					+ "testData/Policies");
			authN = new PasswordAuth(null);

			List<X509Certificate> certs = new ArrayList<X509Certificate>();
			File rootCertFile 
				= new File("resources/testData/Certificates/cacert.pem");
			CertificateFactory certFact 
				= CertificateFactory.getInstance("X.509");

			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(rootCertFile));

			X509Certificate rootCert 
				= (X509Certificate) certFact.generateCertificate(bis);
			certs.add(rootCert);

			as = new AssertionServer(db, authZ, authN,
					"resources/testData/Certificates/aserver.p12",
					"password", certs, 12);
			setupDone = true;
		}
	}

	
	/**
	 * Test creating the database.
	 * 
	 * @throws SQLException 
	 */
	public void testCreateDB() throws SQLException {
		//Test creating the database
		db.executeCommand("DROP DATABASE IF EXISTS " 
				+ DBConnector.dbName + ";");
		db.init("password");
		//Test creating the authentication database
		authN.init(db);
		//Test creating the access control database
		authZ.init(db);
		
	}
	
	/**
	 * Test the authentication module.
	 * 
	 * @throws SQLException 
	 */
	public void testAuthN() throws SQLException {
		//Test creating the user
		authN.newUser("testSOA", "password", db);
		ResultSet res = db.executeQuery("SELECT * FROM " 
					+ DBConnector.dbName + "." + authN.table
					+ " WHERE " + authN.login +"='" + "testSOA" + "';");
		Assert.assertTrue(res.next());
		
		//Test authenticating the user
		RequestResult result = as.connectUser(new SAMLNameID("testSOA"),
				"password");
		Assert.assertTrue(result.success());
	}
	
	/**
	 * Test a negative authorization.
	 */
	public void testAccessDenied() {
		HashSet<String> allowedValues = new HashSet<String>();
		allowedValues.add("member");
		allowedValues.add("administrator");
		
		String soaStr = "testNotSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		String id = "testID";
		String dataType = SAMLAttribute.xmlStringId;
		
		AttributeDefinition ad = new AttributeDefinition(soa, id, 
				dataType, allowedValues);
		RequestResult result = as.createAttribute(ad);
		Assert.assertFalse(result.success());
	}
	
	/**
	 * Test creating an attribute.
	 * 
	 * @throws SQLException 
	 */
	public void testCreateAttr() throws SQLException {	
		HashSet<String> allowedValues = new HashSet<String>();
		allowedValues.add("member");
		allowedValues.add("administrator");
		
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		String id = "testID";
		String dataType = SAMLAttribute.xmlStringId;
		
		AttributeDefinition ad = new AttributeDefinition(soa, id, 
				dataType, allowedValues);
		RequestResult result = as.createAttribute(ad);
		
		Assert.assertTrue(result.success());
		
		ResultSet res = db.executeQuery("SELECT * FROM " +
					DBConnector.dbName + "." + DBConnector.attrDefTable
					+ " WHERE SOA='" + soaStr + "' AND AttrId='"
					+ id.toString() + "' AND " 
					+ " DataType='" + dataType.toString() + "';");
		Assert.assertTrue(res.next());		
	}
	
	/**
	 * Test adding an attribute value to a subject.
	 */
	public void testAddValue() {
		String id = "testID";
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		SAMLNameID subject = new SAMLNameID("ludwig@sics.se");
		String dataType = SAMLAttribute.xmlStringId;
		HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
		otherXMLAttrs.put("xmlns:" + SAMLAttribute.xacmlprofNSPrefix, 
				SAMLAttribute.xacmlprofNS);
		otherXMLAttrs.put(SAMLAttribute.xacmlprofNSPrefix + ":" +
				"DataType", dataType);
		ArrayList<SAMLAttributeValue> values
			= new ArrayList<SAMLAttributeValue>();
		values.add(new SAMLAttributeValue(null, "member"));
		SAMLAttribute valueMember = new SAMLAttribute(
			id, SAMLAttribute.xacmlNameFormat, null, 
			otherXMLAttrs, values);
	
		//Test adding a legal attribute value
		RequestResult result = as.addAttributeValue(
				subject, soa, valueMember);
		Assert.assertTrue(result.success());
	}
	
	/**
	 * Test adding an illegal attribute value to a subject
	 */
	public void testAddIllegalValue() {
		//Test adding an illegal attribute value
		String id = "testID";
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		SAMLNameID subject = new SAMLNameID("ludwig@sics.se");
		String dataType = SAMLAttribute.xmlStringId;
		HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
		otherXMLAttrs.put("xmlns:" + SAMLAttribute.xacmlprofNSPrefix, 
				SAMLAttribute.xacmlprofNS);
		otherXMLAttrs.put(SAMLAttribute.xacmlprofNSPrefix + ":" +
				"DataType", dataType);
		ArrayList<SAMLAttributeValue> values
			= new ArrayList<SAMLAttributeValue>();
		values.add(new SAMLAttributeValue(null, "clown"));
		SAMLAttribute valueClown = new SAMLAttribute(
				id, SAMLAttribute.xacmlNameFormat, null, 
				otherXMLAttrs, values);
		RequestResult result = as.addAttributeValue(
				subject, soa, valueClown);
		Assert.assertFalse(result.success());
	}
	
	/**
	 * Test updating an attribute value to an illegal value.
	 */
	public void testIllegalUpdate() {
		//Test updating to an illegal attribute value
		String id = "testID";
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		SAMLNameID subject = new SAMLNameID("ludwig@sics.se");
		String dataType = SAMLAttribute.xmlStringId;
		HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
		otherXMLAttrs.put("xmlns:" + SAMLAttribute.xacmlprofNSPrefix, 
				SAMLAttribute.xacmlprofNS);
		otherXMLAttrs.put(SAMLAttribute.xacmlprofNSPrefix + ":" +
				"DataType", dataType);
		ArrayList<SAMLAttributeValue> values
			= new ArrayList<SAMLAttributeValue>();
		values.add(new SAMLAttributeValue(null, "member"));
		SAMLAttribute valueMember = new SAMLAttribute(
			id, SAMLAttribute.xacmlNameFormat, null, 
			otherXMLAttrs, values);
	
		RequestResult result = as.updateAttributeValue(
				subject, soa, valueMember, "clown");		
		Assert.assertFalse(result.success());
	}
	
	/**
	 * Test updating an inexistant attribute.
	 */
	public void testTargetNotFoundUpdate() {
		//Test update on inexistant attribute
		String id = "testID";
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		SAMLNameID subject = new SAMLNameID("ludwig@sics.se");
		String dataType = SAMLAttribute.xmlStringId;
		HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
		otherXMLAttrs.put("xmlns:" + SAMLAttribute.xacmlprofNSPrefix, 
				SAMLAttribute.xacmlprofNS);
		otherXMLAttrs.put(SAMLAttribute.xacmlprofNSPrefix + ":" +
				"DataType", dataType);
		ArrayList<SAMLAttributeValue> values
			= new ArrayList<SAMLAttributeValue>();
		values.add(new SAMLAttributeValue(null, "clown"));
		SAMLAttribute valueClown = new SAMLAttribute(
				id, SAMLAttribute.xacmlNameFormat, null, 
				otherXMLAttrs, values);
		
		RequestResult result = as.updateAttributeValue(
				subject, soa, valueClown, "member");
		Assert.assertFalse(result.success());
	}
	
	/**
	 * Test updating an attribute.
	 */
	public void testUpdate() {
		//Test updating attribute value
		String id = "testID";
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		SAMLNameID subject = new SAMLNameID("ludwig@sics.se");
		String dataType = SAMLAttribute.xmlStringId;
		HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
		otherXMLAttrs.put("xmlns:" + SAMLAttribute.xacmlprofNSPrefix, 
				SAMLAttribute.xacmlprofNS);
		otherXMLAttrs.put(SAMLAttribute.xacmlprofNSPrefix + ":" +
				"DataType", dataType);
		ArrayList<SAMLAttributeValue> values
			= new ArrayList<SAMLAttributeValue>();
		values.add(new SAMLAttributeValue(null, "member"));
		SAMLAttribute valueMember = new SAMLAttribute(
				id, SAMLAttribute.xacmlNameFormat, null, 
				otherXMLAttrs, values);
		
		RequestResult result = as.updateAttributeValue(
				subject, soa, valueMember, "administrator");
		Assert.assertTrue(result.success());	
	}
	
	/**
	 * Test retrieving an assertion.
	 * 
	 * @throws VerificationException 
	 * @throws XMLSignatureException 
	 * @throws MarshalException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public void testGetAssertion() throws VerificationException, 
			ParserConfigurationException, SAXException, MarshalException, 
			XMLSignatureException {
		//Test getting an assertion
		String id = "testID";
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		SAMLNameID subject = new SAMLNameID("ludwig@sics.se");
		String dataType = SAMLAttribute.xmlStringId;
		HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
		otherXMLAttrs.put("xmlns:" + SAMLAttribute.xacmlprofNSPrefix, 
				SAMLAttribute.xacmlprofNS);
		otherXMLAttrs.put(SAMLAttribute.xacmlprofNSPrefix + ":" +
				"DataType", dataType);
		ArrayList<SAMLAttributeValue> values
			= new ArrayList<SAMLAttributeValue>();
		values.add(new SAMLAttributeValue(null, "administrator"));
		SAMLAttribute valueAdmin = new SAMLAttribute(
				id, SAMLAttribute.xacmlNameFormat, null, 
				otherXMLAttrs, values);
		
		AssertionRequest request = new AssertionRequest(
				subject, soa, valueAdmin);
		RequestResult result = as.getAssertions(request);
		SignedSAMLAssertion assertion = result.getResults().get(0);
		assertion.isValid();
		SignatureVerifier.verifySAMLSignature(assertion);
	}
	
	
	/**
	 * Test positive authorization.
	 * @throws SQLException 
	 */
	public void testAccessGranted() throws SQLException {
		authN.newUser("testBob", "password", db);
		//Disconnect the current user
		as.disconnectUser();
		
		as.connectUser(new SAMLNameID("testBob"), "password");
		
		//Test getting an assertion
		String id = "testID";
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		SAMLNameID subject = new SAMLNameID("ludwig@sics.se");
		String dataType = SAMLAttribute.xmlStringId;
		HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
		otherXMLAttrs.put("xmlns:" + SAMLAttribute.xacmlprofNSPrefix, 
				SAMLAttribute.xacmlprofNS);
		otherXMLAttrs.put(SAMLAttribute.xacmlprofNSPrefix + ":" +
				"DataType", dataType);
		ArrayList<SAMLAttributeValue> values
			= new ArrayList<SAMLAttributeValue>();
		values.add(new SAMLAttributeValue(null, "administrator"));
		SAMLAttribute valueAdmin = new SAMLAttribute(
				id, SAMLAttribute.xacmlNameFormat, null, 
				otherXMLAttrs, values);
		
		AssertionRequest request = new AssertionRequest(
				subject, soa, valueAdmin);
		RequestResult result = as.getAssertions(request);
		Assert.assertTrue(result.success());
		
		//Reconnect the SOA
		as.disconnectUser();
		as.connectUser(new SAMLNameID("testSOA"), "password");
	}
	
	/**
	 * Test deleting an attribute value.
	 */
	public void testDeleteAttrValue() {
		//Test deleting an attribute value
		String id = "testID";
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		SAMLNameID subject = new SAMLNameID("ludwig@sics.se");
		String dataType = SAMLAttribute.xmlStringId;
		HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
		otherXMLAttrs.put("xmlns:" + SAMLAttribute.xacmlprofNSPrefix, 
				SAMLAttribute.xacmlprofNS);
		otherXMLAttrs.put(SAMLAttribute.xacmlprofNSPrefix + ":" +
				"DataType", dataType);
		ArrayList<SAMLAttributeValue> values
			= new ArrayList<SAMLAttributeValue>();
		values.add(new SAMLAttributeValue(null, "administrator"));
		SAMLAttribute valueAdmin = new SAMLAttribute(
				id, SAMLAttribute.xacmlNameFormat, null, 
				otherXMLAttrs, values);
		RequestResult result = as.removeAttributeValue(
				subject, soa, valueAdmin);
		Assert.assertTrue(result.success());
	}
	
	/**
	 * Test deleting an attribute.
	 * @throws SQLException 
	 */
	public void testDeleteAttr() throws SQLException {
		HashSet<String> allowedValues = new HashSet<String>();
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		String id = "testID";
		String dataType = SAMLAttribute.xmlStringId;
		
		AttributeDefinition ad = new AttributeDefinition(soa, id, 
				dataType, allowedValues);
		
		//Test deleting an attribute
		RequestResult result = as.deleteAttribute(ad);
		
		Assert.assertTrue(result.success());
		ResultSet res = db.executeQuery("SELECT * FROM " +
					DBConnector.dbName + "." + DBConnector.attrDefTable
					+ " WHERE SOA='testSOA' AND AttrId='testID' AND " 
					+ " DataType='" + dataType + "';");
		Assert.assertFalse(res.next());
	}
	
	/**
	 * Test deleting a user from the authN module.
	 * @throws SQLException 
	 */
	public void testDeleteUser() throws SQLException {
		authN.deleteUser("testSOA", db);
		ResultSet res = db.executeQuery("SELECT * FROM " 
				+ DBConnector.dbName + "." 
				+ authN.table + " WHERE " + authN.login 
				+"='testSOA';");
		Assert.assertFalse(res.next());
	}

	@Override
	protected void tearDown() throws Exception {
		if (--testMethodsLeft == 0) {
		//Delete the database
			db.executeCommand(
					"DROP DATABASE " + DBConnector.dbName + ";");
		}
	}

}
