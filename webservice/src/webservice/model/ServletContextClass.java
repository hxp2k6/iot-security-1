package webservice.model;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import assertionserver.control.AssertionServerController;
import se.sics.assertionServer.AssertionRequest;
import se.sics.assertionServer.AssertionServer;
import se.sics.assertionServer.AssertionServerSchemaConfiguration.ConfigurationException;
import se.sics.assertionServer.AttributeDefinition;
import se.sics.assertionServer.RequestResult;
import se.sics.assertionServer.accessControl.AccessControlModule;
import se.sics.assertionServer.accessControl.BasicAccessControl;
import se.sics.assertionServer.auth.PasswordAuth;
import se.sics.assertionServer.databaseAccess.SQLConnector;
import se.sics.saml.SAMLAttribute;
import se.sics.saml.SAMLAttributeValue;
import se.sics.saml.SAMLNameID;
import se.sics.saml.SignedSAMLAssertion;
import se.sics.saml.VerificationException;
import se.sics.util.Indenter;
import se.sics.util.XMLInputParser;


public class ServletContextClass implements ServletContextListener {
		
	@Override
	public void contextInitialized(ServletContextEvent arg0){
		try {
			initDatabase();
		} catch (UnrecoverableKeyException | NoSuchAlgorithmException
				| CertificateException | KeyStoreException | SQLException
				| IOException | ConfigurationException
				| ParserConfigurationException | SAXException
				| VerificationException | MarshalException
				| XMLSignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void initDatabase()	throws NoSuchAlgorithmException, 
	SQLException, CertificateException, UnrecoverableKeyException, KeyStoreException, IOException, 
	ConfigurationException, ParserConfigurationException, SAXException, VerificationException, MarshalException, XMLSignatureException{
		//Use default values for connecting to the database
		SQLConnector db =  new SQLConnector(null, null, null);
		AccessControlModule pdp = new BasicAccessControl();
		//Use the default hash algorithm on the passwords
		PasswordAuth auth = new PasswordAuth(null);
		List<X509Certificate> certs = new ArrayList<X509Certificate>();
			
		InputStream pathCacert= this.getClass().getResourceAsStream("/resources/testData/Certificates/cacert.pem");
		
		System.out.println("\n\n");
		//System.out.println("Root Cert File: " + rootCertFile.getAbsolutePath());
		BufferedInputStream bis = new BufferedInputStream(pathCacert);
			
		CertificateFactory certFact = CertificateFactory.getInstance("X.509");
			
		//BufferedInputStream bis = new BufferedInputStream(
		//		new FileInputStream(rootCertFile));
					
		X509Certificate rootCert
		= (X509Certificate) certFact.generateCertificate(bis);
		certs.add(rootCert);
			
			
		URL URLpathAserver = this.getClass().getResource("/resources/testData/Certificates/aserver.p12");
		
		String pathAserver = URLpathAserver.getPath();
		System.out.println("\n\n\n\n\n" + "Path server " + pathAserver + "\n\n\n\n\n");
			
		AssertionServer sas = new AssertionServer(db, pdp, auth,
				pathAserver,
				"password", certs, 1);
		
		AssertionServerController asc = new AssertionServerController();
		asc.setAssertionServer(sas);

		/*sas = new AssertionServer(db, pdp, auth,
				"/resources/testData/Certificates/aserver.p12",
				"password", certs, 1);*/
			
			//if(sas == null)
			//	System.out.println("eh null no text example");
			
			//AssertionServer sas = new AssertionServer(db, pdp, auth,
			//		"resources/testData/Certificates/aserver.p12",
			//		"password", certs, 1);
			
		HashSet<String> allowedValues = new HashSet<String>();
		allowedValues.add("member");
		allowedValues.add("administrator");
		SAMLNameID subject = new SAMLNameID("ludwig@sics.se");
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		String id = "testID";
		String dataType = SAMLAttribute.xmlStringId;
		
		//Signalizes start of test
		System.out.println("Teste");
		
		//Add required XML-attributes for the SAML-XACML profile
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
		values.clear();
		values.add(new SAMLAttributeValue(null, "administrator"));
		SAMLAttribute valueAdmin = new SAMLAttribute(
				id, SAMLAttribute.xacmlNameFormat, null,
				otherXMLAttrs, values);
		AttributeDefinition ad = new AttributeDefinition(soa, id,
				dataType, allowedValues);
			//Create the database
			//db.init("password");
		
		try{
			db.init("rootPwd");
			auth.init(db);
		}
		catch(Exception e){
			
		}
		

		//Create a new user
		auth.newUser(ad.getSOA().getName(), "password", db);
		//Authenticate the user
		RequestResult result = sas.connectUser(ad.getSOA(), "password");
		if (!result.success()) {
			System.out.println(result.getFailureReasons());
		}
		//Create an attribute
		result = sas.createAttribute(ad);
		if (!result.success()) {
			System.out.println("..." + result.getFailureReasons());
		}
		//Adding an attribute value
		result = sas.addAttributeValue(subject, soa, valueMember);
		if (!result.success()) {
			System.out.println("..." + result.getFailureReasons());
		}
		//Update an attribute value
		result = sas.updateAttributeValue(subject, soa, valueMember,
			"administrator");
		if (!result.success()) {
			System.out.println("..." + result.getFailureReasons());
		}
		//Get assertion
		AssertionRequest request = new AssertionRequest(subject, soa, valueAdmin);
		result = sas.getAssertions(request);
		SignedSAMLAssertion assertion = result.getResults().get(0);
		String assertionXML = assertion.toString(new Indenter());
		System.out.println(assertionXML);
		//Parse an assertion
		XMLInputParser parser = new XMLInputParser(null, null);
		Document doc = parser.parseDocument(assertionXML);
		SignedSAMLAssertion parsedAssertion
		= SignedSAMLAssertion.getInstance(doc.getDocumentElement());
		//Test validity of conditions
		//if (!parsedAssertion.getConditions().checkValidityIntervall()) {
		//if (!parsedAssertion.getConditions().checkValidityIntervall()){
		// Failed
		//} else {
		// Success
		//}
		//Delete an attribute value
		result = sas.removeAttributeValue(subject, soa, valueAdmin);
		if (!result.success()) {
			System.out.println(result.getFailureReasons());
		}
		//Delete an attribute
		result = sas.deleteAttribute(ad);
		if (!result.success()) {
			System.out.println("..." + result.getFailureReasons());
		}
		//Delete a user
		auth.deleteUser(ad.getSOA().getName(), db);
	}
		
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}
	
}


