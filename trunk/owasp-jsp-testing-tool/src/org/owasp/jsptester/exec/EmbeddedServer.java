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

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Embedded;
import org.owasp.jsptester.conf.Configuration;

/**
 * @author Jason Li
 * 
 */
public class EmbeddedServer
{

    Embedded server;

    private static String getDocBaseDir()
    {
        return ( new File( Configuration.getInstance().getProperty( Configuration.EMBEDDED_DOC_BASE ) ) )
                .getAbsolutePath();
    }

    private static String getWebRoot()
    {
        return new File( Configuration.getInstance().getProperty( Configuration.EMBEDDED_WEB_ROOT ) )
                .getAbsolutePath();
    }

    private static int getPortNum()
    {
        return Integer.parseInt( Configuration.getInstance().getProperty( Configuration.EMBEDDED_PORT_NUM ) );
    }

    public void init()
    {
        // instantiate a new instance of Embedded class
        server = new Embedded();

        // set relevant properties

        // call create engine
        Engine engine = server.createEngine();

        // set relevant engine properties

        // Create host
        Host host = server.createHost( "localhost", getDocBaseDir() );

        // Add host to engine
        engine.addChild( host );
        engine.setDefaultHost( "localhost" );

        Context context = server.createContext( "", getWebRoot() );

        host.addChild( context );

        server.addEngine( engine );

        Connector connector = server.createConnector( "127.0.0.1",
                getPortNum(), false );

        server.addConnector( connector );
    }

    public void start() throws LifecycleException
    {
        server.start();
    }

    public static void main( String[] args ) throws Exception
    {
        EmbeddedServer es = new EmbeddedServer();
        es.init();
        es.start();
        //
        // Embedded embedded;
        // Host host;
        //        
        // Engine engine = null;
        // // Set the home directory
        // System.setProperty("catalina.home", getAppBaseDir());
        //
        // // Create an embedded server
        // embedded = new Embedded();
        //
        // // Create an engine
        // engine = embedded.createEngine();
        // engine.setDefaultHost("localhost");
        //
        // // Create a default virtual host
        // host = embedded.createHost("localhost", getAppBaseDir()
        // + "/webapps");
        // engine.addChild(host);
        //
        // // Create the ROOT context
        // Context context = embedded.createContext("",
        // getAppBaseDir() + "/webapps/ROOT");
        // host.addChild(context);
        //
        // // Install the assembled container hierarchy
        // embedded.addEngine(engine);
        //
        // // Assemble and install a default HTTP connector
        // Connector connector =
        // embedded.createConnector((String) null, 8080, false);
        // embedded.addConnector(connector);
        // // Start the embedded server
        // embedded.start();

        Thread.sleep( 600000 );

    }
}
