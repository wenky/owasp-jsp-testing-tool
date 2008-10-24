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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.catalina.LifecycleException;
import org.apache.commons.io.FileUtils;
import org.owasp.jsptester.attack.Attack;
import org.owasp.jsptester.attack.AttackLibrary;
import org.owasp.jsptester.conf.Configuration;
import org.owasp.jsptester.conf.TagProperties;
import org.owasp.jsptester.exec.EmbeddedServer;
import org.owasp.jsptester.exec.TestCaseSerializer;
import org.owasp.jsptester.parser.TagFileParser;
import org.owasp.jsptester.report.ReportGenerator;
import org.xml.sax.SAXException;

/**
 * Main class that ties all the functionality together. The executable parses
 * the given library using an attack library and generates tests for the entire
 * tag library or an individual tag and serializes the test cases.
 * 
 * @author Jason Li
 * 
 */
public class JspTester
{

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger( JspTester.class
            .getName() );

    /**
     * The tag library to test
     */
    private TagLibraryInfo tagLibrary;

    /**
     * The properties file to use for testing
     */
    private TagProperties tagProperties;

    /**
     * The list of attacks to try
     */
    private Attack[] attacks;

    /**
     * The report generator to use
     */
    private ReportGenerator reportGenerator;

    /**
     * Creates an instance of the JSPTester with the given tag library and tag
     * property file locations
     * 
     * @param libraryFileLocation
     *            the location of the tag library file
     * @param tagPropsFileLocation
     *            the location of the tag property file
     * @throws IOException
     *             if an I/O error occurs
     * @throws SAXException
     *             if an error occurs parsing the tag library file
     */
    public JspTester( String libraryFileLocation, String tagPropsFileLocation )
            throws SAXException, IOException
    {
        LOGGER.entering( JspTester.class.getName(), "JspTester", new Object[]
            { libraryFileLocation, tagPropsFileLocation } );

        LOGGER.fine( "Verifying existence of tag library file." );
        LOGGER.finer( "\tTag library file: " + libraryFileLocation );

        // verify that if the tag library file exists, it's a file and not a
        // directory
        File libraryFile = new File( libraryFileLocation );
        if ( !libraryFile.exists() || libraryFile.exists()
                && !libraryFile.isFile() )
        {
            throw new IllegalArgumentException(
                    "The specified TLD file does not exists" );
        }

        // parse the tag library file
        tagLibrary = TagFileParser.loadTagFile( libraryFile );

        LOGGER.fine( "Loaded tag library file" );
        LOGGER
                .finer( "\tUsing library file: "
                        + libraryFile.getCanonicalPath() );

        LOGGER.fine( "Verifying existence of tag properties file." );
        LOGGER.finer( "\tTag properties file: " + tagPropsFileLocation );

        // verify that if the tag properties file exists, it's a file and not a
        // directory
        File tagPropsFile = new File( tagPropsFileLocation );
        if ( !tagPropsFile.exists() || tagPropsFile.exists()
                && !tagPropsFile.isFile() )
        {
            throw new IllegalArgumentException(
                    "The specified tag properties file does not exists" );
        }

        // load the tag properties file
        tagProperties = new TagProperties();
        tagProperties.load( tagPropsFile );

        LOGGER.fine( "Loaded tag properties file" );
        LOGGER.finer( "\tUsing properties file: "
                + tagPropsFile.getCanonicalPath() );

        // get the attack library
        attacks = AttackLibrary.getInstance().getAttacks();

        LOGGER.finer( "Obtained attack library" );

        // get the report generator
        reportGenerator = ReportGenerator.getInstance();

        LOGGER.finer( "Obtained report generator" );

        LOGGER.exiting( JspTester.class.getName(), "JspTester" );
    }

    /**
     * Performs sanity checks on the output directory and creates the directory
     * if necessary
     * 
     * @param outputDirLocation
     *            the output directory location
     * @return a <code>File</code> object representing the output directory
     * @throws IOException
     *             if an I/O error occurs
     */
    private File initOutputDir( String outputDirLocation ) throws IOException
    {
        LOGGER.entering( JspTester.class.getName(), "initOutputDir",
                outputDirLocation );

        // verify that if the output directory exists, it is not a file
        File outputDir = new File( outputDirLocation );
        if ( outputDir.exists() && !outputDir.isDirectory() )
        {
            throw new IllegalArgumentException(
                    "The specified output directory is a file." );
        }
        // create the directory if it does not exist
        else if ( !outputDir.exists() )
        {
            if ( !outputDir.mkdirs() )
            {
                throw new IOException( "Unable to output directory" );
            }

            LOGGER.fine( "Output directory [" + outputDir.getCanonicalPath()
                    + "] created" );
        }

        LOGGER.exiting( JspTester.class.getName(), "initOutputDir", outputDir );

        return outputDir;
    }

    /**
     * Performs sanity checks on the build directory and creates the directory
     * if necessary
     * 
     * @return a <code>File</code> object representing the build directory
     * @throws IOException
     *             if an I/O error occurs
     */
    private File initBuildDir() throws IOException
    {
        File buildDir = new File( Configuration.getInstance().getProperty(
                Configuration.EMBEDDED_WEB_ROOT ) );

        LOGGER.fine( "Using build directory: " + buildDir.getCanonicalPath() );

        if ( buildDir.exists() && !buildDir.isDirectory() )
        {
            throw new IllegalArgumentException(
                    "Build directory exists as a file" );
        }
        else if ( !buildDir.exists() && !buildDir.mkdirs() )
        {
            throw new IOException( "Unable to create build directory" );
        }

        LOGGER.exiting( JspTester.class.getName(), "initBuildDir", buildDir );

        return buildDir;
    }

    /**
     * Removes the build directory
     * 
     * @throws IOException
     *             if an I/O error occurs
     */
    private void cleanup() throws IOException
    {
        LOGGER.entering( JspTester.class.getName(), "cleanup" );
        FileUtils.deleteDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.EMBEDDED_DOC_BASE ) ) );

        LOGGER.exiting( JspTester.class.getName(), "cleanup" );
    }

    /**
     * Serializes the test cases by downloading the processed test cases from
     * the embedded Tomcat instance to the given directory
     * 
     * @param files
     *            the <code>List&lt;File&gt;</code> of files to serialize
     * @param outputDir
     *            the directory to download files to
     * @throws IOException
     *             if an I/O error occurs
     * @throws LifecycleException
     *             if an error occurs starting, stopping or restarting the
     *             embedded Tomcat instance
     */
    private void serializeTests( List/* <File> */files, File outputDir )
            throws IOException, LifecycleException
    {
        LOGGER.entering( JspTester.class.getName(), "serializeTests",
                new Object[]
                    { files, outputDir } );

        EmbeddedServer server = new EmbeddedServer();
        try
        {
            // Start the embedded Tomcat instance
            LOGGER.fine( "Starting server..." );
            server.start();

            LOGGER.fine( "Server started" );

            // create the context root directory for the test cases
            File reportOut = new File( outputDir, Configuration.getInstance()
                    .getProperty( Configuration.REPORT_CONTEXT_ROOT ) );
            reportOut.mkdirs();

            LOGGER.fine( "Created report output directory." );

            int counter = 0;

            // loop over each of the files to be serialized
            for ( Iterator/* <File> */i = files.iterator(); i.hasNext(); counter++ )
            {
                try
                {
                    File f = (File) i.next();

                    // construct the URL for the test case
                    URL test = new URL( "http://localhost:"
                            + Configuration.getInstance().getProperty(
                                    Configuration.EMBEDDED_PORT_NUM )
                            + "/"
                            + Configuration.getInstance().getProperty(
                                    Configuration.REPORT_CONTEXT_ROOT )
                            + f.getName() );
                    LOGGER.finer( "Using URL: " + test );

                    // serialize the URL
                    TestCaseSerializer.serialize( test, new File( reportOut, f
                            .getName() ) );
                }
                catch ( URISyntaxException urise )
                {
                    LOGGER.throwing( this.getClass().getName(),
                            "testLibrary(String)", urise );
                }

                /*
                 * To ensure out of memory exceptions do not occur, whenever the
                 * memory is getting low, restart the embedded Tomcat server
                 */
                LOGGER.finer( "Free Memory: "
                        + Runtime.getRuntime().freeMemory() );
                if ( Runtime.getRuntime().freeMemory() < 1000000 )
                {
                    LOGGER.info( "Memory level at: "
                            + Runtime.getRuntime().freeMemory() + ". Made "
                            + counter + " iterations. Server restarting..." );
                    counter = 0;

                    // stop the server
                    server.stop();
                    server = null;

                    // hint the JVM to garbage collect
                    System.gc();

                    // start a new server
                    server = new EmbeddedServer();
                    server.start();
                    LOGGER.info( "Restarted" );
                }
            }
        }
        finally
        {
            server.stop();
        }

        LOGGER.exiting( JspTester.class.getName(), "serializeTests" );
    }

    /**
     * Generates a report that tests the entire tag library
     * 
     * @param outputDirLocation
     *            the location of the output directory
     * @throws IOException
     *             if an I/O error occurs
     * @throws LifecycleException
     *             if an error occurs starting, stopping or restarting the
     *             embedded Tomcat instance
     */
    public void testLibrary( String outputDirLocation ) throws IOException,
            LifecycleException
    {
        LOGGER.entering( JspTester.class.getName(), "testLibrary",
                outputDirLocation );

        // initialize the output directory
        File outputDir = this.initOutputDir( outputDirLocation );

        LOGGER.info( "Initialized output directory." );
        LOGGER.fine( "\tOutput directory: " + outputDir.getCanonicalPath() );

        // Initialize the build directory
        File buildDir = this.initBuildDir();

        LOGGER.info( "Initialized build directory." );
        LOGGER.fine( "\tBuild directory: " + buildDir.getCanonicalPath() );

        // generate the test case files
        List/* <File> */files = reportGenerator.generateLibraryReport(
                tagLibrary, tagProperties, attacks, buildDir );

        LOGGER.info( "Generated test case files." );
        LOGGER.fine( "\tTest case files: " + files );

        // load the test cases into the embedded Tomcat instance and serialize
        // them
        serializeTests( files, outputDir );

        LOGGER.info( "Serialized test cases" );

        // copy the report file to the output location
        FileUtils.copyFileToDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.EMBEDDED_WEB_ROOT )
                + File.separatorChar
                + Configuration.getInstance().getProperty(
                        Configuration.REPORT_FILE_NAME ) ), outputDir );

        TagInfo[] tags = tagLibrary.getTags();
        
        // copy the individual tag report files to the output location
        for ( int tagIdx = 0; tagIdx < tags.length; tagIdx++ )
        {
            FileUtils.copyFileToDirectory(
                    new File( Configuration.getInstance().getProperty(
                            Configuration.EMBEDDED_WEB_ROOT )
                            + File.separatorChar
                            + tags[tagIdx].getTagName()
                            + ".html" ), outputDir );
        }

        LOGGER.info( "Copied report files." );

        cleanup();

        LOGGER.info( "Performed cleanup." );

        LOGGER.exiting( JspTester.class.getName(), "testLibrary" );
    }

    /**
     * Generates a report that tests the one tag from the tag library
     * 
     * @param outputDirLocation
     *            the location of the output directory
     * @param tagName
     *            the name of the tag to test
     * @throws IOException
     *             if an I/O error occurs
     * @throws LifecycleException
     *             if an error occurs starting, stopping or restarting the
     *             embedded Tomcat instance
     */
    public void testTag( String outputDirLocation, String tagName )
            throws IOException, LifecycleException
    {
        LOGGER.entering( JspTester.class.getName(), "testTag", new Object[]
            { outputDirLocation, tagName } );

        // Initialize the output directory
        File outputDir = this.initOutputDir( outputDirLocation );

        LOGGER.fine( "Initialized output directory." );
        LOGGER.finer( "\tOutput directory: " + outputDir.getCanonicalPath() );

        // Initialize the build directory
        File buildDir = this.initBuildDir();

        LOGGER.fine( "Initialized build directory." );
        LOGGER.finer( "\tBuild directory: " + buildDir.getCanonicalPath() );

        // get the tag to test
        TagInfo tag = tagLibrary.getTag( tagName );

        // verify the tag exists
        if ( tag == null )
        {
            throw new IllegalArgumentException( tagName
                    + " not found in tag library" );
        }

        LOGGER.fine( "Testing tag: " + tag );

        // generate the test case files
        List/* <File> */files = reportGenerator.generateTagReport( tagLibrary,
                tagProperties, tag, attacks, buildDir );

        LOGGER.fine( "Generated test case files." );
        LOGGER.finer( "\tTest case files: " + files );

        // load the test cases into the embedded Tomcat instance and serialize
        // them
        serializeTests( files, outputDir );

        LOGGER.fine( "Serialized test cases" );

        // copy the report file to the output location
        FileUtils.copyFileToDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.EMBEDDED_WEB_ROOT )
                + File.separatorChar + tag.getTagName() + ".html" ), outputDir );

        cleanup();

        LOGGER.fine( "Performed cleanup." );

        LOGGER.exiting( JspTester.class.getName(), "testTag" );
    }

    /**
     * Runs the JSP Tester on the given tag/tag library based on the command
     * line arguments:
     * <ol>
     * <li>tag library file</li>
     * <li>tag properties file</li>
     * <li>name of the tag to test; if null, test the entire tag library</li>
     * </ol>
     * 
     * 
     * @param args
     *            command line arguments
     */
    public static void main( String[] args ) throws Exception
    {

        /*
         * TODO: use a real command line parser to support more flexible
         * arguments
         */
        if ( args.length < 2 )
        {
            throw new IllegalArgumentException( "Tag Library Definition File"
                    + " and tag properties file are required arguments" );
        }

        String tldFile = args[0];
        String tagPropsFile = args[1];

        String outputDir = "output";
        if ( args.length > 2 )
        {
            outputDir = args[2];
        }

        String tagName = null;
        if ( args.length > 3 )
        {
            tagName = args[3];
        }

        FileHandler fHandler = null;
        try
        {

            Handler handler = new ConsoleHandler();
            handler.setLevel( Level.INFO );

            Logger.getLogger( "" ).setLevel( Level.ALL );
            Logger.getLogger( "" ).addHandler( handler );

            // capture log output to a file for debugging
            // fHandler = new FileHandler( ( tagName == null ? "report" :
            // tagName )
            // + "-log.txt" );
            // fHandler.setLevel( Level.ALL );
            // Logger.getLogger( "" ).addHandler( fHandler );

            JspTester tester = new JspTester( tldFile, tagPropsFile );

            // if no tag name is provided, test whole library
            if ( tagName == null )
            {
                tester.testLibrary( outputDir );
            }
            // otherwise, test individual tag
            else
            {
                tester.testTag( outputDir, tagName );
            }
        }
        finally
        {
            if ( fHandler != null )
            {
                fHandler.close();
            }
        }
    }
}
