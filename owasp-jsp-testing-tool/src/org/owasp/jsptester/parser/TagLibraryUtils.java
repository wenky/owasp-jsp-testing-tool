/*
 * Copyright (c) 2008, Jason Li
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer; (2) Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution; (3) Neither the name of OWASP nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.owasp.jsptester.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;

import org.owasp.jsptester.conf.TagProperties;

/**
 * Utility class that provides convenience methods for tags and tag attributes
 * 
 * @author Jason Li
 * 
 */
public class TagLibraryUtils
{

    /**
     * Returns true if the given tag has required attributes; false otherwise
     * 
     * @param tag
     *            the tag to check
     * @return true if the given tag has required attributes; false otherwise
     */
    public static boolean hasRequiredAttributes( TagInfo tag )
    {
        TagAttributeInfo[] attrs = tag.getAttributes();

        // loop through the tags attributes
        for ( int attrIdx = 0; attrIdx < attrs.length; attrIdx++ )
        {
            // if the attribute is required, return true
            if ( attrs[attrIdx].isRequired() )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a <code>Set&lt;TagAttributeInfo&gt;</code> of required
     * attributes for the given tag
     * 
     * @param tag
     *            the tag to obtain the required attributes for
     * @return a <code>Set&lt;TagAttributeInfo&gt;</code> of required
     *         attributes for the given tag
     */
    public static Set/* <TagAttributeInfo> */getRequiredAttributes( TagInfo tag )
    {
        Set/* <TagAttributeInfo> */reqAttrs = new HashSet/* <TagAttributeInfo> */();

        TagAttributeInfo[] attrs = tag.getAttributes();

        // loop through the tags attributes
        for ( int attrIdx = 0; attrIdx < attrs.length; attrIdx++ )
        {
            // if the attribute is required, add it to the set
            if ( attrs[attrIdx].isRequired() )
            {
                reqAttrs.add( attrs[attrIdx] );
            }
        }
        return reqAttrs;
    }

    /**
     * Returns a <code>Map&lt;TagAttributeInfo, String&gt;</code> of required
     * attributes to the values in the given tag properties (if configured)
     * 
     * @param tag
     *            the tag to retrieve the map for
     * @param tagProperties
     *            the tag properties to use
     * @return a <code>Map&lt;TagAttributeInfo, String&gt;</code> of required
     *         attributes to the values
     */
    public static Map/* <TagAttributeInfo, String> */getRequiredAttributesMap(
            TagInfo tag, TagProperties tagProperties )
    {
        Map/* <TagAttributeInfo, String> */attrMap = new HashMap();

        Set/* <TagAttributeInfo> */reqAttrs = getRequiredAttributes( tag );

        // loop over required attributes
        for ( Iterator/* <TagAttributeInfo> */itr = reqAttrs.iterator(); itr
                .hasNext(); )
        {
            TagAttributeInfo reqAttr = (TagAttributeInfo) itr.next();

            // if attribute is configured, put in value in map
            if ( tagProperties.hasTagProperty( tag.getTagName(), reqAttr
                    .getName() ) )
            {
                attrMap.put( reqAttr, tagProperties.getTagProperty( tag
                        .getTagName(), reqAttr.getName() ) );
            }
        }

        return attrMap;
    }
    
    /**
     * Constructs an instance of <code>TagLibraryUtils</code>
     */
    private TagLibraryUtils(){}
}
