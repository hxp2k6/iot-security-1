package se.sics.assertionServer.databaseAccess;

import se.sics.assertionServer.AttributeDefinition;
import se.sics.saml.SAMLAttribute;
import se.sics.saml.SAMLID;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.util.Properties;

/**
 * This class provides SQL database connectivity for the Attribute Authority.
 * 
 * @author Ludwig Seitz
 *
 */
public class SQLConnector implements DBConnector {

	/**
	 * The default user of the database
	 */
	private String defaultUser = "saml-aa";
	
	/**
	 * The default password of the default user. 
	 * CAUTION! Only use this for testing, this is very insecure
	 * (but then if you didn't figure that out yourself, I cannot help you
	 * anyway).
	 */
	private String defaultPassword = "password";
	
	/**
	 * The default connection URL for the database. Here we use a 
	 * MySQL database on port 3306.
	 */
	private String defaultDbUrl = "jdbc:mysql://localhost:3306/";
	
	/**
	 * A prepared connection.
	 */
	private Connection conn = null;
	
	/**
	 * A prepared SELECT statement with the parameters subject, soa,
	 * attributeId, and dataType
	 */
	private PreparedStatement selectAttr;
	
	/**
	 * A prepared SELECT statement with the parameter subject
	 */
	private PreparedStatement selectAllAttrs;
	
	/**
	 * A prepared SELECT statement with the parameters soa, attributeId,
	 * and dataType
	 */
	private PreparedStatement selectAllSubjects;
	
	/**
	 * A prepared SELECT statement with the parameters soa, attributeId,
	 * and dataType
	 */
	private PreparedStatement selectAttrDef;
	
	/**
	 * A prepared INSERT statement to create a new attribute definition.
	 */
	private PreparedStatement insertNewAttrDef;
	
	/**
	 * A prepared DELETE statement to delete an attribute definition. 
	 */
	private PreparedStatement deleteAttrDef;
	
	/**
	 * A prepared DELETE statement to delete all attribute values for
	 * a specific attribute
	 */
	private PreparedStatement deleteAttrs;
	
	/**
	 * A prepared INSERT statement to create a new attribute value
	 */
	private PreparedStatement insertNewAttr;
	
	/**
	 * A prepared UPDATE statement to change an attribute value
	 */
	private PreparedStatement updateAttr;
	
	/**
	 * A prepared DELETE statement to delete a specific attribute value
	 */
	private PreparedStatement deleteAttr;
	
	
	/**
	 * Create a new database connector either from given values or the 
	 * defaults.
	 * 
	 * @param dbUrl  the database URL, if null the default will be used
	 * @param user   the database user, if null the default will be used
	 * @param pwd    the database user's password, if null the default 
	 * 				 will be used
	 * @throws SQLException 
	 */
	public SQLConnector(String dbUrl, String user, String pwd) 
			throws SQLException {
		if (dbUrl != null) {
			this.defaultDbUrl = dbUrl;
		}
		if (user != null) {
			this.defaultUser = user;
		}
		if (pwd != null) {
			this.defaultPassword = pwd;
		}
		Properties connectionProps = new Properties();		
		connectionProps.put("user", this.defaultUser);
		connectionProps.put("password", this.defaultPassword);
		this.conn = DriverManager.getConnection(this.defaultDbUrl, 
				connectionProps);
		this.selectAttr = this.conn.prepareStatement("SELECT *" 
				+ " FROM " + DBConnector.dbName + "." + DBConnector.attrTable 
				+ " WHERE "
				+ DBConnector.subject + "=? AND "
				+ DBConnector.soa + "=? AND "
				+ DBConnector.attrId + "=? AND "
				+ DBConnector.dataType + "=?;");
		
		this.selectAllAttrs= this.conn.prepareStatement("SELECT *"
				+ " FROM " + DBConnector.dbName + "." + DBConnector.attrTable 
				+ " WHERE " 
				+ DBConnector.subject + "=?;");		
		
		this.selectAllSubjects = this.conn.prepareStatement("SELECT * "
				+ " FROM " + DBConnector.dbName + "." + DBConnector.attrTable 
				+ " WHERE "
				+ DBConnector.soa + "=? AND "
				+ DBConnector.attrId + "=? AND "
				+ DBConnector.dataType + "=?;");
		
		this.selectAttrDef = this.conn.prepareStatement("SELECT * "
				+ " FROM " + DBConnector.dbName + "." 
				+ DBConnector.attrDefTable + " WHERE "
				+ DBConnector.soa + "=? AND "
				+ DBConnector.attrId + "=? AND "
				+ DBConnector.dataType + "=?;");
		
		this.insertNewAttrDef = this.conn.prepareStatement("INSERT INTO "
				+ DBConnector.dbName + "." + DBConnector.attrDefTable 
				+ " VALUES (?,?,?,?);");
		
		this.deleteAttrDef = this.conn.prepareStatement("DELETE FROM "
				+ DBConnector.dbName + "." + DBConnector.attrDefTable 
				+ " WHERE "
				+ DBConnector.soa + "=? AND "
				+ DBConnector.attrId + "=? AND "
				+ DBConnector.dataType + "=?;");
		
		this.deleteAttrs = this.conn.prepareStatement("DELETE FROM "
				+ DBConnector.dbName + "." + DBConnector.attrTable + " WHERE "
				+ DBConnector.soa + "=? AND "
				+ DBConnector.attrId + "=? AND "
				+ DBConnector.dataType + "=?;");
		
		this.insertNewAttr = this.conn.prepareStatement("INSERT INTO "
				+ DBConnector.dbName + "." + DBConnector.attrTable 
				+ " VALUES (?,?,?,?,?);");

		
		this.updateAttr = this.conn.prepareStatement("UPDATE "
				+ DBConnector.dbName + "." + DBConnector.attrTable 
				+ " SET " + DBConnector.attrValue + "=? WHERE "
				+ DBConnector.subject + "=? AND "
				+ DBConnector.soa + "=? AND "
				+ DBConnector.attrId + "=? AND "
				+ DBConnector.dataType + "=? AND "
				+ DBConnector.attrValue + "=?;");
		
		this.deleteAttr = this.conn.prepareStatement("DELETE FROM "
				+ DBConnector.dbName + "." + DBConnector.attrTable 
				+ " WHERE "	+ DBConnector.subject + "=? AND "
				+ DBConnector.soa + "=? AND "
				+ DBConnector.attrId + "=? AND "
				+ DBConnector.dataType + "=? AND "
				+ DBConnector.attrValue + "=?;");
	}
	
	/**
	 * Get the prepared Statement object.
	 * 
	 * @return  the prepared Statement
	 * @throws SQLException
	 */
	private Statement getStatement() throws SQLException {
		return this.conn.createStatement();
	}
	
	/**
	 * Create the necessary database and tables. Requires the
	 * root user password.
	 * 
	 * @param rootPwd  the root user password
	 * @throws SQLException 
	 */
	@Override
	public void init(String rootPwd) throws SQLException {
		Properties connectionProps = new Properties();
		connectionProps.put("user", "root");
		connectionProps.put("password", rootPwd);
		Connection rootConn = DriverManager.getConnection(
				this.defaultDbUrl, connectionProps);
		
		String createDB = "CREATE DATABASE " + DBConnector.dbName 
		+ " CHARACTER SET utf8 COLLATE utf8_bin;";
		
		String createAttrDef = "CREATE TABLE " + DBConnector.dbName + "."
		    + DBConnector.attrDefTable + "(" 
			+ DBConnector.soa + " varchar(255) NOT NULL, " + DBConnector.attrId 
			+ " varchar(255) NOT NULL," + DBConnector.dataType 
			+ " varchar(255) " + "NOT NULL, " + DBConnector.allowedValue 
			+ " varchar(255)" + " DEFAULT NULL);";
		
		String createAttrVal = "CREATE TABLE " + DBConnector.dbName + "." 
			+ DBConnector.attrTable + " ("
			+ DBConnector.subject + " varchar(255) NOT NULL COMMENT"
			+ "'The identifier of the subject whom the attribute describes', "
			+ DBConnector.soa + " varchar(255) NOT NULL"
			+ " COMMENT 'The Source Of Authority for the Attribute.', " 
			+ DBConnector.attrId + " varchar(255) NOT NULL"
			+ " COMMENT 'The attribute identifier', " + DBConnector.dataType 
			+ " varchar(255) NOT NULL, " + DBConnector.attrValue 
			+ " varchar(255) NOT NULL COMMENT 'The attribute value');";
		
		Statement stmt = rootConn.createStatement();
		stmt.execute(createDB);
		stmt.execute(createAttrDef);
		stmt.execute(createAttrVal);
		stmt.close();
		rootConn.close();		
	}
	
	
	/**
	 * Execute a arbitrary query.
	 * CAUTION! This can be a big security risk, be sure to check 
	 * the query first.
	 * 
	 * @param query  the SQL query
	 * @return  the ResultSet of the submitted query
	 * @throws SQLException
	 */
	@Override
	public synchronized ResultSet executeQuery(String query) 
			throws SQLException {
		Statement stmt = getStatement();
		return stmt.executeQuery(query);		
	}
	
	
	/**
	 * Execute a arbitrary SQL statement
	 * CAUTION! This is a big security risk, be sure to check 
	 * the query first.
	 * 
	 * @param statement  the SQL command
	 * @throws SQLException
	 */
	@Override
	public synchronized void executeCommand(String statement) 
			throws SQLException {
		Statement stmt = getStatement();
		stmt.execute(statement);
	}
	
	/**
	 * Do a retrieval of attribute values.
	 * This does either:
	 * <ul>
	 * 	<li>Retrieve a specific attribute for a subject, needs:
	 * 		subject, soa, attrId, dataType</li>
	 *  <li>Retrieve all attributes for a subject, needs: subject;
	 *  	rest is null</li>
	 *  <li>Retrieve all subjects and values for a specific attribute,
	 *  	needs: soa, attrId, dataType; subject is null.
	 * </ul>
	 * 
	 * @param subject  the subject of the attribute. If this is null,
	 * 					soa, attrId and dataType must not be null
	 * @param soa      the Source Of Authority for the Attribute.
	 * 					If this is null, subject must not be null
	 * @param attrId   the attribute identifier. If this is null, subject 
	 * 					must not be null	 				
	 * @param dataType the data type of the attribute. If this is null
	 * 					subject must not be null
	 * @return  the results of the SELECT query
	 * @throws SQLException
	 */
	@Override
	public synchronized ResultSet select(String subject, String soa, 
			String attrId, String dataType) 
			throws SQLException {
		ResultSet result = null;
		if (subject == null && soa != null && attrId != null 
				&& dataType != null) {
			this.selectAllSubjects.setString(1, soa);
			this.selectAllSubjects.setString(2, attrId);
			this.selectAllSubjects.setString(3, dataType);
			result = this.selectAllSubjects.executeQuery();
			this.selectAllSubjects.clearParameters();
		} else if (subject != null && soa == null && attrId == null 
				&& dataType == null) {
			this.selectAllAttrs.setString(1, subject);
			result = this.selectAllAttrs.executeQuery();
			this.selectAllAttrs.clearParameters();
		} else if (subject != null && soa != null && attrId != null 
				&& dataType != null) {
			this.selectAttr.setString(1, subject);
			this.selectAttr.setString(2, soa);
			this.selectAttr.setString(3, attrId);
			this.selectAttr.setString(4, dataType);
			result = this.selectAttr.executeQuery();
			this.selectAllAttrs.clearParameters();
		} else {
			throw new IllegalArgumentException("Illegal null values in method"
					+ "call");
		}
		return result;
	}
	
	/**
	 * Selects an attribute definition
	 * 
	 * @param soa      the Source Of Authority for the attribute
	 * @param attrId   the attribute identifier	
	 * @param dataType the data type of the attribute
	 * 
	 * @return  the results of the SELECT query
	 * @throws SQLException 
	 */
	@Override
	public synchronized ResultSet selectDefs(String soa,
			String attrId, String dataType) throws SQLException {
		this.selectAttrDef.setString(1, soa);
		this.selectAttrDef.setString(2, attrId);
		this.selectAttrDef.setString(3, dataType);
		ResultSet result = this.selectAttrDef.executeQuery();
		this.selectAttrDef.clearParameters();
		return result;
	}
	
	/**
	 * Creates a new attribute definition.
	 * 
	 * @param ad  the attribute definition
	 * @throws SQLException
	 */
	@Override
	public synchronized void insertAttrDef(AttributeDefinition ad) 
			throws SQLException {
		this.insertNewAttrDef.setString(1, ad.getSOA().getName());
		this.insertNewAttrDef.setString(2, ad.getAttributeId().toString());
		this.insertNewAttrDef.setString(3, ad.getDataType().toString());
		if (!ad.getAllowedValues().isEmpty()) {
			for (String value : ad.getAllowedValues()) {
				this.insertNewAttrDef.setString(4, value);
				this.insertNewAttrDef.execute();
			}
		} else {
			this.insertNewAttrDef.setNull(4, Types.VARCHAR);
			this.insertNewAttrDef.execute();
		}
		this.insertNewAttrDef.clearParameters();
	}
	

	/**
	 * Deletes an attribute definition and all associated attribute
	 * values. 
	 * 
	 * @param ad  the attribute definition
	 * @throws SQLException
	 */
	@Override
	public synchronized void deleteAttrDef(AttributeDefinition ad) 
			throws SQLException {
		this.deleteAttrDef.setString(1, ad.getSOA().getName());
		this.deleteAttrs.setString(1, ad.getSOA().getName());
		this.deleteAttrDef.setString(2, ad.getAttributeId().toString());
		this.deleteAttrs.setString(2, ad.getAttributeId().toString());		
		this.deleteAttrDef.setString(3, ad.getDataType().toString());
		this.deleteAttrs.setString(3, ad.getDataType().toString());
		this.deleteAttrs.execute();
		this.deleteAttrDef.execute();
		this.deleteAttrs.clearParameters();
		this.deleteAttrDef.clearParameters();
	}
	
	/**
	 * Inserts a new attribute value into the database.
	 * 
	 * @param subject  the identifier of the subject the attribute describes 
	 * @param soa      the source of authority for the attribute 
	 * @param attr     the attribute
	 * @throws SQLException 
	 */
	@Override
	public synchronized void insertAttr(SAMLID subject, SAMLID soa, 
			SAMLAttribute attr) throws SQLException {
		this.insertNewAttr.setString(1, subject.getName());
		this.insertNewAttr.setString(2, soa.getName());
		this.insertNewAttr.setString(3, attr.getName());
		String dataType = attr.getOtherXMLAttr("xacmlprof:DataType");
		if (dataType == null) {
			dataType = SAMLAttribute.xmlStringId;
		}
		this.insertNewAttr.setString(4, dataType);
		//We checked before that this is the one and only value
		this.insertNewAttr.setString(5, 
				attr.getAttributeValues().get(0).getValue());
		this.insertNewAttr.execute();
		this.insertNewAttr.clearParameters();
	}
	
	/**
	 * Updates an existing attribute in the database.
	 * 
	 * @param subject  the identifier of the subject the attribute describes 
	 * @param soa      the source of authority for the attribute 
	 * @param oldAttr  the old attribute
	 * @param newAttrValue  the new attribute value
	 * @throws SQLException 
	 */
	@Override
	public synchronized void updateAttr(SAMLID subject, SAMLID soa,
			SAMLAttribute oldAttr, String newAttrValue) throws SQLException {
		this.updateAttr.setString(1, newAttrValue);
		this.updateAttr.setString(2, subject.getName());
		this.updateAttr.setString(3, soa.getName());
		this.updateAttr.setString(4, oldAttr.getName());
		String dataType = oldAttr.getOtherXMLAttr("xacmlprof:DataType");
		if (dataType == null) {
			dataType = SAMLAttribute.xmlStringId;
		}
		this.updateAttr.setString(5, dataType);
		//We checked before that this is the one and only value
		this.updateAttr.setString(6, 
				oldAttr.getAttributeValues().get(0).getValue());
		this.updateAttr.execute();
		this.updateAttr.clearParameters();
	}
	
	/**
	 * Deletes an existing attribute value from the database.
	 * 
	 * @param subject  the identifier of the subject the attribute describes 
	 * @param soa      the source of authority for the attribute
	 * @param attr     the attribute
	 * @throws SQLException  
	 */
	@Override
	public synchronized void deleteAttr(SAMLID subject, SAMLID soa,
			SAMLAttribute attr) 
			throws SQLException {
		this.deleteAttr.setString(1, subject.getName());
		this.deleteAttr.setString(2, soa.getName());
		this.deleteAttr.setString(3, attr.getName());
		String dataType = attr.getOtherXMLAttr("xacmlprof:DataType");
		if (dataType == null) {
			dataType = SAMLAttribute.xmlStringId;
		}
		this.deleteAttr.setString(4, dataType);
		//We checked before that this is the one and only value
		this.deleteAttr.setString(5, 
				attr.getAttributeValues().get(0).getValue());
		this.deleteAttr.execute();
		this.deleteAttr.clearParameters();
	}
	
	/**
	 * Close the connections. After this any other method calls to this
	 * object will lead to an exception.
	 * 
	 * @throws SQLException
	 */
	@Override
	public void close() throws SQLException {
		this.conn.close();
	}
	
	/**
	 * Test code.
	 * @param args
	 */
	public static void main(String[] args) {
		// Just a test
		try {
			SQLConnector dbc = new SQLConnector(null, null, null);
			ResultSet res = dbc.select("ludwig@sics.se", 
					"saml-aa", "groupRole:SWIN-project", 
					SAMLAttribute.xmlStringId);
			 while (res.next()) {
				 String attrV = res.getString(DBConnector.attrValue);
				 System.out.println(attrV);
			 }
			 dbc.close();
		} catch (SQLException e) {
			// This is just a test, so this will do:
			e.printStackTrace();
		}

	}	 
}