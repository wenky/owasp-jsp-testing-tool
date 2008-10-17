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
 * @author Jason Li
 * 
 */
public class TagLibraryUtils
{

    public static boolean hasRequiredAttributes( TagInfo tag )
    {
        TagAttributeInfo[] attrs = tag.getAttributes();
        for ( int attrIdx = 0; attrIdx < attrs.length; attrIdx++ )
        {
            if ( attrs[attrIdx].isRequired() )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a Set<TagAttributeInfo> of required attributes for the given tag
     * 
     * @param tag
     *            the tag to obtain the required attributes for
     * @return a Set<TagAttributeInfo> of required attributes for the given tag
     */
    public static Set/* <TagAttributeInfo> */getRequiredAttributes( TagInfo tag )
    {
        Set/* <TagAttributeInfo> */reqAttrs = new HashSet/* <TagAttributeInfo> */();
        TagAttributeInfo[] attrs = tag.getAttributes();
        for ( int attrIdx = 0; attrIdx < attrs.length; attrIdx++ )
        {
            if ( attrs[attrIdx].isRequired() )
            {
                reqAttrs.add( attrs[attrIdx] );
            }
        }
        return reqAttrs;
    }

    public static Map/* <TagAttributeInfo, String> */getRequiredAttributesMap(
            TagInfo tag, TagProperties tagProperties )
    {
        Map/* <TagAttributeInfo, String> */attrMap = new HashMap();

        Set/* <TagAttributeInfo> */reqAttrs = getRequiredAttributes( tag );

        for ( Iterator/* <TagAttributeInfo> */itr = reqAttrs.iterator(); itr
                .hasNext(); )
        {
            TagAttributeInfo reqAttr = (TagAttributeInfo) itr.next();
            if ( tagProperties.hasTagProperty( tag.getTagName(), reqAttr
                    .getName() ) )
            {
                attrMap.put( reqAttr, tagProperties.getTagProperty( tag
                        .getTagName(), reqAttr.getName() ) );
            }
        }

        return attrMap;
    }
}
