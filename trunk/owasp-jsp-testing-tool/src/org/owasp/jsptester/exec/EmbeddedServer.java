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
package org.owasp.jsptester.exec;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;
import org.owasp.jsptester.conf.Configuration;

/**
 * Encapsulates operations involving the embedded Tomcat server
 * 
 * @author Jason Li
 * 
 */
public class EmbeddedServer
{

    /**
     * Default Tomcat port number
     */
    public static final int DEFAULT_PORT = 8096;

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger( EmbeddedServer.class
            .getName() );

    /**
     * The embedded Tomcat server
     */
    private Embedded server;

    /**
     * Returns the document base for the server
     * 
     * @return the document base for the server
     */
    private static String getDocBaseDir()
    {
        return ( new File( Configuration.getInstance().getProperty(
                Configuration.EMBEDDED_DOC_BASE ) ) ).getAbsolutePath();
    }

    /**
     * Returns the web root to use for the server
     * 
     * @return the web root to use for the server
     */
    private static String getWebRoot()
    {
        return new File( Configuration.getInstance().getProperty(
                Configuration.EMBEDDED_WEB_ROOT ) ).getAbsolutePath();
    }

    /**
     * Returns the port number to use for the embedded Tomcat server
     * 
     * @return the port number to use for the embedded Tomcat server
     */
    private static int getPortNum()
    {
        int port = DEFAULT_PORT;
        try
        {
            port = Integer.parseInt( Configuration.getInstance().getProperty(
                    Configuration.EMBEDDED_PORT_NUM ) );
        }
        catch ( NumberFormatException nfe )
        {
            LOGGER.log( Level.INFO, "Configuration port number is "
                    + "not a valid port number. Using default port number ("
                    + DEFAULT_PORT + ")", nfe );
        }

        return port;
    }

    /**
     * Creates an instance of <code>EmbeddedServer</code>
     */
    public EmbeddedServer()
    {
        init();
    }

    /**
     * Initializes the embedded Tomcat server
     */
    private void init()
    {
        LOGGER.entering( EmbeddedServer.class.getName(), "init" );
        
        // instantiate a new instance of Embedded class
        server = new Embedded();

        // TODO: set relevant properties

        // call create engine
        Engine engine = server.createEngine();

        // TODO: set relevant engine properties

        // Create host
        Host host = server.createHost( "localhost", getDocBaseDir() );

        // Add host to engine
        engine.addChild( host );
        engine.setDefaultHost( "localhost" );

        // create server context
        Context context = server.createContext( "", getWebRoot() );

        // add context to host
        host.addChild( context );

        // add engine to server
        server.addEngine( engine );

        // create net connector
        Connector connector = server.createConnector( "127.0.0.1",
                getPortNum(), false );

        // add connector
        server.addConnector( connector );
        
        LOGGER.exiting( EmbeddedServer.class.getName(), "init" );
    }

    /**
     * Starts the embedded Tomcat server
     * 
     * @throws LifecycleException
     * @see org.apache.catalina.startup.Embedded#start()
     */
    public void start() throws LifecycleException
    {
        LOGGER.fine( "Starting embedded Tomcat server" );
        
        this.server.start();
        
        LOGGER.fine( "Started embedded Tomcat server" );
    }

    /**
     * Stops the embedded Tomcat server
     * 
     * @throws LifecycleException
     * @see org.apache.catalina.startup.Embedded#stop()
     */
    public void stop() throws LifecycleException
    {
        LOGGER.fine( "Stopping embedded Tomcat server" );
        
        this.server.stop();
        
        LOGGER.fine( "Stopped embedded Tomcat server" );
    }

    /**
     * Test code
     * 
     * @deprecated
     */
    public static void main( String[] args ) throws Exception
    {
        EmbeddedServer es = new EmbeddedServer();
        es.init();
        es.start();

        // keep the server running for 10 minutes
        Thread.sleep( 600000 );

    }
}
