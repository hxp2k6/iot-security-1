
/*
 * @(#)AssertionServerSchemaConfiguration.java
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

package se.sics.assertionServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Some utility methods to configure the XML parser with the 
 * assertion server schemas. 
 *
 * @author Ludwig Seitz
 * @author Erik Rissanen
 */
public class AssertionServerSchemaConfiguration {
  
	/**
	 * Exception thrown when configuration is wrong.
	 */
    static public class ConfigurationException extends Exception {


        /**
		 * Generated serial version UID
		 */
		private static final long serialVersionUID = 2180253187526721697L;

		/**
         * Constructor with a message
         * @param msg  the message
         */
        public ConfigurationException(String msg) {
            super(msg);
        }
    }

    /**
     * Properties for configuration
     */
    Properties properties = null;
    
    /**
     * Constructor, loads the properties from a default location.
     * 
     * @throws IOException
     */
    public AssertionServerSchemaConfiguration() throws IOException {
        this.properties = new Properties();
        this.properties.load(getClass()
                       .getResourceAsStream("/schema/schemas.properties"));
    }
    
    /**
     * Get the schemas for the assertion server.
     * 
     * @return  an input stream with the schemas
     * @throws ConfigurationException
     */
    public InputStream[] getAsserionServerSchemas() 
            throws ConfigurationException {
        String sch_saml =
            this.properties.getProperty("se.sics.schema.saml_assertion");
        if(sch_saml == null) {
            throw new ConfigurationException("Missing property "+
            "se.sics.schema.saml_assertion");
        }
        
        InputStream[] schemas = {getClass().getResourceAsStream(sch_saml)};
        return schemas;
    }
    
    /**
     * @return  the entity mappings
     * @throws ConfigurationException
     */
    public Map<String, String> getEntityMap() throws ConfigurationException {
        Map<String, String> res = new HashMap<String, String>();
        
        String filename =
            this.properties.getProperty("se.sics.entity.xmldsig_core");
        if(filename == null) {
            throw new ConfigurationException("Missing property "+
            "se.sics.entity.xmldsig_core");
        }
        res.put("http://www.w3.org/TR/xmldsig-core/xmldsig-core-schema.xsd",
                filename);
                
        filename = this.properties.getProperty("se.sics.entity.xenc");
        if(filename == null) {
            throw new ConfigurationException("Missing property "+
            "se.sics.entity.xenc");
        }
        res.put("http://www.w3.org/TR/2002/"+
                "REC-xmlenc-core-20021210/xenc-schema.xsd", filename);

        filename = this.properties.getProperty("se.sics.entity.xmlschema");
        if(filename == null) {
            throw new ConfigurationException("Missing property "+
            "se.sics.entity.xmlschema");
        }
        res.put("http://www.w3.org/2001/XMLSchema.dtd", filename);
        
        filename = this.properties.getProperty("se.sics.entity.datatypes");
        if(filename == null) {
            throw new ConfigurationException("Missing property "+
            "se.sics.entity.datatypes");
        }
        res.put("datatypes", filename);
        
        return res;
    }
}
