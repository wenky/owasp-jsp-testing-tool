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
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.owasp.jsptester.attack.Attack;
import org.owasp.jsptester.attack.AttackLibrary;
import org.owasp.jsptester.conf.Configuration;
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

    private Attack[] attacks;

    private ReportGenerator reportGenerator;

    public JspTester( String libraryFileLocation ) throws Exception
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
        LOGGER.entering( JspTester.class.getName(), "testLibrary" );

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

        List/* <File> */files = reportGenerator.generateTagReport( tagLibrary,
                attacks, outputDir );

        EmbeddedServer server = new EmbeddedServer();
        server.init();
        server.start();

        File reportOut = new File("./output");
        reportOut.mkdirs();
        for ( Iterator i = files.iterator(); i.hasNext(); )
        {
            try
            {
                File f = (File) i.next();
                LOGGER.info( f.getName() );
                URL test = new URL(
                        "http://localhost:"
                        + Configuration.getInstance().getProperty(
                                Configuration.EMBEDDED_PORT_NUM )
                        + "/"
                        + Configuration.getInstance().getProperty(
                                Configuration.REPORT_CONTEXT_ROOT) 
                                        + f.getName() )  ;
                LOGGER.info( "trying: " + test );
                TestCaseSerializer tcs = new TestCaseSerializer( test );
                

                tcs.serialize( new File( reportOut, f.getName() ) );
            }
            catch ( Exception e )
            {
                LOGGER.throwing( this.getClass().getName(), "testLibrary", e );
            }
        }

        LOGGER.exiting( JspTester.class.getName(), "testLibrary" );
    }

    /**
     * @param args
     */
    public static void main( String[] args ) throws Exception
    {
        Handler handler = new ConsoleHandler();
        handler.setLevel( Level.INFO );
        Logger.getLogger( "" ).setLevel( Level.INFO );
        Logger.getLogger( "" ).addHandler( handler );

        String tldFile = "resources/subset.tld";
        String outputDir = Configuration.getInstance().getProperty(
                Configuration.EMBEDDED_DOC_BASE );

        JspTester tester = new JspTester( tldFile );
        tester.testLibrary( outputDir );
    }

}
