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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * Encapsulates the configuration parameters for the JSP Tester
 * @author Jason Li
 * 
 */
public class Configuration
{
    public static final String ATTACK_LIBRARY = "ATTACK_LIBRARY";

    public static final String EMBEDDED_DOC_BASE = "EMBEDDED_DOC_BASE";
    public static final String EMBEDDED_WEB_ROOT = "EMBEDDED_WEB_ROOT";
    public static final String EMBEDDED_PORT_NUM = "EMBEDDED_PORT_NUM";
    
    public static final String TEMPLATE_REPORT = "REPORT_REPORT_TEMPLATE";
    public static final String TEMPLATE_TEST_CASE = "REPORT_TEST_CASE_TEMPLATE";
    public static final String TEMPLATE_INDEX_JSP = "REPORT_INDEX_JSP";
    public static final String TEMPLATE_ERROR_JSP = "REPORT_ERROR_JSP";
    public static final String TEMPLATE_META_INF = "REPORT_META_INF_FOLDER";
    public static final String TEMPLATE_WEB_INF = "REPORT_WEB_INF_FOLDER";
    
    public static final String REPORT_CONTEXT_ROOT = "REPORT_CONTEXT_ROOT";
    public static final String REPORT_FILE_EXTENSION = "REPORT_FILE_EXTENSION";
    public static final String REPORT_FRAME_NAMESPACE = "REPORT_FRAME_NAMESPACE";

    public static final String REPORT_TEST_PREFIX = "REPORT_TEST_PREFIX";
    public static final String REPORT_TEST_SUFFIX = "REPORT_TEST_SUFFIX";

    private static final Properties DEFAULTS = new Properties();
        
    // set defaults for properties
    static {
        
        DEFAULTS.setProperty( "ATTACK_LIBRARY", "resources/attacks.xml" );
        
        DEFAULTS.setProperty( EMBEDDED_DOC_BASE, "./report" );
        DEFAULTS.setProperty( EMBEDDED_WEB_ROOT, "./report" );
        DEFAULTS.setProperty( EMBEDDED_PORT_NUM, "8096" );
        
        DEFAULTS.setProperty( TEMPLATE_REPORT, "template/report.vm" );
        DEFAULTS.setProperty( TEMPLATE_TEST_CASE, "template/testcase.vm" );
        DEFAULTS.setProperty( TEMPLATE_INDEX_JSP, "template/index.jsp" );
        DEFAULTS.setProperty( TEMPLATE_ERROR_JSP, "template/error.jsp" );
        DEFAULTS.setProperty( TEMPLATE_META_INF, "template/META-INF/" );
        DEFAULTS.setProperty( TEMPLATE_WEB_INF, "template/WEB-INF/" );
        
        DEFAULTS.setProperty( REPORT_CONTEXT_ROOT, "test/");
        DEFAULTS.setProperty( REPORT_FILE_EXTENSION, ".jsp");
        DEFAULTS.setProperty( REPORT_FRAME_NAMESPACE, "frame");
        
        /*
         * TODO: this is a temporary hack to make it work wit JSF. Will
         * eventually have a UI component to allow custom prefix/suffix stuff
         */
        DEFAULTS.setProperty( "REPORT_TEST_PREFIX", "<%@ taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n<f:view>");
        DEFAULTS.setProperty( "REPORT_TEST_SUFFIX", "</f:view>");
    }    
    
    private static final Configuration INSTANCE = new Configuration();
    public static Configuration getInstance()
    {
        return INSTANCE;
    }
    
    private final Properties config;
    
    private Configuration()
    {
        config = new Properties(DEFAULTS);
    }    
    
    /**
     * @param key
     * @param defaultValue
     * @return
     * @see java.util.Properties#getProperty(java.lang.String, java.lang.String)
     */
    public String getProperty( String key, String defaultValue )
    {
        return this.config.getProperty( key, defaultValue );
    }

    /**
     * @param key
     * @return
     * @see java.util.Properties#getProperty(java.lang.String)
     */
    public String getProperty( String key )
    {
        return getProperty( key, DEFAULTS.getProperty( key ));
    }

    /**
     * @param inStream
     * @throws IOException
     * @see java.util.Properties#load(java.io.InputStream)
     */
    public void load( InputStream inStream ) throws IOException
    {
        this.config.load( inStream );
    }

    /**
     * @param reader
     * @throws IOException
     * @see java.util.Properties#load(java.io.Reader)
     */
    public void load( Reader reader ) throws IOException
    {
        this.config.load( reader );
    }
    
}
