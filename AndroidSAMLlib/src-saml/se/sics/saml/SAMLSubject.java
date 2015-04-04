
/*
 * @(#)SAMLSubject.java
 *
 * Copyright 2005-2006 Swedish Institute of Computer Science All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import se.sics.util.Indenter;

/**
 * A class to encode SAML subjects according to the definition in 
 * section 2.4.1 of the SAML 2.0 standard. 
 * 
 * @author Ludwig Seitz
 *
 */
public class SAMLSubject implements Cloneable {

    /**
     * A static variable for system independent newline characters.
     */
    private static final String nl = System.getProperty("line.separator");
        
    /**
     * The optional subject identifier.
     */
    private SAMLID subjectId;
    
    /**
     * The optional SubjectConfirmations.
     */
    private List<SAMLSubjectConfirmation> confs;

    /**
     * Constructor using only a SAML ID.
     * 
     * @param subjectId  The SAML ID identifying the Subject.
     */
    public SAMLSubject(SAMLID subjectId) {
        if (subjectId == null) {
            throw new IllegalArgumentException("subjectId must be non-null");
        }
        this.subjectId = (SAMLID)subjectId.clone();
        this.confs = Collections.emptyList();
    }
    
    /**
     * Constructor for a Subject with either SAML ID or SubjectConfirmation
     * or both.
     * @param subjectId  The SAML ID identifying the Subject, may be null
     *                   (but then subjectConf may not be null or empty).
     * @param subjectConfs  The SubjectConfirmations, may be null (but then
     *                   subjectId must be non-null).
     */
    public SAMLSubject(SAMLID subjectId, 
    		List<SAMLSubjectConfirmation> subjectConfs) {
        if (subjectId == null && subjectConfs == null) {
            throw new IllegalArgumentException("At least one of subjectId or" 
                    + "subjectConfs must be non-null");
        }
        if (subjectId != null) {
            this.subjectId = (SAMLID)subjectId.clone();
        }
        if (subjectConfs != null) {
            if (subjectConfs.isEmpty()) {
                throw new IllegalArgumentException("subjectConfs must null" 
                        + " or non-empty");
            }
            this.confs = new ArrayList<SAMLSubjectConfirmation>();
            //check it's really SAMLSubjectConfirmations and copy them.
            Iterator<SAMLSubjectConfirmation> iter = subjectConfs.iterator();
            while (iter.hasNext()) {
                this.confs.add((SAMLSubjectConfirmation)iter.next().clone());
            }
        } else {
            this.confs = Collections.emptyList();
        }
    }

    /**
     * @return  the SubjectConfirmations as a <code>List</code> of
     *           <code>SAMLSubjectConfirmation</code> objects, can be empty.
     */
    public List<SAMLSubjectConfirmation> getConfs() {
       return Collections.unmodifiableList(this.confs);
    }

    /**
     * @return the subjectID or null.
     */
    public SAMLID getSubjectId() {
        if (this.subjectId != null) {
            return (SAMLID)this.subjectId.clone();
        }
        return null;
    }
    
	@Override
	public String toString() {
		return toString(new Indenter());
	}
    
    /**
     * Create a String containing the XML representation of this Subject.
	 *     
	 * @param in  An indenter for correct XML indentation
     * @return  The XML encoded representation of this Subject
     */
	public String toString(Indenter in) {
        String indent = in.makeString();
        in.in();
        String res = indent + "<saml:Subject>" + nl;
        if (this.subjectId != null) {
            res += this.subjectId.toString(in) + nl;
        }
        Iterator<SAMLSubjectConfirmation> iter = this.confs.iterator();
        while(iter.hasNext()) {
            res += iter.next().toString(in) + nl;
        }
        res += indent + "</saml:Subject>";
        in.out();
        return res;
    }
    
    /**
     * @return  a deep-copy of this SAMLSubject.
     */
    @Override
    public Object clone() {
        try {
            SAMLSubject clone = (SAMLSubject)super.clone();
            clone.subjectId = (SAMLID)this.subjectId.clone();
            clone.confs = new ArrayList<SAMLSubjectConfirmation>();
            Iterator<SAMLSubjectConfirmation> iter = this.confs.iterator();
            while(iter.hasNext()) {
                clone.confs.add(
                        (SAMLSubjectConfirmation)iter.next().clone());
            }
            return clone;
        } catch (CloneNotSupportedException e1) {//this should never happen
            throw new RuntimeException("Couldn't clone SAMLSubject");
        }
    }

    /**
     * Create a SAMLSubject from a node object.
     * 
     * @param root  The xml node containing the subject.
     * 
     * @return  The SAMLSubject encoded by the node.
     * @throws VerificationException 
     */
    public static SAMLSubject getInstance(Node root) 
            throws VerificationException {
        // check if this really is a Subject
        if (root.getNodeType() != Node.ELEMENT_NODE
                || !root.getLocalName().equals("Subject")) {
            throw new VerificationException("Can't create a Subject from a "
                    + root.getLocalName() + " element");
        }

        // prepare the necessary variables
        SAMLID subjectId = null;
        List<SAMLSubjectConfirmation> confs 
        	= new ArrayList<SAMLSubjectConfirmation>();
       
        //parse the children
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
            	if (node.getLocalName().equals("SubjectConfirmation")) {
            		confs.add(SAMLSubjectConfirmation.getInstance(node));
            	} else { //Try to parse to SAMLID            		
            		SAMLID samlID = SAMLIDFactory.parseSAMLIDNode(node);
            		if (samlID != null) {
            			subjectId = samlID;
            		}
            	}
            }
        }
        if (confs.isEmpty()) {
            return new SAMLSubject(subjectId, null);
        } 
        return new SAMLSubject(subjectId, confs);
    }
    
}
