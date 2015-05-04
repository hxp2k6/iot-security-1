/*
 * @(#)X509KeySelector.java
 *
 * Copied from http://java.sun.com/developer/technicalArticles/xml/dig_signature_api/
 * until a suitable replacement is found/made.
 * 
 */

package se.sics.util;

import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Iterator;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;

/**
 * Implementation of KeySelector.
 * 
 * Returns the public key from the first X.509 certificate it finds in the 
 * X509Data. 
 *  
 * @author Sean Mullan
 *
 */
public class X509KeySelector extends KeySelector {
	
	/**
	 * Select first key from the keyInfo parameter.
	 */
    @Override
	public KeySelectorResult select(KeyInfo keyInfo,
                                    KeySelector.Purpose purpose,
                                    AlgorithmMethod method,
                                    XMLCryptoContext context)
        	throws KeySelectorException {
        Iterator<XMLStructure> ki = keyInfo.getContent().iterator();
        while (ki.hasNext()) {
            XMLStructure info = ki.next();
            if (!(info instanceof X509Data)) {
                continue;
            }
            X509Data x509Data = (X509Data) info;
            Iterator xi = x509Data.getContent().iterator();
            while (xi.hasNext()) {
                Object o = xi.next();
                if (!(o instanceof X509Certificate))
                    continue;
                final PublicKey key = ((X509Certificate)o).getPublicKey();
                // Make sure the algorithm is compatible
                // with the method.
                if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
                    return new KeySelectorResult() {
                        @Override
						public Key getKey() { return key; }
                    };
                }
            }
        }
        throw new KeySelectorException("No key found!");
    }

    /**
     * Compares two signature algorithm names in different formats.
     * 
     * @param algURI   either "DSA_SHA1" or "RSA_SHA1"
     * @param algName  either "DSA" or "RSA"
     * 
     * @return  true if the algorithms are equivalent
     */
    static boolean algEquals(String algURI, String algName) {
        if ((algName.equalsIgnoreCase("DSA") &&
            algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) ||
            (algName.equalsIgnoreCase("RSA") &&
            algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1))) {
            return true;
        }
		return false;
    }
}
