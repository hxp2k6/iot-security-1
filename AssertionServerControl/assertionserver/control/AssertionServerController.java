package assertionserver.control;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import my.test.TestExample;
import se.sics.assertionServer.AssertionRequest;
import se.sics.assertionServer.AssertionServer;
import se.sics.assertionServer.AssertionServerSchemaConfiguration.ConfigurationException;
import se.sics.assertionServer.RequestResult;
import se.sics.saml.SAMLAttribute;
import se.sics.saml.SAMLAttributeValue;
import se.sics.saml.SAMLNameID;
import se.sics.saml.SignedSAMLAssertion;
import se.sics.saml.VerificationException;
import se.sics.util.Indenter;
import webservice.model.JSONAssertion;
import webservice.model.JSONRequestResult;

public class AssertionServerController{
	
	String assertionXML;
	public static AssertionServer sas;
	
	public void setAssertionServer(AssertionServer server){
		sas = server;
	}
	
	public void init() throws UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, KeyStoreException, SQLException, IOException, ConfigurationException, ParserConfigurationException, SAXException, VerificationException, MarshalException, XMLSignatureException{

		System.out.println(sas);
		if(sas == null)
			System.out.println("eh null po");
		SAMLNameID subject = new SAMLNameID("ludwig@sics.se");
		String soaStr = "testSOA";
		SAMLNameID soa = new SAMLNameID(soaStr);
		String id = "testID";
		HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
		otherXMLAttrs.put("xmlns:" + SAMLAttribute.xacmlprofNSPrefix,
				SAMLAttribute.xacmlprofNS);
		ArrayList<SAMLAttributeValue> values
		= new ArrayList<SAMLAttributeValue>();
		SAMLAttribute valueAdmin = new SAMLAttribute(
				id, SAMLAttribute.xacmlNameFormat, null,
				otherXMLAttrs, values);
		AssertionRequest request = new AssertionRequest(subject, soa, valueAdmin);
		JSONRequestResult result = sas.getAssertions(request);
		JSONAssertion assertion = result.getResults().get(0);
		//assertionXML = assertion.toString(new Indenter());
		assertionXML = assertion.getJSON().toString();
	}
	
	public String getAssertion(){
		return assertionXML;
	}

}
