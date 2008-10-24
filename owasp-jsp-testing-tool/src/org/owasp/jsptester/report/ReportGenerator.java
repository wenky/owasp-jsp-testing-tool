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
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.owasp.esapi.codecs.JavaScriptCodec;
import org.owasp.jsptester.attack.Attack;
import org.owasp.jsptester.attack.AttackLibrary;
import org.owasp.jsptester.conf.Configuration;
import org.owasp.jsptester.conf.TagProperties;
import org.owasp.jsptester.parser.TagLibraryUtils;

/**
 * Class that encapsulates generating files related to the report web app,
 * including the individual test cases, the final summary and any other base
 * files necessary to deploy the web application
 * 
 * @author Jason Li
 * 
 */
public class ReportGenerator
{
    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger( ReportGenerator.class.getName() );

    private static final FileFilter SVN_FILTER = new FileFilter()
    {
        private static final String SVN = ".svn";

        private boolean containedInSVN( File file )
        {
            File parent = file.getParentFile();
            return file.getName().equals( SVN )
                    || ( parent != null && containedInSVN( parent ) );
        }

        public boolean accept( File pathname )
        {
            return !containedInSVN( pathname );
        }

    };

    /**
     * Encoder to encode attack for test case
     */
    private static final JavaScriptCodec CODEC = new JavaScriptCodec();

    /**
     * Singleton instance of the report generator
     */
    private static ReportGenerator INSTANCE;

    /**
     * Velocity template engine
     */
    private final VelocityEngine engine = new VelocityEngine();

    /**
     * Creates an instance of the report generator
     * 
     * @throws VelocityException
     *             if an error occurs initialize the Velocity engine
     */
    private ReportGenerator() throws VelocityException
    {
        // Set the engine to use the Java Logger
        engine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                JdkLogChute.class.getName() );

        try
        {
            // initialize the Velocity engine
            engine.init();
        }
        catch ( Exception e )
        {
            throw new VelocityException( e );
        }
    }

    /**
     * Returns the singleton instance of the report generator
     * 
     * @return the singleton instance of the report generator
     * @throws VelocityException
     *             if the Velocity engine cannot be initialized
     */
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
        LOGGER
                .entering( ReportGenerator.class.getName(), "copyBase",
                        outputDir );

        // copy the index.jsp
        FileUtils.copyFileToDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.TEMPLATE_INDEX_JSP ) ), outputDir );

        LOGGER.fine( "Copied index.jsp" );

        // copy the error handler (error.jsp)
        FileUtils.copyFileToDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.TEMPLATE_ERROR_JSP ) ), outputDir );

        LOGGER.fine( "Copied error.jsp" );

        // copy the META-INF folder
        FileUtils.copyDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.TEMPLATE_META_INF ) ), new File(
                outputDir, "META-INF" ), SVN_FILTER );

        LOGGER.fine( "Copied META-INF folder" );

        // copy the WEB-INF folder
        FileUtils.copyDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.TEMPLATE_WEB_INF ) ), new File(
                outputDir, "WEB-INF" ), SVN_FILTER );

        LOGGER.fine( "Copied WEB-INF folder" );

        LOGGER.exiting( ReportGenerator.class.getName(), "copyBase" );
    }

    /**
     * Generates a report on the given tag library using the given attacks and
     * places the output in the given outputDir.
     * 
     * @param tagLibrary
     *            the tag library to test
     * @param tagProperties
     *            the tag properties to use
     * @param attacks
     *            the set of attacks to use
     * @param outputDir
     *            the output directory to place the report in
     * @return a <ocde>List&lt;File&gt;</code> of files generated
     * @throws IOException
     *             if any I/O error occurs
     */
    public List/* <File> */generateLibraryReport( TagLibraryInfo tagLibrary,
            TagProperties tagProperties, Attack[] attacks, File outputDir )
            throws IOException
    {

        LOGGER.entering( ReportGenerator.class.getName(),
                "generateLibraryReport", new Object[]
                    { tagLibrary, tagProperties, attacks, outputDir } );

        // keep track of generated test case files
        List/* <File> */generatedTestCases = new ArrayList/* <File> */();

        // Copy the base files over
        copyBase( outputDir );

        LOGGER.fine( "Base files copied" );

        generateLibraryReportFile( tagLibrary, outputDir );

        LOGGER.fine( "Main report file generated" );

        TagInfo[] tags = tagLibrary.getTags();

        // generate test case for each tag
        for ( int tagIdx = 0; tagIdx < tags.length; tagIdx++ )
        {
            generatedTestCases.addAll( generateTagReport( tagLibrary,
                    tagProperties, tags[tagIdx], attacks, outputDir ) );
            LOGGER.finer( "Test case for " + tags[tagIdx].getTagName()
                    + " generated" );
        }

        LOGGER.fine( "Test cases generated" );

        LOGGER.exiting( ReportGenerator.class.getName(),
                "generateLibraryReport", generatedTestCases );

        return generatedTestCases;
    }

    /**
     * Generates a report on the given tag from the given tag library using the
     * given attacks and places the output in the given outputDir.
     * 
     * @param tagLibrary
     *            the tag library being used
     * @param tagProperties
     *            the tag properties to use
     * @param tag
     *            the tag to be tested
     * @param attacks
     *            the set of attacks to use the output directory to place the
     *            report in
     * @param outputDir
     *            the output directory to place the report in
     * @return a <code>List&lt;File&gt;</code> of files generated
     * @throws IOException
     *             if any I/O error occurs
     */
    public List/* <File> */generateTagReport( TagLibraryInfo tagLibrary,
            TagProperties tagProperties, TagInfo tag, Attack[] attacks,
            File outputDir ) throws IOException
    {

        LOGGER.entering( ReportGenerator.class.getName(), "generateTagReport",
                new Object[]
                    { tagLibrary, tagProperties, tag, attacks, outputDir } );

        // keep track of the generated files
        List/* <File> */generatedTestCases = new ArrayList/* <File> */();

        // copy base files over
        copyBase( outputDir );

        LOGGER.fine( "Base files copied" );

        // For each tag, test each attack embedded inside the component
        for ( int attackIdx = 0; attackIdx < attacks.length; attackIdx++ )
        {
            Attack attack = attacks[attackIdx];

            FileWriter compFileWriter = null;

            try
            {
                // Create a test case file [tagName-attackName.jsp]
                File compFile = new File( outputDir, tag.getTagName()
                        + "-"
                        + attack.getName()
                        + Configuration.getInstance().getProperty(
                                Configuration.REPORT_FILE_EXTENSION ) );
                compFileWriter = new FileWriter( compFile );

                // generate the component test file
                writeComponentTest( tagLibrary, tagProperties, tag, attack,
                        compFileWriter );

                LOGGER.finer( "Test file " + compFile.getCanonicalPath()
                        + " generated" );

                generatedTestCases.add( compFile );
            }
            finally
            {
                IOUtils.closeQuietly( compFileWriter );
                compFileWriter = null;
            }
        }

        LOGGER.fine( "Component tests generated." );

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
                    File attrFile = new File( outputDir, tag.getTagName()
                            + "-"
                            + attr.getName()
                            + "-"
                            + attack.getName()
                            + Configuration.getInstance().getProperty(
                                    Configuration.REPORT_FILE_EXTENSION ) );
                    compFileWriter = new FileWriter( attrFile );

                    writeAtrributeTest( tagLibrary, tagProperties, tag,
                            attrs[attrIdx], attacks[attackIdx], compFileWriter );

                    LOGGER.finer( "Test file " + attrFile.getCanonicalPath()
                            + " file generated" );

                    generatedTestCases.add( attrFile );
                }
                finally
                {
                    IOUtils.closeQuietly( compFileWriter );
                }
            }
        }

        LOGGER.fine( "Attribute tests generated." );

        // Generate the tag report file
        generateTagReportFile( tagLibrary, tag, outputDir );

        LOGGER.fine( "Tag report generated" );

        LOGGER.exiting( ReportGenerator.class.getName(), "generateTagReport",
                generatedTestCases );

        return generatedTestCases;
    }

    /**
     * Crates the main tag library report file from a Velocity report template
     * 
     * @param tagLibrary
     *            the tab library being tested
     * @param outputDir
     *            the output directory to place the report in
     * @throws VelocityException
     *             if an error using the Velocity engine occurs
     */
    private void generateLibraryReportFile( TagLibraryInfo tagLibrary,
            File outputDir ) throws VelocityException
    {

        LOGGER.entering( ReportGenerator.class.getName(),
                "generateLibraryReportFile", new Object[]
                    { tagLibrary, outputDir } );

        File reportFile = new File( outputDir, Configuration.getInstance()
                .getProperty( Configuration.REPORT_FILE_NAME ) );
        FileWriter writer = null;

        try
        {
            writer = new FileWriter( reportFile );

            Template reportTemplate = null;

            try
            {
                // Load the Velocity template
                reportTemplate = engine.getTemplate( Configuration
                        .getInstance().getProperty(
                                Configuration.TEMPLATE_LIBRARY_REPORT ) );
            }
            catch ( Exception e )
            {
                throw new VelocityException( e );
            }

            LOGGER.fine( "Retrieved Velocity template" );

            VelocityContext context = new VelocityContext();

            // Set template properties
            context.put( "tagLibName", "Tag Library" );
            context.put( "tagLib", tagLibrary );
            context.put( "attacks", AttackLibrary.getInstance().getAttacks() );
            context.put( "frame_namespace", Configuration.getInstance()
                    .getProperty( Configuration.REPORT_FRAME_NAMESPACE ) );

            context.put( "context_root", Configuration.getInstance()
                    .getProperty( Configuration.REPORT_CONTEXT_ROOT ) );
            context.put( "extension", Configuration.getInstance().getProperty(
                    Configuration.REPORT_FILE_EXTENSION ) );

            LOGGER.fine( "Placed attributes in Velocity conetxt" );

            if ( reportTemplate != null )
            {
                // fill the template attributes
                reportTemplate.merge( context, writer );
            }

            LOGGER.fine( "Executed template merge" );
        }
        catch ( IOException ioe )
        {
            throw new VelocityException( ioe );
        }
        finally
        {
            IOUtils.closeQuietly( writer );
            writer = null;
        }

        LOGGER.exiting( ReportGenerator.class.getName(),
                "generateLibraryReportFile" );
    }

    /**
     * Creates the report file for an individual tag from a Velocity report
     * template
     * 
     * @param tagLibrary
     *            the tab library to use
     * @param tag
     *            the tag being tested
     * @param outputDir
     *            the output directory to place the report in
     * @throws VelocityException
     *             if an error using the Velocity engine occurs
     */
    private void generateTagReportFile( TagLibraryInfo tagLibrary, TagInfo tag,
            File outputDir ) throws VelocityException
    {

        LOGGER.entering( ReportGenerator.class.getName(),
                "generateTagReportFile", new Object[]
                    { tagLibrary, tag, outputDir } );

        File reportFile = new File( outputDir, tag.getTagName() + ".html" );
        FileWriter writer = null;

        try
        {
            writer = new FileWriter( reportFile );

            Template reportTemplate = null;

            try
            {
                // Load the Velocity template
                reportTemplate = engine.getTemplate( Configuration
                        .getInstance().getProperty(
                                Configuration.TEMPLATE_TAG_REPORT ) );
            }
            catch ( Exception e )
            {
                throw new VelocityException( e );
            }

            LOGGER.fine( "Retrieved Velocity template" );

            VelocityContext context = new VelocityContext();

            // Set template properties
            context.put( "tagLibName", "Tag Library" );
            context.put( "tagLib", tagLibrary );
            context.put( "tag", tag );
            context.put( "attacks", AttackLibrary.getInstance().getAttacks() );
            context.put( "frame_namespace", Configuration.getInstance()
                    .getProperty( Configuration.REPORT_FRAME_NAMESPACE ) );

            context.put( "context_root", Configuration.getInstance()
                    .getProperty( Configuration.REPORT_CONTEXT_ROOT ) );
            context.put( "extension", Configuration.getInstance().getProperty(
                    Configuration.REPORT_FILE_EXTENSION ) );

            LOGGER.fine( "Placed attributes in Velocity conetxt" );

            if ( reportTemplate != null )
            {
                // fill the template attributes
                reportTemplate.merge( context, writer );
            }

            LOGGER.fine( "Executed template merge" );
        }
        catch ( IOException ioe )
        {
            throw new VelocityException( ioe );
        }
        finally
        {
            IOUtils.closeQuietly( writer );
            writer = null;
        }

        LOGGER.exiting( ReportGenerator.class.getName(),
                "generateTagReportFile" );
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
    private void writeAtrributeTest( TagLibraryInfo tagLibrary,
            TagProperties tagProperties, TagInfo tag, TagAttributeInfo attr,
            Attack attack, Writer output ) throws VelocityException
    {

        LOGGER.entering( ReportGenerator.class.getName(), "writeAttributeTest",
                new Object[]
                    { tagLibrary, tagProperties, tag, attr, attack, output } );

        // verify that required attributes are configured
        verifyRequiredAttributes( tag, attr, tagProperties );

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

        LOGGER.fine( "Retrieved Velocity template" );

        VelocityContext context = new VelocityContext();

        // Create the JSP Tag test case
        String testCase = TestCase.generateAttrTestCaseJspTag( tagLibrary, tag,
                attr, attack, TagLibraryUtils.getRequiredAttributesMap( tag,
                        tagProperties ) );

        LOGGER.fine( "Created tag" );
        LOGGER.finest( "\tTag: " + testCase );

        // add the template attributes
        context.put( "tagLib", tagLibrary );
        context.put( "tag", tag );
        context.put( "attribute", attr );
        context.put( "attack", attack );
        context
                .put( "encoded_attack", CODEC.encode( attack.getAttackString() ) );
        context.put( "tag_test", testCase );

        context.put( "test_prefix", tagProperties.getTagPrefix( tag
                .getTagName() ) );
        context.put( "test_suffix", tagProperties.getTagSuffix( tag
                .getTagName() ) );

        LOGGER.fine( "Placed attributes in Velocity conetxt" );

        if ( reportTemplate != null )
        {
            try
            {
                // fill the attributes in the template
                reportTemplate.merge( context, output );
                LOGGER.fine( "Executed template merge" );
            }
            catch ( IOException ioe )
            {
                throw new VelocityException( ioe );
            }
        }

        LOGGER.exiting( ReportGenerator.class.getName(), "writeAttributeTest" );
    }

    /**
     * Writes a test case for the given Attack embedded in the given tag using
     * the given Writer.
     * 
     * @param tagLibrary
     *            the tag library being tested
     * @param tagProperties
     *            the tag properties to use
     * @param tag
     *            the tag being tested
     * @param attack
     *            the attack to use in testing
     * @param output
     *            the Writer to write the test to
     * @throws VelocityException
     *             if an error occurs using the Velocity engine
     */
    private void writeComponentTest( TagLibraryInfo tagLibrary,
            TagProperties tagProperties, TagInfo tag, Attack attack,
            Writer output ) throws VelocityException
    {
        LOGGER.entering( ReportGenerator.class.getName(), "writeComponentTest",
                new Object[]
                    { tagLibrary, tagProperties, tag, attack, output } );

        // verify that all required attributes are configured
        verifyRequiredAttributes( tag, null, tagProperties );

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

        LOGGER.fine( "Retrieved Velocity template" );

        VelocityContext context = new VelocityContext();

        // generate the JSP tag test case
        String testCase = TestCase.generateTagTestCaseJspTag( tagLibrary, tag,
                attack, TagLibraryUtils.getRequiredAttributesMap( tag,
                        tagProperties ) );

        LOGGER.fine( "Created tag" );
        LOGGER.finest( "\tTag: " + testCase );

        // add the template attributes
        context.put( "tagLib", tagLibrary );
        context.put( "tag", tag );
        context.put( "attack", attack );
        context.put( "encoded_attack", "" );
        context.put( "tag_test", testCase );

        context.put( "test_prefix", tagProperties.getTagPrefix( tag
                .getTagName() ) );
        context.put( "test_suffix", tagProperties.getTagSuffix( tag
                .getTagName() ) );

        LOGGER.fine( "Placed attributes in Velocity conetxt" );

        if ( reportTemplate != null )
        {
            try
            {
                // fill the attributes in the template
                reportTemplate.merge( context, output );
                LOGGER.fine( "Executed template merge" );
            }
            catch ( IOException ioe )
            {
                throw new VelocityException( ioe );
            }
        }

        LOGGER.exiting( ReportGenerator.class.getName(), "writeComponentTest" );
    }

    /**
     * Checks to see that all the required attributes for the given tag are
     * configured in the tag properties file
     * 
     * @param tag
     *            the tag to test
     * @param attr
     *            the attribute being tested
     * @param tagProperties
     *            the tag properties to use
     */
    private void verifyRequiredAttributes( TagInfo tag, TagAttributeInfo attr,
            TagProperties tagProperties )
    {
        LOGGER.entering( ReportGenerator.class.getName(),
                "verifyRequiredAttributes", new Object[]
                    { tag, attr, tagProperties } );

        // if the tag has required attributes, check to see that these
        // attributes are configured in the tag properties file
        if ( TagLibraryUtils.hasRequiredAttributes( tag ) )
        {
            // get the set of required attributes
            Set/* <TagAttributeInfo> */reqAttrs = TagLibraryUtils
                    .getRequiredAttributes( tag );

            boolean reqAttrsConfigured = true;

            // loop over required attributes
            for ( Iterator itr = reqAttrs.iterator(); itr.hasNext()
                    && reqAttrsConfigured; )
            {
                TagAttributeInfo reqAttr = (TagAttributeInfo) itr.next();

                // if the required attribute is the one being tested, don't need
                // to pull from tag properties
                if ( reqAttr.equals( attr ) )
                {
                    continue;
                }

                // if the tag properties file doesn't not contain the required
                // attribute, tag is not configured properly
                reqAttrsConfigured &= tagProperties.hasTagProperty( tag
                        .getTagName(), reqAttr.getName() );
            }

            // if not all required attributes are configured, log a warning
            // message
            if ( !reqAttrsConfigured )
            {
                LOGGER.warning( tag.getTagName()
                        + " has unconfigured required attributes: "
                        + TagLibraryUtils.getRequiredAttributes( tag ) );
            }
        }

        LOGGER.exiting( ReportGenerator.class.getName(),
                "verifyRequiredAttributes" );
    }
}
