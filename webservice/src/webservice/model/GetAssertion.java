package webservice.model;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import se.sics.assertionServer.AssertionServerSchemaConfiguration.ConfigurationException;
import se.sics.saml.VerificationException;
import assertionserver.control.AssertionServerController;


@XmlRootElement
@Path("/assertion")
public class GetAssertion {
	
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public String getXML() throws UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, 
	KeyStoreException, SQLException, IOException, ConfigurationException, ParserConfigurationException, 
	SAXException, VerificationException, MarshalException, XMLSignatureException{
		AssertionServerController assertionServerController = new AssertionServerController();
		assertionServerController.init();
		String assertion = assertionServerController.getAssertion();
		//String assertion = "<xml>teste primeiro xml no assertion</xml>";
		return assertion;
	}
	
	@GET
	@Produces({ MediaType.TEXT_XML })
	public String getHTML() throws UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, 
	KeyStoreException, SQLException, IOException, ConfigurationException, ParserConfigurationException, SAXException, 
	VerificationException, MarshalException, XMLSignatureException {
		AssertionServerController assertionServerController = new AssertionServerController();
		String assertion = assertionServerController.getAssertion();
		//String assertion = "<xml>teste segundo xml no assertion</xml>";
		return assertion;
	}
}
