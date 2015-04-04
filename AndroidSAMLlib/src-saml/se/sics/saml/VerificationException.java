
/*
 * @(#)VerificationException.java
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

/**
 * An exception that represents a failure to verify SAML control
 * data.
 *
 * @author Erik Rissanen
 * @version 1.0
 * @since 1.0
 */
public class VerificationException extends java.lang.Exception {
	
	/**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = -3031411844572543198L;


	/**
	 * Constructor for an empty exception
	 */
	public VerificationException() {//empty exception
    }

	/**
	 * Constructor for an exception with a message
	 * 
	 * @param msg  the message
	 */
    public VerificationException(String msg) {
        super(msg);
    }

    /**
     * Constructor for an exception with a message and a 
     * cause.
     * 
     * @param msg  the message
     * @param e  the cause
     */
    public VerificationException(String msg, Throwable e) {
        super(msg, e);
    }


    /**
     * Constructor for an exception with a cause.
     * 
     * @param e  the cause
     */
    public VerificationException(Throwable e) {
        super(e);
    }
}
