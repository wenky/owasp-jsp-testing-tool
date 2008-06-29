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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.JdkLogChute;
import org.owasp.jsptester.attack.Attack;
import org.owasp.jsptester.attack.AttackLibrary;

public class ReportGenerator
{

    private static final Logger LOGGER = Logger
            .getLogger( ReportGenerator.class.getName() );

    private static final String FRAME_NAMESPACE = "frame";

    private static ReportGenerator INSTANCE;

    private final VelocityEngine engine = new VelocityEngine();

    private ReportGenerator() throws Exception
    {
        engine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                JdkLogChute.class.getName() );

        engine.init();
    }

    public synchronized static final ReportGenerator getInstance()
            throws Exception
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new ReportGenerator();
        }

        return INSTANCE;
    }

    private static final String getReportTemplate()
    {
        // TODO: Do this in a configuration file
        File f = new File( "template/report.vm" );
        if ( !f.exists() )
        {
            throw new RuntimeException( "File doesn't exist " );
        }

        return "template/report.vm";
    }

    private static final String getTestCaseTemplate()
    {
        // TODO: Do this in a configuration file
        File f = new File( "template/testcase.vm" );
        if ( !f.exists() )
        {
            throw new RuntimeException( "File doesn't exist " );
        }

        return "template/testcase.vm";
    }

    private static void copyFile( File src, File dst ) throws IOException
    {
        if ( !src.exists() )
        {
            throw new IllegalArgumentException( "Source file does not exist" );
        }

        if ( dst.exists() && dst.isDirectory() )
        {
            throw new IllegalArgumentException(
                    "Destination file exists as a directory" );
        }

        FileReader reader = null;
        FileWriter writer = null;
        try
        {
            reader = new FileReader( src );
            writer = new FileWriter( dst );

            int read = -1;
            while ( ( read = reader.read() ) >= 0 )
            {
                writer.write( read );
            }
            
            writer.flush();
        }
        catch ( IOException ioe )
        {
            LOGGER.throwing( ReportGenerator.class.getName(), "copyFile", ioe );
            throw ioe;
        }
        finally
        {
            try
            {
                if ( reader != null )
                {
                    reader.close();
                }
            }
            catch ( IOException ioe )
            {
                LOGGER.log( Level.WARNING, "Unable to close reader", ioe );
            }
            try
            {
                if ( writer != null )
                {
                    writer.close();
                }
            }
            catch ( IOException ioe )
            {
                LOGGER.log( Level.WARNING, "Unable to close writer", ioe );
            }

        }

    }

    public void copyBase( File outputDir ) throws IOException
    {
        File indexJsp = new File( "template/index.jsp" );
        File indexCopy = new File( outputDir, "index.jsp" );
        
        copyFile( indexJsp, indexCopy );
        
        File errorJsp = new File( "template/error.jsp" );
        File errorCopy = new File( outputDir, "error.jsp" );
        
        copyFile( errorJsp, errorCopy );

    }

    public void generateReport( TagLibraryInfo tagLibrary, Writer output )
            throws Exception
    {
        Template reportTemplate = engine.getTemplate( getReportTemplate() );

        VelocityContext context = new VelocityContext();

        context.put( "tagLibName", "Tag Library" );
        context.put( "tagLib", tagLibrary );
        context.put( "attacks", AttackLibrary.getInstance().getAttacks() );
        context.put( "frame_namespace", FRAME_NAMESPACE );

        // TODO: these need to be in a configuration file
        context.put( "context_root", "test/" );
        context.put( "extension", ".jsp" );

        if ( reportTemplate != null )
        {
            reportTemplate.merge( context, output );
        }
    }

    public void generateAtrributeTest( TagLibraryInfo tagLibrary, TagInfo tag,
            TagAttributeInfo attr, Attack attack, Writer output )
            throws Exception
    {
        Template reportTemplate = engine.getTemplate( getTestCaseTemplate() );

        VelocityContext context = new VelocityContext();

        String testCase = TestCase.generateTestCase( tagLibrary, tag, attr,
                attack );

        context.put( "tagLib", tagLibrary );
        context.put( "tag", tag );
        context.put( "attribute", attr );
        context.put( "attack", attack );
        context.put( "tag_test", testCase );

        /*
         * TODO: this is a temporary hack to make it work wit JSF. Will
         * eventually have a UI component to allow custom prefix/suffix stuff
         */
        context
                .put(
                        "test_prefix",
                        new String[]
                            {
                                    "<%@ taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>",
                                    "<f:view>" } );
        context.put( "test_suffix", new String[]
            { "</f:view>" } );

        if ( reportTemplate != null )
        {
            reportTemplate.merge( context, output );
        }
    }

    public void generateComponentTest( TagLibraryInfo tagLibrary, TagInfo tag,
            Attack attack, Writer output ) throws Exception
    {
        Template reportTemplate = engine.getTemplate( getTestCaseTemplate() );

        VelocityContext context = new VelocityContext();

        String testCase = TestCase.generateTagTextTestCase( tagLibrary, tag,
                attack );

        context.put( "tagLib", tagLibrary );
        context.put( "tag", tag );
        context.put( "attack", attack );
        context.put( "tag_test", testCase );

        /*
         * TODO: this is a temporary hack to make it work wit JSF. Will
         * eventually have a UI component to allow custom prefix/suffix stuff
         */
        context
                .put(
                        "test_prefix",
                        new String[]
                            {
                                    "<%@ taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>",
                                    "<f:view>" } );
        context.put( "test_suffix", new String[]
            { "</f:view>" } );

        if ( reportTemplate != null )
        {
            reportTemplate.merge( context, output );
        }
    }
}
