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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpContentTooLargeException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

/**
 * @author Jason Li
 * 
 */
public class TestCaseSerializer
{
    private static final Logger LOGGER = Logger.getLogger( TestCaseSerializer.class.getName() );
    
    private final URL page;

    public TestCaseSerializer( URL page )
    {
        this.page = page;
    }

    public void serialize(File outputFile) throws URISyntaxException
    {
        LOGGER.entering( this.getClass().getName(), "serialize(File)" );
        
        HttpClient httpClient = new HttpClient();

        // HttpConnectionManagerParams params = httpClient
        // .getHttpConnectionManager().getParams();
        // params.setConnectionTimeout(timeout);
        // params.setSoTimeout(timeout);
        // httpClient.getHttpConnectionManager().setParams(params);

        URI pageUri = page.toURI();

        GetMethod pageRequest = new GetMethod( pageUri.toString() );

        InputStream responseBody = null;
        OutputStream output = null;
        try
        {
            // get test case from embedded server
            httpClient.executeMethod( pageRequest );
            responseBody = pageRequest.getResponseBodyAsStream();
            
            output = new FileOutputStream(outputFile);
            
            IOUtils.copy( responseBody, output );
        }
//        catch ( HttpContentTooLargeException hctle )
//        {
//            // TODO: proper error handling
//            hctle.printStackTrace();            
//        }
        catch ( IOException ioe )
        {
            LOGGER.log( Level.WARNING, "Error serializing test case", ioe );            
        }
        finally
        {
            pageRequest.releaseConnection();
            IOUtils.closeQuietly( responseBody );
            IOUtils.closeQuietly( output );
        }

        LOGGER.exiting( this.getClass().getName(), "serialize" );
    }
    
    public static void main(String[] args) throws Exception
    {
        TestCaseSerializer tcs = new TestCaseSerializer(new URL("http://localhost:8096/test/outputLabel-raw.jsp"));
        tcs.serialize(new File("./output/outputLabel-raw.html"));
        
    }
}
