/*
 * @(#)AssertionServerAction.java
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

package se.sics.assertionServer.actions;

import se.sics.assertionServer.AttributeDefinition;
import se.sics.saml.SAMLID;

import java.util.Collections;
import java.util.Set;

/**
 * Class for collecting information about an action (e.g. create/delete 
 * attribute, set/delete attribute value, get attribute values) that the
 * assertion server can perform. 
 *  
 * @author Ludwig Seitz
 *
 */
public class AssertionServerAction {
	
	/**
	 * The action of asking for attribute assertions
	 */
	public static final String QUERY = "assertionServerAction:query";
	
	/**
	 * The action of creating a new attribute type
	 */
	public static final String CREATE = "assertionServerAction:create";
	
	/**
	 * The action of deleting an attribute type and all associated values
	 */
	public static final String DELETE = "assertionServerAction:delete";
	
	/**
	 * The action of updating an attribute value for a subject
	 */
	public static final String UPDATE = "assertionServerAction:update";

	/**
	 * The action of adding a new attribute value to a subject
	 */
	public static final String ADD = "assertionServerAction:add";
	
	/**
	 * The action of removing an attribute value from a subject
	 */
	public static final String REMOVE = "assertionServerAction:remove";
	
	
	/**
	 * The attribute(s) that are target of the action
	 */
	private final Set<AttributeDefinition> attrs;
	
	/**
	 * The action identifier
	 */
	private final String actionId;
	
	/**
	 * The identifier of the subject that is target of the action.
	 * Can be null if this action doesn't target a specific subject.
	 */
	private final SAMLID subjectId;
	
	
	/**
	 * Constructor. 
	 * 
	 * @param target  the attribute(s) that are target of the action
	 * @param action  the action identifier
	 * @param subjectId  the subject identifier, can be null if the
	 * 					action doesn't target a specific subject.  
	 */
	public AssertionServerAction(Set<AttributeDefinition> target, 
			String action, SAMLID subjectId) {
		this.attrs = Collections.unmodifiableSet(target);
		this.actionId = action;		
		this.subjectId = subjectId;
	}

	/**
	 * @return  the attribute(s) that are target of the action
	 */
	public Set<AttributeDefinition> getAttrs() {
		return this.attrs;
	}

	/**
	 * @return  the action identifier
	 */
	public String getActionId() {
		return this.actionId;
	}
	
	/**
	 * @return  the subject identifier
	 */
	public SAMLID getSubjectId() {
		return this.subjectId;
	}
	
}

