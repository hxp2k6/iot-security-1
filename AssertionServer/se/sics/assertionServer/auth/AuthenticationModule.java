/*
 * @(#)AuthenticationModule.java
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
package se.sics.assertionServer.auth;

import java.sql.SQLException;

import se.sics.assertionServer.databaseAccess.DBConnector;

/**
 * Interface for authentication modules to be used with the Assertion Server.
 * 
 * @author Ludwig Seitz
 */
public interface AuthenticationModule {
	
	/**
	 * Perform an authentication based on a userId and an authentication
	 * token.
	 * 
	 * @param userId  the user to be authenticated
	 * @param token  the authentication token
	 * @param database  a database connector
	 * @return  true if the user could be authenticated, false otherwise
	 * @throws SQLException 
	 */
	public boolean doAuth(String userId, Object token, 
			DBConnector database) throws SQLException;
	

	/**
	 * Create a new user in the database and store the corresponding
	 * authentication token (e.g. password, certificate).
	 * 
	 * @param userId  the user to be authenticated
	 * @param token  the authentication token
	 * @param database  a database connector
	 * @throws SQLException 
	 */
	public void newUser(String userId, Object token, 
			DBConnector database) throws SQLException;
	
	/**
	 * Delete an existing user from the authentication table in the
	 * database.
	 * @param userId  the user to be deleted
	 * @param database  a database connector
	 * @throws SQLException 
	 */
	public void deleteUser(String userId, DBConnector database) 
		throws SQLException;
	
	/**
	 * Create the database table for this authentication module.
	 * 
	 * @param database  a database connector
	 * @throws SQLException
	 */
	public void init(DBConnector database) throws SQLException;
}
