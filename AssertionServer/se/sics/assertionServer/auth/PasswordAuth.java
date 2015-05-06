/*
 * @(#)PasswordAuth.java
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.ResultSet;
import java.sql.SQLException;

import se.sics.assertionServer.databaseAccess.DBConnector;

import sun.misc.BASE64Encoder;

/**
 * Simple login/password authentication class.
 * 
 * @author Ludwig Seitz
 *
 */
public class PasswordAuth implements AuthenticationModule {

	/**
	 * The default login/password table
	 */
	public String table = "SimpleAuthData";
	
	/**
	 * The default login column name
	 */
	public String login = "Login";
	
	/**
	 * The default password column name
	 */
	public String password = "password";
	
	/**
	 * The default password hashing algorithm
	 */
	private MessageDigest hash;
	
	/**
	 * The Base64 encoder
	 */
	private static BASE64Encoder base64enc = new BASE64Encoder();
	
	/**
	 * Select query template for getting the password
	 */
	private String selectQuery = "SELECT * FROM 1.2 WHERE 3='4';";
	
	/**
	 * Insert command template for setting a new user
	 */
	private String insertCommand = "INSERT INTO 1.2 VALUES ('3','4');";
	
	/**
	 * Delete command template for deleting a user
	 */
	private String deleteCommand = "DELETE FROM 1.2 WHERE 3='4';";
	
	/**
	 * Constructor. Lets you select a password hashing algorithm.
	 * Default is SHA-512.
	 * 
	 * @param hashAlg  a password hashing algorithm or null for default
	 * @throws NoSuchAlgorithmException
	 */
	public PasswordAuth(String hashAlg) throws NoSuchAlgorithmException {
		this.hash = MessageDigest.getInstance((hashAlg == null) 
					? "SHA-512" : hashAlg);
	}
	
	
	@Override
	public void init(DBConnector database) throws SQLException {
		String createLoginPwd = "CREATE TABLE " + DBConnector.dbName + "."
			+ this.table + "(" 
			+ this.login + " VARCHAR(255)  NOT NULL, " + this.password 
			+ " VARCHAR(255) NOT NULL, PRIMARY KEY (" + this.login +"));";
		database.executeCommand(createLoginPwd);
	}
	
	/**
	 * Checks a login/password versus a database table.
	 * 
	 * @param login  the login of the user
	 * @param password   the password submitted by the user
	 * @param database  the database storing the logins/passwords
	 * 
	 * @return  true if the password is correct false otherwise
	 * @throws SQLException 
	 */
	@Override
	public boolean doAuth(String login, Object password, 
			DBConnector database) throws SQLException {
		if (!(password instanceof String)) {
			throw new IllegalArgumentException("PasswordAuth needs the"
				 + " password as a String as authentication token");
		}
		
		String dbPwd = null;
		String pwdQuery = this.selectQuery.replace("1", DBConnector.dbName);
		pwdQuery = pwdQuery.replace("2", this.table);
		pwdQuery = pwdQuery.replace("3", this.login);
		pwdQuery = pwdQuery.replace("4", login);
		ResultSet result = database.executeQuery(pwdQuery);
		if (result.next()) {
			dbPwd = result.getString(this.password);
		} else {  //User does not exist
			return false;
		}
		
		String cryptPwd = crypt((String)password);	
		if (cryptPwd.equals(dbPwd)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Creates a hash and base64 encodes it.
	 * 
	 * @param password  the String to be hashed
	 * 
	 * @return  the hashed, base64 encoded password
	 */
	private String crypt(String password) {
		byte[] digest = this.hash.digest(password.getBytes());
		return base64enc.encode(digest);
	}
	
	/**
	 * Save a new user to the database table.
	 * 
	 * @param login  the user's login
	 * @param token  the raw password
	 * @param database  the database for storing the logins/passwords
	 * @throws SQLException 
	 */
	@Override
	public void newUser(String login, Object token, 
			DBConnector database) throws SQLException {
		if (! (token instanceof String)) {
			throw new IllegalArgumentException("Authentication token must" 
					+ " be of String-type");
		}
		String password = (String)token;
		//Check length the tables are VARCHAR(255)
		if (login.length() > 255 || password.length() > 255) {
			throw new IllegalArgumentException("Login or password too long" 
					+ " (must be less than 256 characters");
		}
		
		String command = this.insertCommand.replace("1", DBConnector.dbName);
		command = command.replace("2", this.table);
		command = command.replace("3", login);
		command = command.replace("4", crypt(password));
		
		database.executeCommand(command);
	}
	
	/**
	 * Delete a user from the password table.
	 * 
	 * @param login  the user's login
	 * @param database  the database storing the logins/passwords
	 * @throws SQLException 
	 */
	@Override
	public void deleteUser(String login, DBConnector database) 
		throws SQLException {
		String command = this.deleteCommand.replace("1", DBConnector.dbName);
		command = command.replace("2", this.table);
		command = command.replace("3", this.login);
		command = command.replace("4", login);
		database.executeCommand(command);
	}
	
}
