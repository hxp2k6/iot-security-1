
/*
 * @(#)XMLInputParser.java
 * 
 * Copyright (c) 2005, Swedish Institute of Computer Science.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 *    * Neither the name of the Swedish Institute of Computer Science
 *      nor the names of its contributors may be used to endorse or
 *      promote products derived from this software without specific
 *      prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package se.sics.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

/**
 * A convenience class for XML parsing. The main reason for the
 * existence of this class is that it saves the lines of code needed
 * to configure the <code>DocumentBuilder</code>. The documentbuilder
 * is configured to cache parsed schemas, so performance is improved
 * for successive parsings with the same instance.
 *
 * @author Erik Rissanen
 * @version 0.1
 * @since 0.1
 */
public class XMLInputParser {

	/**
	 * A document builder for this parser
	 */
    private DocumentBuilder db = null;
    
    /**
     * It seems like the DocumentBuilder calls static methods in the
     * SAX parser, so we need to make sure only one instance of this class
     * is parsing at a time. This lock is used for that.
     */
    static private Lock lock = new ReentrantLock();
    
    /**
     * Class constructor.
     *
     * @param schemas schema files to use for validation or null if no
     * validation is to be done. This parameter is passed to the parser as the
     * schemaSource property.
     * @param entityMap a map which is used for resolving entities to
     * local files or null if no custom entity resolving is to be done.
     * The keys of the map are Strings with the systemIds or publicIds of
     * the entities and the values are names of the local files as Strings.
     * 
     * @throws ParserConfigurationException 
     */
    public XMLInputParser(InputStream[] schemas, Map<String,String> entityMap)
        throws ParserConfigurationException {

        //boolean validating = schemas != null;
        boolean validating = false;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setIgnoringComments(true);
        factory.setNamespaceAware(true);            
        factory.setValidating(validating);

        if(schemas != null) {
            
            String JAXP_SCHEMA_LANGUAGE =
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
            String W3C_XML_SCHEMA =
                "http://www.w3.org/2001/XMLSchema";
            factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA); 
            String JAXP_SCHEMA_SOURCE =
                "http://java.sun.com/xml/jaxp/properties/schemaSource";
            factory.setAttribute(JAXP_SCHEMA_SOURCE, schemas);
        }

        this.db = factory.newDocumentBuilder();

        if(entityMap != null)
            this.db.setEntityResolver(new MyEntityResolver(entityMap));
        
        ErrorHandler ehandler = new ErrorHandler() {
            @Override
			public void warning(SAXParseException e) throws SAXException {
                // Ignore warning
            }
            @Override
			public void error(SAXParseException e) throws SAXException {
                throw e;
            }
            @Override
			public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        };      
        this.db.setErrorHandler(ehandler);
    }


    /**
     * Parses an xml document from an <code>InputSource</code>.
     *
     * @param source the document to parse
     * @return the parsed document as a DOM
     * 
     * @throws SAXException 
     * @throws java.io.IOException 
     */
    public Document parseDocument(org.xml.sax.InputSource source)
        throws SAXException, java.io.IOException
    {
        lock.lock();
        Document doc = null;
        try {
            doc = this.db.parse(source);
        } 
        finally {
            lock.unlock();
        }
        return doc;
    }


    /**
     * Parses an xml document from a <code>String</code>.
     *
     * @param xmlText the text of the document to parse
     * @return the parsed document as a DOM
     * 
     * @throws SAXException 
     */
    public Document parseDocument(String xmlText)
        throws SAXException
    {
        try {
            return parseDocument(new org.xml.sax.InputSource
                    (new java.io.StringReader(xmlText)));
        }
        catch(IOException e) {
            // This cannot happen since we have the xml in memory
            throw new RuntimeException("Impossible error");
        }
    }


    /**
     * Parses an xml document from a byte array
     *
     * @param xmlText the text of the document to parse
     * @return the parsed document as a DOM
     * 
     * @throws SAXException 
     */
    public Document parseDocument(byte[] xmlText)
        throws SAXException
    {
        try {
            return parseDocument(new org.xml.sax.InputSource
                    (new ByteArrayInputStream(xmlText)));      
        }
        catch(IOException e) {
            // This cannot happen since we have the xml in memory
            throw new RuntimeException("Impossible error");
        }
    }
    

    /**
     * Parses an xml document from a <code>File</code>.
     *
     * @param xmlFile the file with the document to parse
     * @return the parsed document as a DOM
     * 
     * @throws SAXException 
     * @throws java.io.IOException 
     */
    public Document parseDocument(java.io.File xmlFile)
        throws SAXException, java.io.IOException
    {
        return parseDocument(new org.xml.sax.InputSource
                             (new java.io.FileInputStream(xmlFile)));
    }


    /**
     * Creates a new empty document. The document is created with the
     * contained <code>DocumentBuilder</code>.
     *
     * @return an empty XML document
     */
    public Document newDocument() {
        return this.db.newDocument();
    }
    
    
    /**
     * The internal entity resolver class.
     *
     */
    private static class MyEntityResolver implements EntityResolver2 {
    	
    	/**
    	 * Entity mappings
    	 */
        private Map<String,String> entityMap = null;
        
        /**
         * Constructor
         * 
         * @param entityMap  the entity mappings
         */
        public MyEntityResolver(Map<String,String> entityMap) {
            this.entityMap = entityMap;
        }

        @Override
		public InputSource getExternalSubset(String name, String baseURI)
        throws SAXException, IOException {
            return null;
        }

        @Override
		public InputSource resolveEntity(String name, String publicId,
                String baseURI, String systemId)
        throws SAXException, IOException {
            return null;
        }

        @Override
		public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException, IOException {
            // First try with the systemId
            String filename = this.entityMap.get(systemId);
            if(filename != null)
                return new InputSource(getClass().getResourceAsStream(filename));
            
            // If that did not work, try the publicId
            if(publicId == null)
                return null;
            filename = this.entityMap.get(publicId);
            if(filename != null)
                return new InputSource(getClass().getResourceAsStream(filename));
            
            return null;
        }
    }
}
