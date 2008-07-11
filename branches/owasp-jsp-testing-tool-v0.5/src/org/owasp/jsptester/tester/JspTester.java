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
package org.owasp.jsptester.tester;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.owasp.jsptester.attack.Attack;
import org.owasp.jsptester.attack.AttackLibrary;
import org.owasp.jsptester.parser.TagFileParser;
import org.owasp.jsptester.report.ReportGenerator;

/**
 * @author Jason Li
 * 
 */
public class JspTester
{

    private static final Logger logger = Logger.getLogger( "JspTester" );

    private TagLibraryInfo tagLibrary;
    private Attack[] attacks;
    private ReportGenerator reportGenerator;
    
    public JspTester(String libraryFileLocation) throws Exception
    {
        File libraryFile = new File( libraryFileLocation );
        if ( !libraryFile.exists() || libraryFile.exists()
                && !libraryFile.isFile() )
        {
            throw new IllegalArgumentException(
                    "The specified TLD file does not exists" );
        }
        
        tagLibrary = TagFileParser.loadTagFile( libraryFile );

        attacks = AttackLibrary.getInstance().getAttacks();
        
        reportGenerator = ReportGenerator.getInstance();
    }
    
    public void testLibrary( String outputDirLocation ) throws Exception
    {
        logger.entering( JspTester.class.getName(), "testLibrary" );

        File outputDir = new File( outputDirLocation );
        if ( outputDir.exists() && !outputDir.isDirectory() )
        {
            throw new IllegalArgumentException(
                    "The specified output directory is a file." );
        }
        else if ( !outputDir.exists() )
        {
            if ( !outputDir.mkdirs() )
            {
                throw new IOException( "Unable to create directories" );
            }
        }

        reportGenerator.copyBase( outputDir );
        
        File reportFile = new File( outputDir, "report.html" );
        FileWriter writer = new FileWriter( reportFile );

        reportGenerator.generateReport( tagLibrary, writer );

        logger.fine( "Main report file generated" );

        TagInfo[] tags = tagLibrary.getTags();

        for ( int tagIdx = 0; tagIdx < tags.length; tagIdx++ )
        {
            TagInfo tag = tags[tagIdx];

            // do tag text attack
            for ( int attackIdx = 0; attackIdx < attacks.length; attackIdx++ )
            {
                Attack attack = attacks[attackIdx];
                FileWriter compFileWriter = new FileWriter( new File(
                        outputDir, tag.getTagName() + "-" + attack.getName()
                                + ".jsp" ) );

                reportGenerator.generateComponentTest(
                        tagLibrary, tag, attack, compFileWriter );
                compFileWriter.close();

                logger.fine( tag.getTagName() + "-" + attack.getName()
                        + " file generated" );
            }

            TagAttributeInfo[] attrs = tag.getAttributes();
            for ( int attrIdx = 0; attrIdx < attrs.length; attrIdx++ )
            {
                TagAttributeInfo attr = attrs[attrIdx];
                for ( int attackIdx = 0; attackIdx < attacks.length; attackIdx++ )
                {
                    Attack attack = attacks[attackIdx];
                    FileWriter compFileWriter = new FileWriter( new File(
                            outputDir, tag.getTagName() + "-" + attr.getName()
                                    + "-" + attack.getName() + ".jsp" ) );

                    reportGenerator.generateAtrributeTest(
                            tagLibrary, tags[tagIdx], attrs[attrIdx],
                            attacks[attackIdx], compFileWriter );

                    compFileWriter.close();

                    logger.fine( tag.getTagName() + "-" + attr.getName() + "-"
                            + attack.getName() + " file generated" );
                }
            }
        }

        writer.close();

        logger.exiting( JspTester.class.getName(), "testLibrary" );
    }

    /**
     * @param args
     */
    public static void main( String[] args ) throws Exception
    {
        String tldFile = "resources/subset.tld";
        String outputDir = "WebContent/";

        JspTester tester = new JspTester( tldFile );
        tester.testLibrary( outputDir );
    }

}
