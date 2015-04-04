package se.sics.assertionServer.databaseAccess;

import java.sql.ResultSet;
import java.sql.SQLException;

import se.sics.assertionServer.AttributeDefinition;
import se.sics.saml.SAMLAttribute;
import se.sics.saml.SAMLID;

/**
 * This interface provides database connectivity methods for the 
 * Attribute Authority.
 * 
 * @author Ludwig Seitz
 *
 */
public interface DBConnector {
	/**
	 * The default database name
	 */
	public String dbName = "saml_aa";
	
	/**
	 * The default attribute values table name
	 */	
	public String attrTable = "AttributeValue";
	
	/**
	 * The default attribute definitions table name
	 */
	public String attrDefTable = "AttributeDefinitions";
	
	/**
	 * The default column name for the source of authority
	 */
	public String soa = "SOA";
	
	/**
	 * The default column name for the subject of an attribute
	 */	
	public String subject = "Subject";
	
	/**
	 * The default column name for the identifier of an attribute
	 */	
	public String attrId = "AttrId";
		
	/**
	 * The default column name for the data type of an attribute
	 */	
	public String dataType = "DataType";
	
	/**
	 * The default column name for the value of an attribute
	 */	
	public String attrValue = "AttributeValue";
	
	/**
	 * The default column name for an allowed value of an attribute
	 */	
	public String allowedValue = "AllowedValue";
	
	/**
	 * Create the necessary database and tables. Requires the
	 * root user password.
	 * 
	 * @param rootPwd  the root user password
	 * @throws SQLException 
	 */
	public void init(String rootPwd) throws SQLException;
	
	/**
	 * Execute a arbitrary query.
	 * CAUTION! This can be a big security risk, be sure to check 
	 * the query first.
	 * 
	 * @param query  the SQL query
	 * @return  the ResultSet of the submitted query
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String query) throws SQLException;	
	
	/**
	 * Execute a arbitrary database command
	 * CAUTION! This is a big security risk, be sure to check 
	 * the query first.
	 * 
	 * @param statement  the database command
	 * @throws SQLException
	 */
	public void executeCommand(String statement) throws SQLException;
	
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
	public ResultSet select(String subject, String soa, 
			String attrId, String dataType) throws SQLException;
	
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
	public ResultSet selectDefs(String soa,
			String attrId, String dataType) throws SQLException;
	
	/**
	 * Creates a new attribute definition.
	 * 
	 * @param ad  the attribute definition
	 * @throws SQLException
	 */
	public void insertAttrDef(AttributeDefinition ad) 
			throws SQLException;
	/**
	 * Deletes an attribute definition and all associated attribute
	 * values. 
	 * 
	 * @param ad  the attribute definition
	 * @throws SQLException
	 */
	public void deleteAttrDef(AttributeDefinition ad) 
			throws SQLException;
	
	/**
	 * Inserts a new attribute value into the database.
	 * 
	 * @param subject  the identifier of the subject the attribute describes 
	 * @param soa      the source of authority for the attribute 
	 * @param attr     the attribute
	 * @throws SQLException 
	 */
	public void insertAttr(SAMLID subject, SAMLID soa, 
			SAMLAttribute attr) throws SQLException;
	
	/**
	 * Updates an existing attribute in the database.
	 * 
	 * @param subject  the identifier of the subject the attribute describes 
	 * @param soa      the source of authority for the attribute 
	 * @param oldAttr  the old attribute
	 * @param newAttrValue  the new attribute value
	 * @throws SQLException 
	 */
	public void updateAttr(SAMLID subject, SAMLID soa,
			SAMLAttribute oldAttr, String newAttrValue) throws SQLException;

	/**
	 * Deletes an existing attribute value from the database.
	 * 
	 * @param subject  the identifier of the subject the attribute describes 
	 * @param soa      the source of authority for the attribute
	 * @param attr     the attribute
	 * @throws SQLException  
	 */
	public void deleteAttr(SAMLID subject, SAMLID soa,            
			SAMLAttribute attr) throws SQLException;

	/**
	 * Close the connections. After this any other method calls to this
	 * object will lead to an exception.
	 * 
	 * @throws SQLException
	 */
	public void close() throws SQLException;

}
