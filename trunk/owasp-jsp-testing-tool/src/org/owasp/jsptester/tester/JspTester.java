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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.commons.io.FileUtils;
import org.owasp.jsptester.attack.Attack;
import org.owasp.jsptester.attack.AttackLibrary;
import org.owasp.jsptester.conf.Configuration;
import org.owasp.jsptester.conf.TagProperties;
import org.owasp.jsptester.exec.EmbeddedServer;
import org.owasp.jsptester.exec.TestCaseSerializer;
import org.owasp.jsptester.parser.TagFileParser;
import org.owasp.jsptester.report.ReportGenerator;

/**
 * @author Jason Li
 * 
 */
public class JspTester
{

    private static final Logger LOGGER = Logger.getLogger( JspTester.class
            .getName() );

    private TagLibraryInfo tagLibrary;

    private TagProperties tagProperties;

    private Attack[] attacks;

    private ReportGenerator reportGenerator;

    public JspTester( String libraryFileLocation, String tagPropsFileLocation )
            throws Exception
    {
        File libraryFile = new File( libraryFileLocation );
        if ( !libraryFile.exists() || libraryFile.exists()
                && !libraryFile.isFile() )
        {
            throw new IllegalArgumentException(
                    "The specified TLD file does not exists" );
        }

        File tagPropsFile = new File( tagPropsFileLocation );
        if ( !tagPropsFile.exists() || tagPropsFile.exists()
                && !tagPropsFile.isFile() )
        {
            throw new IllegalArgumentException(
                    "The specified tag properties file does not exists" );
        }

        tagProperties = new TagProperties();
        tagProperties.load( tagPropsFile );

        tagLibrary = TagFileParser.loadTagFile( libraryFile );

        attacks = AttackLibrary.getInstance().getAttacks();

        reportGenerator = ReportGenerator.getInstance();
    }

    private File initOutputDir( String outputDirLocation ) throws Exception
    {
        LOGGER.entering( JspTester.class.getName(), "initOutputDir(String)" );

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

            LOGGER.fine( "Output directory [" + outputDir.getAbsolutePath()
                    + "] created" );
        }

        LOGGER.exiting( JspTester.class.getName(), "initOutputDir(String)" );

        return outputDir;
    }

    private void serializeTests( List/* <File> */files ) throws Exception
    {
        LOGGER.entering( JspTester.class.getName(), "serializeTests(List)" );

        LOGGER.fine( "Starting server..." );
        EmbeddedServer server = new EmbeddedServer();
        server.start();

        LOGGER.fine( "Server started" );

        File reportOut = new File( Configuration.getInstance().getProperty(
                Configuration.REPORT_OUTPUT_DIR )
                + File.separatorChar
                + Configuration.getInstance().getProperty(
                        Configuration.REPORT_CONTEXT_ROOT ) );
        reportOut.mkdirs();

        LOGGER.fine( "Created report output directory." );

        int counter = 0;

        for ( Iterator i = files.iterator(); i.hasNext(); counter++ )
        {
            try
            {
                File f = (File) i.next();
                URL test = new URL( "http://localhost:"
                        + Configuration.getInstance().getProperty(
                                Configuration.EMBEDDED_PORT_NUM )
                        + "/"
                        + Configuration.getInstance().getProperty(
                                Configuration.REPORT_CONTEXT_ROOT )
                        + f.getName() );
                LOGGER.finer( "Using URL: " + test );
                TestCaseSerializer tcs = new TestCaseSerializer( test );

                tcs.serialize( new File( reportOut, f.getName() ) );
            }
            catch ( Exception e )
            {
                LOGGER.throwing( this.getClass().getName(),
                        "testLibrary(String)", e );
            }

            LOGGER.finer( "Free Memory: " + Runtime.getRuntime().freeMemory() );
            if ( Runtime.getRuntime().freeMemory() < 1000000 )
            {
                LOGGER.info( "Memory level at: "
                        + Runtime.getRuntime().freeMemory() + ". Made "
                        + counter + " iterations. Server restarting..." );
                counter = 0;
                server.stop();
                server = null;
                System.gc();
                server = new EmbeddedServer();
                server.start();
                LOGGER.info( "Restarted" );
            }
        }

        LOGGER.exiting( JspTester.class.getName(), "serializeTests(List)" );
    }

    public void testLibrary( String outputDirLocation ) throws Exception
    {
        LOGGER.entering( JspTester.class.getName(), "testLibrary(String)" );

        File outputDir = this.initOutputDir( outputDirLocation );

        List/* <File> */files = reportGenerator.generateLibraryReport(
                tagLibrary, tagProperties, attacks, outputDir );

        // TEMP
//        File reportDir = new File( Configuration.getInstance().getProperty(
//                Configuration.EMBEDDED_DOC_BASE ) );
//        File[] reportDirFiles = reportDir.listFiles( new FilenameFilter()
//        {
//
//            /*
//             * (non-Javadoc)
//             * 
//             * @see java.io.FilenameFilter#accept(java.io.File,
//             *      java.lang.String)
//             */
//            public boolean accept( File dir, String name )
//            {
//                return name.endsWith( Configuration.getInstance().getProperty(
//                        Configuration.REPORT_FILE_EXTENSION ) );
//            }
//
//        } );
//        List files = Arrays.asList( reportDirFiles );
        // END TEMP

        serializeTests( files );

        FileUtils.copyFileToDirectory( new File( Configuration.getInstance()
                .getProperty( Configuration.EMBEDDED_DOC_BASE )
                + File.separatorChar
                + Configuration.getInstance().getProperty(
                        Configuration.REPORT_FILE_NAME ) ), new File(
                Configuration.getInstance().getProperty(
                        Configuration.REPORT_OUTPUT_DIR ) ) );

        LOGGER.exiting( JspTester.class.getName(), "testLibrary" );
    }

    public void testTag( String outputDirLocation, String tagName )
            throws Exception
    {
        LOGGER.entering( JspTester.class.getName(), "testTag(String, String" );

        File outputDir = this.initOutputDir( outputDirLocation );

        TagInfo tag = tagLibrary.getTag( tagName );

        if ( tag == null )
        {
            throw new IllegalArgumentException( tagName
                    + " not found in tag library" );
        }

        List/* <File> */files = reportGenerator.generateTagReport( tagLibrary,
                tagProperties, tag, attacks, outputDir );

        serializeTests( files );

        LOGGER.exiting( JspTester.class.getName(), "testTag(String, String" );
    }

    /**
     * @param args
     */
    public static void main( String[] args ) throws Exception
    {
        String tagName = args[0];

        FileHandler fHandler = null;
        FileHandler attrHandler = null;
        try
        {
            Handler handler = new ConsoleHandler();
            handler.setLevel( Level.INFO );
            Logger.getLogger( "" ).setLevel( Level.ALL );
//            Logger.getLogger( "" ).addHandler( handler );

            fHandler = new FileHandler( System.getProperty( "user.home" )
                    + File.separatorChar + "Documents" + File.separatorChar
                    + "JSP Testing Tool Output" + File.separatorChar + tagName
                    + "-log.txt" );
            fHandler.setLevel( Level.INFO );
            Logger.getLogger( "" ).addHandler( fHandler );

            attrHandler = new FileHandler( System.getProperty( "user.home" )
                    + File.separatorChar + "Documents" + File.separatorChar
                    + "JSP Testing Tool Output" + File.separatorChar
                    + "req-attr-log.txt", true );
            attrHandler.setLevel( Level.SEVERE );
            Logger.getLogger( "" ).addHandler( attrHandler );

            String tldFile = "resources/html_basic.tld";
            String tagPropsFile = "resources/html_basic.tpx";
            String outputDir = Configuration.getInstance().getProperty(
                    Configuration.EMBEDDED_DOC_BASE );

            JspTester tester = new JspTester( tldFile, tagPropsFile );
            tester.testLibrary( outputDir );
            // tester.testTag( outputDir, tagName );
        }
        catch ( OutOfMemoryError oome )
        {
            LOGGER.severe( "Out of Memory" );
            System.exit( -1 );
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
