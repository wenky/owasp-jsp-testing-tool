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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.ecs.xml.XML;
import org.owasp.jsptester.attack.Attack;
import org.owasp.jsptester.attack.AttackLibrary;
import org.owasp.jsptester.parser.TagFileParser;

/**
 * Utility class to create valid JSP tag syntax from tag library information.
 * 
 * @author Jason Li
 */
public class TestCase
{

    private static final Logger LOGGER = Logger.getLogger( TestCase.class
            .getName() );

    /**
     * Create a proper JSP tag with the given attribute set to the given attack
     * 
     * @param tagLibrary
     *            the tagLibrary being tested
     * @param tag
     *            the tag being tested
     * @param attr
     *            the attribute being tested
     * @param attack
     *            the attack being used
     * @return a String representing the valid JSP tag syntax for this test case
     */
    public static String generateAttrTestCaseJspTag( TagLibraryInfo tagLibrary,
            TagInfo tag, TagAttributeInfo attr, Attack attack,
            Map/* <TagAttributeInfo, String> */reqAttrs )
    {
        LOGGER.entering( TestCase.class.getName(),
                "generateAttrTestCaseJspTag", new Object[]
                    { tagLibrary, tag, attr, attack, reqAttrs } );

        // create the tag
        XML customTag = new XML( tagLibrary.getShortName() + ":"
                + tag.getTagName() );

        // add the attack attribute to the tag
        customTag.addAttribute( attr.getName(), attack.getAttackString() );

        // for any required attributes, add the attributes to the tag
        for ( Iterator/* <TagAttributeInfo> */itr = reqAttrs.keySet()
                .iterator(); itr.hasNext(); )
        {
            TagAttributeInfo reqAttr = (TagAttributeInfo) itr.next();
            customTag.addAttribute( reqAttr.getName(), reqAttrs.get( reqAttr )
                    .toString() );
        }

        String toReturn = customTag.toString();

        LOGGER.exiting( TestCase.class.getName(), "generateAttrTestCaseJspTag",
                toReturn );

        return toReturn;
    }

    /**
     * Create a proper JSP tag with the given attack embedded in the tag
     * 
     * @param tagLibrary
     *            the tagLibrary being tested
     * @param tag
     *            the tag being tested
     * @param attack
     *            the attack being used
     * @return a String representing the valid JSP tag syntax for this test case
     */
    public static String generateTagTestCaseJspTag( TagLibraryInfo tagLibrary,
            TagInfo tag, Attack attack,
            Map/* <TagAttributeInfo, String> */reqAttrs )
    {
        LOGGER.entering( TestCase.class.getName(), "generateTagTestCaseJspTag",
                new Object[]
                    { tagLibrary, tag, attack } );

        // create the tag
        XML customTag = new XML( tagLibrary.getShortName() + ":"
                + tag.getTagName() );

        // for any required attributes, add the attributes to the tag
        for ( Iterator/* <TagAttributeInfo> */itr = reqAttrs.keySet()
                .iterator(); itr.hasNext(); )
        {
            TagAttributeInfo reqAttr = (TagAttributeInfo) itr.next();
            customTag.addAttribute( reqAttr.getName(), reqAttrs.get( reqAttr )
                    .toString() );
        }

        // set the embedded text of the tag to the attack
        customTag.setTagText( attack.getAttackString() );

        String toReturn = customTag.toString();
        LOGGER.exiting( TestCase.class.getName(), "generateTagTestCaseJspTag",
                toReturn );

        return toReturn;
    }

    /**
     * Test code
     * 
     * @deprecated
     */
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

                    System.out.println( generateTagTestCaseJspTag( tagLibrary,
                            tag, attack, Collections.EMPTY_MAP ) );
                    System.out.println( generateAttrTestCaseJspTag( tagLibrary,
                            tag, attr, attack, Collections.EMPTY_MAP ) );
                }
            }

        }

    }
}
