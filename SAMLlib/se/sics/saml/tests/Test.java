package se.sics.saml.tests;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyName;

import junit.framework.Assert;
import junit.framework.TestCase;

import se.sics.saml.SAMLAttribute;
import se.sics.saml.SAMLAttributeStatement;
import se.sics.saml.SAMLAttributeValue;
import se.sics.saml.SAMLCondition;
import se.sics.saml.SAMLConditions;
import se.sics.saml.SAMLID;
import se.sics.saml.SAMLIDFactory;
import se.sics.saml.SAMLNameID;
import se.sics.saml.SAMLOneTimeUse;
import se.sics.saml.SAMLSigner;
import se.sics.saml.SAMLStatement;
import se.sics.saml.SAMLSubject;
import se.sics.saml.SAMLSubjectConfirmation;
import se.sics.saml.SAMLSubjectConfirmationData;
import se.sics.saml.SignatureVerifier;
import se.sics.saml.SignedSAMLAssertion;
import se.sics.saml.VerificationException;

import se.sics.util.Indenter;
import se.sics.util.XMLInputParser;

import sun.misc.BASE64Encoder;

/**
 * Junit tests
 * 
 * @author Ludwig Seitz
 *
 */
public class Test extends TestCase {

	/**
	 * A parser for parsing XML.
	 */
	XMLInputParser parser;
    
    @Override
    protected void setUp() throws Exception {
    	this.parser = new XMLInputParser(null, null);
    }
    
    /**
     * Test SAMLID, SAMLNameID and SAMLIDFactory classes
     * @throws Exception
     */
    public void testID() throws Exception {
    	
    	//Testing SAMLNameId
    	SAMLNameID nameId = new SAMLNameID("name", true,
    			"qualifier", URI.create("format"), "spId");
    	
    	Assert.assertEquals(nameId.getFormat().toString(), "format");
    	Assert.assertEquals(nameId.getName(), "name");
    	Assert.assertEquals(nameId.getQualifier(), "qualifier");
    	Assert.assertEquals(nameId.getSpProvidedID(), "spId");
    	Assert.assertEquals(nameId.isQualifierType(), true);
    	
    	String nameIdStr = nameId.toString();
    	//Need to add this to make it parsable
    	nameIdStr = nameIdStr.replace("<saml:NameID", 
    			"<saml:NameID xmlns:saml=\"blah.com\"");
    	Document doc = this.parser.parseDocument(nameIdStr);
    	SAMLNameID otherNameId = SAMLNameID.getInstance(
    			doc.getDocumentElement());
    	
    	Assert.assertEquals(nameId.toString(new Indenter()),
    			otherNameId.toString(new Indenter()));
    	
    	SAMLNameID clone = (SAMLNameID)nameId.clone();
    	Assert.assertEquals(nameId.toString(new Indenter()),
    			clone.toString(new Indenter()));
    	
    	Assert.assertTrue(nameId.equals(otherNameId));
    	
    	SAMLID baseId 
    		= SAMLIDFactory.parseSAMLIDNode(doc.getDocumentElement());
    	Assert.assertTrue(baseId.equals(nameId));
    	Assert.assertEquals(baseId.toString(), nameId.toString());
    	
    	//Test with something that isn't a SAMLNameId
    	String xml = "<blah/>";
    	Document blah = this.parser.parseDocument(xml);
    	try {
    		SAMLNameID.getInstance(blah.getDocumentElement());
    	} catch (VerificationException e) {
    		Assert.assertTrue(true);
    		return;
    	}
    	//If we get here then there is something wrong
    	Assert.assertTrue(false);
    }
    
    /**
     * Test SAMLSubject, SAMLSubjectConfirmation, and
     * SAMLSubjectConfimationData classes.
     * @throws Exception 
     */
    public void testSubject() throws Exception {
    	Date nb = new Date();
    	Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date na = cal.getTime();
    	
    	//SubjectConfirmationData without keyInfo
    	SAMLSubjectConfirmationData scd1 = new SAMLSubjectConfirmationData(
    			nb, na, URI.create("recipient"), "inResponseTo", "address");
    	KeyInfoFactory factory = KeyInfoFactory.getInstance("DOM");
    	KeyInfo keyInfo = factory.newKeyInfo(
    			Collections.singletonList(factory.newKeyName("Alice")), 
    					"keyinfo-1");
    	
    	SAMLSubjectConfirmationData scd2 = new SAMLSubjectConfirmationData(
    			null, null, null, null, null, 
    			Collections.singletonList(keyInfo));
    	
    	Assert.assertEquals(scd1.getAddress(), "address");
    	Assert.assertEquals(scd1.getInResponseTo(), "inResponseTo");
    	Assert.assertEquals(scd1.getKeyInfos(), Collections.emptyList());
    	Assert.assertTrue(scd1.getNotAfter().equals(na));
    	Assert.assertTrue(scd1.getNotBefore().equals(nb));
    	Assert.assertEquals(scd1.getRecipient().toString(), "recipient");
    	
    	KeyInfo ki = scd2.getKeyInfos().get(0);
    	Assert.assertEquals(ki.getId(), "keyinfo-1");
    	KeyName kn = (KeyName) ki.getContent().get(0);
    	Assert.assertEquals(kn.getName(), "Alice");
    	
    	String scd1Str = scd1.toString();
    	scd1Str = scd1Str.replace("<saml:SubjectConfirmationData", 
		"<saml:SubjectConfirmationData xmlns:saml=\"blah.com\"");
    	Document doc = this.parser.parseDocument(scd1Str);
    	SAMLSubjectConfirmationData scd3 
    		= SAMLSubjectConfirmationData.getInstance(
    				doc.getDocumentElement());
    	Assert.assertEquals(scd1.toString(), scd3.toString());
    	
    	scd3 = (SAMLSubjectConfirmationData)scd1.clone();
    	Assert.assertEquals(scd1.toString(), scd3.toString());
    	
    	String scd2Str = scd2.toString();
    	scd2Str = scd2Str.replace("<saml:SubjectConfirmationData", 
		"<saml:SubjectConfirmationData xmlns:saml=\"blah.com\"");
    	doc = this.parser.parseDocument(scd2Str);
    	scd3 = SAMLSubjectConfirmationData.getInstance(
    			doc.getDocumentElement());
    	Assert.assertEquals(scd2.toString(), scd3.toString());
    	
    	scd3 = (SAMLSubjectConfirmationData)scd2.clone();
    	Assert.assertEquals(scd2.toString(), scd3.toString());
    	
    	List<SAMLID> si = new ArrayList<SAMLID>();
    	si.add(new SAMLNameID("Alice"));
    	SAMLSubjectConfirmation sc = new SAMLSubjectConfirmation(
    			SAMLSubjectConfirmation.holder, si,
    			Collections.singletonList(scd2));
    	
    	Assert.assertEquals(sc.getMethod().toString(), 
    			SAMLSubjectConfirmation.holder.toString());
    	
    	scd3 = sc.getSubjectConfirmationData().get(0);
    	Assert.assertEquals(scd2.toString(), scd3.toString());
    	Assert.assertEquals(sc.getSubjectIds().get(0).getName(), "Alice");
    	
    	SAMLSubjectConfirmation sc2 = (SAMLSubjectConfirmation)sc.clone();
    	Assert.assertEquals(sc.toString(), sc2.toString());
    	
    	String scStr = sc.toString(new Indenter());
    	scStr = scStr.replace("<saml:SubjectConfirmation ", 
		"<saml:SubjectConfirmation xmlns:saml=\"blah.com\" ");
    	doc = this.parser.parseDocument(scStr);
    	sc2 = SAMLSubjectConfirmation.getInstance(doc.getDocumentElement());
    	Assert.assertEquals(sc.toString(), sc2.toString());
    	
    	SAMLSubject s1 = new SAMLSubject(new SAMLNameID("Bob"));
    	SAMLSubject s2 = new SAMLSubject(new SAMLNameID("Carol"), 
    			Collections.singletonList(sc));
    	
    	Assert.assertEquals(s1.getSubjectId().getName(), "Bob");
    	Assert.assertTrue(s1.getConfs().isEmpty());
    	Assert.assertEquals(s2.getConfs().get(0).toString(), sc2.toString());
    	
    	SAMLSubject s3 = (SAMLSubject)s2.clone();
    	Assert.assertEquals(s2.toString(), s3.toString());
    	String sStr = s2.toString(new Indenter());
    	sStr = sStr.replace("<saml:Subject>", 
		"<saml:Subject xmlns:saml=\"blah.com\">");
    	doc = this.parser.parseDocument(sStr);
    	s3 = SAMLSubject.getInstance(doc.getDocumentElement());
    	Assert.assertEquals(s2.toString(), s3.toString());
    }
    	
    /**
     * Test SAMLConditions, SAMLCondition, and SAMLOneTimeUse classes.
     * @throws Exception 
     */
    public void testConditions() throws Exception {
    	SAMLOneTimeUse otu = new SAMLOneTimeUse();
    	String otuStr = otu.toString(new Indenter());
    	SAMLOneTimeUse otu2 = (SAMLOneTimeUse)otu.clone();
    	Assert.assertEquals(otu2.toString(new Indenter()), otuStr);
    	
    	SAMLCondition cond = (SAMLCondition)otu.clone();
    	Assert.assertEquals(cond.toString(), otu.toString());
    	
    	Date nb = new Date();
    	Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date na = cal.getTime();
		
    	SAMLConditions conds = new SAMLConditions(
    			nb, na, Collections.singletonList(cond));
    	
    	Assert.assertEquals(cond.toString(), 
    			conds.getConditions().get(0).toString());
    	Assert.assertEquals(na.toString(), conds.getNotOnOrAfter().toString());
    	Assert.assertEquals(nb.toString(), conds.getNotBefore().toString());
    	
    	try {
    		conds.checkValidityIntervall();
    	} catch (VerificationException e) {
    		Assert.assertFalse(true);
    	}
    		
    	String condsStr = conds.toString(new Indenter());
    	condsStr = condsStr.replace("<saml:Conditions ", 
		"<saml:Conditions xmlns:saml=\"blah.com\" ");
    	Document doc = this.parser.parseDocument(condsStr);
    	SAMLConditions conds2 = 
    		SAMLConditions.getInstance(doc.getDocumentElement());
    	Assert.assertEquals(conds.toString(), conds2.toString());
    		
    	SAMLConditions conds3 = (SAMLConditions)conds.clone();
    	Assert.assertEquals(conds.toString(), conds3.toString());
    	
    	try {
    		cal.add(Calendar.HOUR, 1);
    		Date later = cal.getTime();
    		conds.checkValidityIntervall(later);
    	} catch (VerificationException e) {
    		Assert.assertEquals(e.getMessage(), "Assertion has expired");
    	} finally {
    		cal.add(Calendar.YEAR, -2);
    		Date earlier = cal.getTime();
    		try {
    			conds.checkValidityIntervall(earlier);
    		} catch (VerificationException e2) {
    			Assert.assertEquals(e2.getMessage(), 
    					"Assertion not yet valid");
    		}
    	}
    }
	
    /**
     * Test SAMLAttributeValue and SAMLAttribute classes.
     * @throws Exception 
     */
    public void testAttribute() throws Exception {
    	SAMLAttributeValue sav = new SAMLAttributeValue(URI.create("type"), 
    			"value");
    	Assert.assertEquals(sav.getType().toString(), "type");
    	Assert.assertEquals(sav.getValue(), "value");
    	SAMLAttributeValue sav2 = (SAMLAttributeValue)sav.clone();
    	Assert.assertEquals(sav.toString(), sav2.toString());
    	
    	String savStr = sav.toString();
    	savStr = savStr.replace("<saml:AttributeValue ", 
		"<saml:AttributeValue xmlns:saml=\"blah.com\" " 
    			+ "xmlns:xsi=\"blubb.com\" ");
    	Document doc = this.parser.parseDocument(savStr);
    	SAMLAttributeValue sav3 = 
    		SAMLAttributeValue.getInstance(doc.getDocumentElement());
    	Assert.assertEquals(sav.toString(), sav3.toString());
    	
    	SAMLAttributeValue sav4 = new SAMLAttributeValue(null, "otherValue");
    	ArrayList<SAMLAttributeValue> avs 
    		= new ArrayList<SAMLAttributeValue>();
    	avs.add(sav4);
    	avs.add(sav);
    	
    	HashMap<String, String> otherXMLAttrs = new HashMap<String, String>();
    	otherXMLAttrs.put("xmlns:saml", "blah.com");
    	otherXMLAttrs.put("xmlns:xsi", "blubb.com");
    	SAMLAttribute attr = new SAMLAttribute(
    			"name", URI.create("nameFormat"), "friendlyName", 
    			otherXMLAttrs, avs);
    	Assert.assertEquals(attr.getAttributeValues().get(0).toString(),
    			sav4.toString());
    	Assert.assertEquals(attr.getAttributeValues().get(1).toString(), 
    			sav.toString());
    	Assert.assertEquals(attr.getFriendlyName(), "friendlyName");
    	Assert.assertEquals(attr.getName(), "name");
    	Assert.assertEquals(attr.getNameFormat().toString(), "nameFormat");
    	Assert.assertEquals(attr.getOtherXMLAttr("xmlns:saml"), "blah.com");
    	Assert.assertEquals(attr.getOtherXMLAttr("xmlns:xsi"), "blubb.com");
    	
    	SAMLAttribute attr2 = (SAMLAttribute)attr.clone();
    	Assert.assertEquals(attr.getName(), attr2.getName());
    	Assert.assertEquals(attr.getFriendlyName(), attr2.getFriendlyName());
    	Assert.assertEquals(attr.getNameFormat().toString(),
    			attr2.getNameFormat().toString());
    	Assert.assertEquals(attr.getAttributeValues().get(0).toString(), 
    			attr2.getAttributeValues().get(0).toString());
    	Assert.assertEquals(attr.getAttributeValues().get(1).toString(), 
    			attr2.getAttributeValues().get(1).toString());
    	Assert.assertEquals(attr.getOtherXMLAttr("xmlns:saml"), 
    			attr2.getOtherXMLAttr("xmlns:saml"));
    	Assert.assertEquals(attr.getOtherXMLAttr("xmlns:xsi"), 
    			attr2.getOtherXMLAttr("xmlns:xsi"));
    	
    	String attrStr = attr.toString(new Indenter());
    	doc = this.parser.parseDocument(attrStr);
    	SAMLAttribute attr3 = 
    	    		SAMLAttribute.getInstance(doc.getDocumentElement());
    	Assert.assertEquals(attr.getName(), attr3.getName());
    	Assert.assertEquals(attr.getFriendlyName(), attr3.getFriendlyName());
    	Assert.assertEquals(attr.getNameFormat().toString(),
    			attr3.getNameFormat().toString());
    	Assert.assertEquals(attr.getAttributeValues().get(0).toString(), 
    			attr3.getAttributeValues().get(0).toString());
    	Assert.assertEquals(attr.getAttributeValues().get(1).toString(), 
    			attr3.getAttributeValues().get(1).toString());
    	Assert.assertEquals(attr.getOtherXMLAttr("xmlns:saml"), 
    			attr3.getOtherXMLAttr("xmlns:saml"));
    	Assert.assertEquals(attr.getOtherXMLAttr("xmlns:xsi"), 
    			attr3.getOtherXMLAttr("xmlns:xsi"));    	
    }
    
    /**
     * Test SAMLStatement and SAMLAttributeStatement classes.
     * @throws Exception 
     */
    public void testStatement() throws Exception {
    	SAMLAttribute attr = new SAMLAttribute("name", null, 
    			null, null, Collections.singletonList(
    					new SAMLAttributeValue(null, "value")));
    	SAMLAttributeStatement sas = new SAMLAttributeStatement(
    			Collections.singletonList(attr));
    	
    	SAMLStatement sst = (SAMLStatement)sas.clone();
    	Assert.assertEquals(sas.toString(), sst.toString());
    	
    	String sasStr = sst.toString(new Indenter());
    	sasStr = sasStr.replace("<saml:AttributeStatement>", 
		"<saml:AttributeStatement xmlns:saml=\"blah.com\">");
    	Document doc = this.parser.parseDocument(sasStr);
    	SAMLAttributeStatement sas2 = 
    		SAMLAttributeStatement.getInstance(doc.getDocumentElement());
    	Assert.assertEquals(sas.toString(), sas2.toString());

    }
    
    /**
     * Test SignedSAMLAssertion, SAMLSigner, and SignatureVerifier classes.
     * @throws Exception 
     */
    public void testAssertion() throws Exception {
		SAMLNameID issuer = new SAMLNameID("AssertionServer");
		SAMLNameID subjectName = new SAMLNameID("Alice");
		SAMLSubject subject = new SAMLSubject(subjectName);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date notOnOrAfter = cal.getTime();
		SAMLConditions conditions 
			= new SAMLConditions(null, notOnOrAfter, null);
		
		ArrayList<SAMLAttributeValue> values 
			= new ArrayList<SAMLAttributeValue>();
		values.add(new SAMLAttributeValue(null, "member"));
		SAMLAttribute valueMember = new SAMLAttribute(
				"group:SWiN", null, null, null, values);
		ArrayList<SAMLAttribute> attrs = new ArrayList<SAMLAttribute>();
		attrs.add(valueMember);
		SAMLAttributeStatement statement = new SAMLAttributeStatement(attrs);  
		ArrayList<SAMLStatement> statements = new ArrayList<SAMLStatement>();  
		statements.add(statement);
		
		XMLInputParser parser = new XMLInputParser(null, null);
		File pkcs12File1 = new File("resources/cert.p12");    
        KeyStore ks1;
        X509Certificate cert = null;
        PrivateKey privateKey = null;
  
        ks1 = KeyStore.getInstance("PKCS12");
        FileInputStream keyFile1 = new FileInputStream(pkcs12File1);
        ks1.load(keyFile1, "password".toCharArray());
        keyFile1.close();
        Enumeration<String> e = ks1.aliases();
        String alias1 = e.nextElement();
        cert = (X509Certificate) ks1.getCertificate(alias1);

        privateKey 
        	= (PrivateKey)ks1.getKey(alias1, "password".toCharArray());            

		
		SAMLSigner signer = new SAMLSigner(parser, privateKey, cert);
		
		SignedSAMLAssertion assertion = new SignedSAMLAssertion(
				issuer, subject, conditions, statements, signer);

		Document assertionXML = parser.parseDocument(assertion.toString(
				new Indenter()));		

		SignatureVerifier.verifySAMLSignature(assertionXML);
		
		X509Certificate cert2 
			= SignatureVerifier.getCertFromDSig(assertionXML);
		X509Certificate cert3 = SignatureVerifier.getCertFromDSig(assertion);
		
		BASE64Encoder enc = new BASE64Encoder();
		Assert.assertEquals(enc.encode(cert.getEncoded()), 
				enc.encode(cert2.getEncoded()));
		Assert.assertEquals(enc.encode(cert.getEncoded()), 
				enc.encode(cert3.getEncoded()));
		
    }
}
