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
package org.owasp.jsptester.conf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.owasp.jsptester.exec.EmbeddedServer;

/**
 * Encapsulates the configuration parameters for the JSP Tester
 * 
 * @author Jason Li
 * 
 */
public class Configuration
{
    /**
     * The location of the attack library XML file
     */
    public static final String ATTACK_LIBRARY = "ATTACK_LIBRARY";

    /**
     * The location where the document base for the embedded Tomcat instance
     */
    public static final String EMBEDDED_DOC_BASE = "EMBEDDED_DOC_BASE";

    /**
     * The folder containing the web root for the embedded Tomcat instance
     */
    public static final String EMBEDDED_WEB_ROOT = "EMBEDDED_WEB_ROOT";

    /**
     * The port number to use for the embedded Tomcat instance
     */
    public static final String EMBEDDED_PORT_NUM = "EMBEDDED_PORT_NUM";

    /**
     * The library report template file
     */
    public static final String TEMPLATE_LIBRARY_REPORT = "REPORT_LIBRARY_REPORT_TEMPLATE";

    /**
     * The tag report template file
     */
    public static final String TEMPLATE_TAG_REPORT = "REPORT_TAG_REPORT_TEMPLATE";

    /**
     * The test case template file
     */
    public static final String TEMPLATE_TEST_CASE = "REPORT_TEST_CASE_TEMPLATE";

    /**
     * The index.jsp file for the test site
     */
    public static final String TEMPLATE_INDEX_JSP = "REPORT_INDEX_JSP";

    /**
     * The error.jsp (error handler) file for the test suite
     */
    public static final String TEMPLATE_ERROR_JSP = "REPORT_ERROR_JSP";

    /**
     * Location of the template META-INF folder to use to build the web
     * application using the tag library
     */
    public static final String TEMPLATE_META_INF = "REPORT_META_INF_FOLDER";

    /**
     * Location of the template WEB-INF folder to use to build the web
     * application using the tag library
     */
    public static final String TEMPLATE_WEB_INF = "REPORT_WEB_INF_FOLDER";

    /**
     * The context root of the web application
     */
    public static final String REPORT_CONTEXT_ROOT = "REPORT_CONTEXT_ROOT";

    /**
     * The extension to use for the test case files
     */
    public static final String REPORT_FILE_EXTENSION = "REPORT_FILE_EXTENSION";

    /**
     * The namespace to use when naming iframes in the report file
     */
    public static final String REPORT_FRAME_NAMESPACE = "REPORT_FRAME_NAMESPACE";

    /**
     * The name to use for the tag library report file
     */
    public static final String REPORT_FILE_NAME = "REPORT_FILE_NAME";

    /*
     * Default configuration properties
     */
    private static final Properties DEFAULTS = new Properties();

    // set defaults for properties
    static
    {

        DEFAULTS.setProperty( ATTACK_LIBRARY, "resources/attacks.xml" );

        DEFAULTS.setProperty( EMBEDDED_DOC_BASE, System
                .getProperty( "java.io.tmpdir" )
                + File.separatorChar
                + "JSP Testing Tool Output" );
        DEFAULTS.setProperty( EMBEDDED_WEB_ROOT, System
                .getProperty( "java.io.tmpdir" )
                + File.separatorChar
                + "JSP Testing Tool Output"
                + File.separatorChar + "report" );
        DEFAULTS.setProperty( EMBEDDED_PORT_NUM, String
                .valueOf( EmbeddedServer.DEFAULT_PORT ) );

        DEFAULTS.setProperty( TEMPLATE_LIBRARY_REPORT, "template/report.vm" );
        DEFAULTS.setProperty( TEMPLATE_TAG_REPORT, "template/tag-report.vm" );
        DEFAULTS.setProperty( TEMPLATE_TEST_CASE, "template/testcase.vm" );
        DEFAULTS.setProperty( TEMPLATE_INDEX_JSP, "template/index.jsp" );
        DEFAULTS.setProperty( TEMPLATE_ERROR_JSP, "template/error.jsp" );
        DEFAULTS.setProperty( TEMPLATE_META_INF, "template/META-INF/" );
        DEFAULTS.setProperty( TEMPLATE_WEB_INF, "template/WEB-INF/" );

        DEFAULTS.setProperty( REPORT_CONTEXT_ROOT, "test/" );
        DEFAULTS.setProperty( REPORT_FILE_EXTENSION, ".jsp" );
        DEFAULTS.setProperty( REPORT_FRAME_NAMESPACE, "frame" );
        DEFAULTS.setProperty( REPORT_FILE_NAME, "report.html" );

    }

    /**
     * The singleton instance of the configuration
     */
    private static final Configuration INSTANCE = new Configuration();

    /**
     * Returns the singleton instance of the configuration
     * 
     * @return the singleton instance of the configuration
     */
    public static Configuration getInstance()
    {
        return INSTANCE;
    }

    /**
     * The configuration properties
     */
    private final Properties config;

    /**
     * Creates an instance of <code>Configuration</code>
     */
    private Configuration()
    {
        config = new Properties( DEFAULTS );
    }

    /**
     * Returns the configuration value for the given key, using the default if
     * unavailable
     * 
     * @param key
     *            the key to lookup
     * @return the configuration value for the given key, using the default if
     *         unavailable
     * @see java.util.Properties#getProperty(java.lang.String)
     */
    public String getProperty( String key )
    {
        return this.config.getProperty( key, DEFAULTS.getProperty( key ) );
    }

    /**
     * Load the configuration from the given
     * 
     * @param inStream
     *            the input stream
     * @throws IOException
     *             if an I/O error occurs
     * @see java.util.Properties#load(java.io.InputStream)
     */
    public void load( InputStream inStream ) throws IOException
    {
        this.config.load( inStream );
    }

    /**
     * Load configuration from the given <code>Reader</code>
     * 
     * @param reader
     *            the reader
     * @throws IOException
     *             if an I/O error occurs
     * @see java.util.Properties#load(java.io.Reader)
     */
    public void load( Reader reader ) throws IOException
    {
        this.config.load( reader );
    }

}
