
/*
 * @(#)DateUtils.java
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

package se.sics.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

/**
 * Utilities for parsing and encoding an UTC date.
 * Based on XACML DateTimeAttribute class.
 * 
 * @author Ludwig Seitz
 *
 */
public class DateUtils {

    /**
     * Parse a <code>Date</code> from a UTC <code>String</code>.
     * 
     * @param instant  The UTC string.
     * 
     * @return  a <code>Date</code> object.
     * @throws IllegalArgumentException 
     */
    public static Date parseInstant(String instant)
            throws IllegalArgumentException {
        SimpleDateFormat utcFormat = new SimpleDateFormat
        ("yyyy-MM-dd'T'HH:mm:ss'Z'");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return utcFormat.parse(instant);
        }
        catch(ParseException e) {
            throw new IllegalArgumentException
            ("Invalid Instant in TSA timestamp");
        }
    }
    
    /**
     * Encodes a <code>Date</code> object to a UTC string.
     * 
     * @param date  The date.
     * 
     * @return  a UTC string.
     */
    public static String toString(Date date) {
        // This parser has a four digit offset to GMT with sign
        DateFormat utcParser 
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        utcParser.setTimeZone(TimeZone.getTimeZone("UTC"));
        return utcParser.format(date);
    }
}
