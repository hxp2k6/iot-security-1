/*
 * @(#)SAMLConditions.java
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

package se.sics.saml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.sics.util.DateUtils;
import se.sics.util.Indenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Conditions element as defined in section 2.5 of the SAML 2.0 standard
 * @author Ludwig Seitz
 *
 */
public class SAMLConditions implements Cloneable {
   
	/**
     * A static variable for system independent newline characters.
     */
    private static final String nl = System.getProperty("line.separator");
    
	/**
	 * The conditions attached
	 */
	private final List<SAMLCondition> conditions;
	
	/**
	 * Not valid before this date + time 
	 */
	private final Date notBefore;
	
	/**
	 * Not valid on or after this date + time
	 */
	private final Date notOnOrAfter;
	
	/**
	 * Constructor.
	 * 
	 * @param notBefore  conditions not valid before this date, can be null
	 * @param notOnOrAfter  conditions not valid on or after this date, 
	 * 						can be null
	 * @param conditions  the conditions, can be null
	 */
	public SAMLConditions(Date notBefore, Date notOnOrAfter, 
			List<SAMLCondition> conditions) {
		if (notBefore != null) {
			this.notBefore = (Date)notBefore.clone();
		} else {
			this.notBefore = null;
		}
		if (notOnOrAfter != null) {
			this.notOnOrAfter = (Date)notOnOrAfter.clone();
		} else {
			this.notOnOrAfter = null;
		}
		if (conditions == null || conditions.isEmpty()) {
			this.conditions = Collections.emptyList();
		} else {
			this.conditions = new ArrayList<SAMLCondition>();
			for (SAMLCondition cond : conditions) {
				if (cond instanceof SAMLOneTimeUse) {
					this.conditions.add(new SAMLOneTimeUse());
				} else { //TODO: Add other condition types
				}
			}
		}
	}

	/**
	 * @return  the list of conditions
	 */
	public List<SAMLCondition> getConditions() {
		return Collections.unmodifiableList(this.conditions);
	}

	/**
	 * @return  the NotBefore attribute
	 */
	public Date getNotBefore() {
		return this.notBefore;
	}

	/**
	 * @return  the NotOnOrAfter attribute
	 */
	public Date getNotOnOrAfter() {
		return this.notOnOrAfter;
	}
	
	@Override
	public String toString() {
		return toString(new Indenter());
	}
	
	/**
	 * Create a String containing the XML representation of this Conditions
	 * element.
	 * 
     * @param in  An indenter for correct XML indentation
     * @return This Conditions-element XML representation
	 */
	public String toString(Indenter in) {
		String indent = in.makeString();
        in.in();
		String conditions = indent + "<saml:Conditions";
		if (this.notBefore != null) {
			conditions += " NotBefore=\"" + DateUtils.toString(this.notBefore)
				+ "\"";
		}
		if (this.notOnOrAfter != null) {
			conditions += " NotOnOrAfter=\"" 
				+ DateUtils.toString(this.notOnOrAfter) + "\"";
		}
		if (this.conditions.isEmpty()) {
			conditions += "/>";
		} else {
			conditions += ">" + nl;
			for (SAMLCondition cond : this.conditions) {
				conditions += cond.toString(in) + nl;
			}
			conditions += indent + "</saml:Conditions>";
		}
		in.out();
		return conditions;
	}
	
	/**
	 * Checks the NotBefore and NotOnOrAfter conditions (if present) based on
	 * the current time.
	 *
	 * @throws VerificationException 
	 */
	public void checkValidityIntervall() throws VerificationException {
		checkValidityIntervall(new Date());
	}
	
	/**
	 * Checks the NotBefore and NotOnOrAfter conditions (if present).
	 * 
	 * @param date  the date for which the validity is checked
	 * @throws VerificationException 
	 */
	public void checkValidityIntervall(Date date) throws VerificationException {
		if (this.notBefore == null && this.notOnOrAfter == null) {
			return;
		}
		if (this.notBefore != null) {
			if (date.before(this.notBefore)) {
				throw new VerificationException("Assertion not yet valid");
			}
		}
		if (this.notOnOrAfter != null) {
			if (date.before(this.notOnOrAfter)) {
				return;
			}
			throw new VerificationException("Assertion has expired");
		}
	}
	
	/**
	 * @param root  the XML node containing the Conditions element
	 * @return  the conditions element parsed from the node
	 * 
	 * @throws VerificationException
	 */
	public static SAMLConditions getInstance(Node root) 
			throws VerificationException {
		 // check if this really is a Conditions element
        if (root.getNodeType() != Node.ELEMENT_NODE
                || !root.getLocalName().equals("Conditions")) {
            throw new VerificationException("Can't create a Conditions-element" 
            		+ " from a " + root.getLocalName() + " element");
        }

        // prepare the necessary variables
        Date notBefore = null;
        Date notOnOrAfter = null;       
        List<SAMLCondition> conditions = new ArrayList<SAMLCondition>();

        NamedNodeMap attrs = root.getAttributes();

        for (int i=0; i<attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            if (attr.getLocalName().equals("NotBefore")) {
                notBefore = DateUtils.parseInstant(attr.getNodeValue());
            } else if (attr.getLocalName().equals("NotOnOrAfter")) {
                notOnOrAfter = DateUtils.parseInstant(attr.getNodeValue());
            }            
        }
        
        NodeList children = root.getChildNodes();
        for (int i=0; i<children.getLength();i++) {
        	Node child = children.item(i);
        	if (child.getNodeType() == Node.ELEMENT_NODE) {
        		if (child.getLocalName().equals("OneTimeUse")) {
        			conditions.add(new SAMLOneTimeUse());
        		} else {
        			//TODO: implement other condition types
        		}
        	}
        }
        return new SAMLConditions(notBefore, notOnOrAfter, conditions);
	}
	
	
	@Override
	public Object clone() {
		List<SAMLCondition> newList = new ArrayList<SAMLCondition>();
		for (SAMLCondition cond : this.conditions) {
			newList.add((SAMLCondition)cond.clone());
		}
		return new SAMLConditions((Date)this.notBefore.clone(), 
				(Date)this.notOnOrAfter.clone(), newList);
	}
}
