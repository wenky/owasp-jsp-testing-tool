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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.JdkLogChute;
import org.owasp.jsptester.attack.Attack;
import org.owasp.jsptester.attack.AttackLibrary;
import org.owasp.jsptester.conf.Configuration;

public class ReportGenerator
{

    private static final Logger LOGGER = Logger
            .getLogger( ReportGenerator.class.getName() );

    private static ReportGenerator INSTANCE;

    private final VelocityEngine engine = new VelocityEngine();

    private ReportGenerator() throws VelocityException
    {
        engine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                JdkLogChute.class.getName() );

        try
        {
            engine.init();
        }
        catch ( Exception e )
        {
            throw new VelocityException( e );
        }
    }

    public synchronized static final ReportGenerator getInstance()
            throws VelocityException
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new ReportGenerator();
        }

        return INSTANCE;
    }

    /**
     * Copies the base template for the report to the given directory
     * 
     * @param outputDir
     *            the output directory to copy template files to
     * @throws IOException
     *             if an I/O error occurs during copying
     */
    private void copyBase( File outputDir ) throws IOException
    {
        LOGGER.entering( ReportGenerator.class.getName(), "copyBase(File)" );

        FileUtils.copyFileToDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.TEMPLATE_INDEX_JSP ) ), outputDir );

        FileUtils.copyFileToDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.TEMPLATE_ERROR_JSP ) ), outputDir );

        FileUtils.copyDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.TEMPLATE_META_INF ) ), new File(
                outputDir, "META-INF" ) );

        FileUtils.copyDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.TEMPLATE_WEB_INF ) ), new File( outputDir,
                "WEB-INF" ) );

        LOGGER.exiting( ReportGenerator.class.getName(), "copyBase(File)" );
    }

    /**
     * Generates a report on the given tag library using the given attacks and
     * places the output in the given outputDir.
     * 
     * @param tagLibrary
     *            the tag library to test
     * @param attacks
     *            the set of attacks to use
     * @param outputDir
     *            the output directory to place the report in
     * @throws IOException
     *             if any I/O error occurs
     */
    public List/*<File>*/ generateTagReport( TagLibraryInfo tagLibrary, Attack[] attacks,
            File outputDir ) throws IOException
    {

        List/*<File>*/ generatedTestCases = new ArrayList/*<File>*/();

        // Copy the base files over
        copyBase( outputDir );

        LOGGER.fine( "Base files copied" );

        File reportFile = new File( outputDir, "report.html" );
        FileWriter writer = null;

        try
        {
            writer = new FileWriter( reportFile );

            // generate initial report
            generateReport( tagLibrary, writer );
        }
        finally
        {
            IOUtils.closeQuietly( writer );
            writer = null;
        }
        LOGGER.fine( "Main report file generated" );

        TagInfo[] tags = tagLibrary.getTags();

        // generate test case for each tag
        for ( int tagIdx = 0; tagIdx < tags.length; tagIdx++ )
        {
            TagInfo tag = tags[tagIdx];

            // For each tag, test each attack embedded inside the component
            for ( int attackIdx = 0; attackIdx < attacks.length; attackIdx++ )
            {
                Attack attack = attacks[attackIdx];

                FileWriter compFileWriter = null;

                try
                {
                    // Create a test case file [tagName-attackName.jsp]
                    File compFile = new File( outputDir, tag.getTagName() + "-"
                            + attack.getName() + ".jsp" );
                    compFileWriter = new FileWriter( compFile );

                    generateComponentTest( tagLibrary, tag, attack,
                            compFileWriter );
                    
                    generatedTestCases.add(compFile);
                }
                finally
                {
                    IOUtils.closeQuietly( compFileWriter );
                    compFileWriter = null;
                }

                LOGGER.fine( tag.getTagName() + "-" + attack.getName()
                        + " file generated" );
            }

            // For each attribute, test each attack in the attribute
            TagAttributeInfo[] attrs = tag.getAttributes();
            for ( int attrIdx = 0; attrIdx < attrs.length; attrIdx++ )
            {
                TagAttributeInfo attr = attrs[attrIdx];
                for ( int attackIdx = 0; attackIdx < attacks.length; attackIdx++ )
                {
                    Attack attack = attacks[attackIdx];

                    FileWriter compFileWriter = null;
                    try
                    {
                        // Create a test case file
                        // [tagName-attackName-attrName.jsp]
                        File compFile = new File( outputDir, tag.getTagName()
                                + "-" + attr.getName() + "-" + attack.getName()
                                + ".jsp" );
                        compFileWriter = new FileWriter( compFile );

                        generateAtrributeTest( tagLibrary, tags[tagIdx],
                                attrs[attrIdx], attacks[attackIdx],
                                compFileWriter );

                        generatedTestCases.add( compFile );
                    }
                    finally
                    {
                        IOUtils.closeQuietly( compFileWriter );
                    }
                    LOGGER.fine( tag.getTagName() + "-" + attr.getName() + "-"
                            + attack.getName() + " file generated" );
                }
            }
        }

        return generatedTestCases;
    }

    /**
     * Writes the main report file from a Velocity report template
     * 
     * @param tagLibrary
     *            the tab library being tested
     * @param output
     *            the Writer to use for output
     * @throws VelocityException
     *             if an error using the Velocity engine occurs
     */
    private void generateReport( TagLibraryInfo tagLibrary, Writer output )
            throws VelocityException
    {
        Template reportTemplate = null;

        try
        {
            // Load the Velocity template
            reportTemplate = engine.getTemplate( Configuration.getInstance()
                    .getProperty( Configuration.TEMPLATE_REPORT ) );
        }
        catch ( Exception e )
        {
            throw new VelocityException( e );
        }

        VelocityContext context = new VelocityContext();

        // Set template properties
        context.put( "tagLibName", "Tag Library" );
        context.put( "tagLib", tagLibrary );
        context.put( "attacks", AttackLibrary.getInstance().getAttacks() );
        context.put( "frame_namespace", Configuration.getInstance()
                .getProperty( Configuration.REPORT_FRAME_NAMESPACE ) );

        context.put( "context_root", Configuration.getInstance().getProperty(
                Configuration.REPORT_CONTEXT_ROOT ) );
        context.put( "extension", Configuration.getInstance().getProperty(
                Configuration.REPORT_FILE_EXTENSION ) );

        if ( reportTemplate != null )
        {
            try
            {
                // fill the template attributes
                reportTemplate.merge( context, output );
            }
            catch ( IOException ioe )
            {
                throw new VelocityException( ioe );
            }
        }
    }

    /**
     * Writes a test case using the given Attack for the given tag attribute
     * using the given Writer
     * 
     * @param tagLibrary
     *            the tag library being tested
     * @param tag
     *            the tag being tested
     * @param attr
     *            the attribute being tested
     * @param attack
     *            the attack used in the test
     * @param output
     *            the output writer where the test case is written
     * @throws VelocityException
     *             if an error using the Velocity engine occurs
     */
    private void generateAtrributeTest( TagLibraryInfo tagLibrary, TagInfo tag,
            TagAttributeInfo attr, Attack attack, Writer output )
            throws VelocityException
    {
        Template reportTemplate = null;

        try
        {
            // load the report test case Velocity template
            reportTemplate = engine.getTemplate( Configuration.getInstance()
                    .getProperty( Configuration.TEMPLATE_TEST_CASE ) );
        }
        catch ( Exception e )
        {
            throw new VelocityException( e );
        }

        VelocityContext context = new VelocityContext();

        // Create the JSP Tag test case
        String testCase = TestCase.generateTestCaseJspTag( tagLibrary, tag,
                attr, attack );

        // add the template attributes
        context.put( "tagLib", tagLibrary );
        context.put( "tag", tag );
        context.put( "attribute", attr );
        context.put( "attack", attack );
        context.put( "tag_test", testCase );
        context.put( "test_prefix", Configuration.getInstance().getProperty(
                Configuration.REPORT_TEST_PREFIX ) );
        context.put( "test_suffix", Configuration.getInstance().getProperty(
                Configuration.REPORT_TEST_SUFFIX ) );

        if ( reportTemplate != null )
        {
            try
            {
                // fill the attributes in the template
                reportTemplate.merge( context, output );
            }
            catch ( IOException ioe )
            {
                throw new VelocityException( ioe );
            }
        }
    }

    /**
     * Writes a test case for the given Attack embedded in the given tag using
     * the given Writer.
     * 
     * @param tagLibrary
     *            the tag library being tested
     * @param tag
     *            the tag being tested
     * @param attack
     *            the attack to use in testing
     * @param output
     *            the Writer to write the test to
     * @throws VelocityException
     *             if an error occurs using the Velocity engine
     */
    private void generateComponentTest( TagLibraryInfo tagLibrary, TagInfo tag,
            Attack attack, Writer output ) throws VelocityException
    {
        Template reportTemplate = null;

        try
        {
            // load the Velocity template
            reportTemplate = engine.getTemplate( Configuration.getInstance()
                    .getProperty( Configuration.TEMPLATE_TEST_CASE ) );
        }
        catch ( Exception e )
        {
            throw new VelocityException( e );
        }

        VelocityContext context = new VelocityContext();

        // generate the JSP tag test case
        String testCase = TestCase.generateTagTextTestCaseJspTag( tagLibrary,
                tag, attack );

        // add the template attributes
        context.put( "tagLib", tagLibrary );
        context.put( "tag", tag );
        context.put( "attack", attack );
        context.put( "tag_test", testCase );

        context.put( "test_prefix", Configuration.getInstance().getProperty(
                Configuration.REPORT_TEST_PREFIX ) );
        context.put( "test_suffix", Configuration.getInstance().getProperty(
                Configuration.REPORT_TEST_SUFFIX ) );

        if ( reportTemplate != null )
        {
            try
            {
                // fill the attributes in the template
                reportTemplate.merge( context, output );
            }
            catch ( IOException ioe )
            {
                throw new VelocityException( ioe );
            }
        }
    }
}
