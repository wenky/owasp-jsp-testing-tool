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
package org.owasp.jsptester.report;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.ecs.xml.XML;
import org.owasp.jsptester.attack.Attack;
import org.owasp.jsptester.attack.AttackLibrary;
import org.owasp.jsptester.parser.TagFileParser;

/**
 * @author Jason Li
 * 
 */
public class TestCase
{

    public static String generateTestCase( TagLibraryInfo tagLibrary,
            TagInfo tag, TagAttributeInfo attr, Attack attack )
    {
        XML customTag = new XML( tagLibrary.getShortName() + ":"
                + tag.getTagName() );
        customTag.addAttribute( attr.getName(), attack.getAttackString() );
        return customTag.toString();
    }

    public static String generateTagTextTestCase( TagLibraryInfo tagLibrary,
            TagInfo tag, Attack attack )
    {
        XML customTag = new XML( tagLibrary.getShortName() + ":"
                + tag.getTagName() );
        customTag.setTagText( attack.getAttackString() );
        return customTag.toString();
    }

    private static Set getRequiredAttributes( TagInfo tag )
    {
        TagAttributeInfo[] attrs = tag.getAttributes();

        Set requiredAttrs = new HashSet();

        for ( int attrIdx = 0; attrIdx < attrs.length; attrIdx++ )
        {
            if ( attrs[attrIdx].isRequired() )
            {
                requiredAttrs.add( attrs[attrIdx].getName() );
            }
        }

        return requiredAttrs;
    }

    public static void main( String[] args ) throws Exception
    {
        String libraryFile = "resources/html_basic.tld";

        TagLibraryInfo tagLibrary = TagFileParser.loadTagFile( new File(
                libraryFile ) );

        Attack[] attacks = AttackLibrary.getInstance().getAttacks();

        TagInfo[] tags = tagLibrary.getTags();
        for ( int tagIdx = 0; tagIdx < tags.length; tagIdx++ )
        {
            TagInfo tag = tags[tagIdx];
            TagAttributeInfo[] attrs = tag.getAttributes();
            for ( int attrIdx = 0; attrIdx < attrs.length; attrIdx++ )
            {

                TagAttributeInfo attr = attrs[attrIdx];

                for ( int attackIdx = 0; attackIdx < attacks.length; attackIdx++ )
                {
                    Attack attack = attacks[attackIdx];

                    System.out.println( generateTagTextTestCase( tagLibrary, tag,
                            attack ) );
                    System.out.println( generateTestCase( tagLibrary, tag,
                            attr, attack ) );
                }
            }

        }

    }
}
