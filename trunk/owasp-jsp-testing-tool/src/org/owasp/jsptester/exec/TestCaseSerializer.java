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
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

/**
 * Encapsulates the process of serializing a test case.
 * 
 * @author Jason Li
 * 
 */
public class TestCaseSerializer
{

    /**
     * Logger
     */
    private static final Logger LOGGER = Logger
            .getLogger( TestCaseSerializer.class.getName() );

    /**
     * Serialize the test case by downloading the given URL and copying the
     * contents to the given output file.
     * 
     * @param page
     *            the page to download
     * @param outputFile
     *            the output file
     * 
     * @throws IOException
     *             if an I/O error occurs
     * @throws URISyntaxException
     *             if the page URL is formatted incorrectly
     */
    public static void serialize( URL page, File outputFile )
            throws IOException, URISyntaxException
    {
        LOGGER.entering( TestCaseSerializer.class.getName(), "serialize",
                new Object[]
                    { page, outputFile } );

        if ( !page.getProtocol().startsWith( "http" ) )
        {
            throw new IllegalArgumentException(
                    "Only HTTP/S progocol is supported" );
        }

        HttpClient httpClient = new HttpClient();

        URI pageUri = page.toURI();

        GetMethod pageRequest = new GetMethod( pageUri.toString() );

        InputStream responseBody = null;
        OutputStream output = null;
        try
        {
            // get test case from embedded server
            httpClient.executeMethod( pageRequest );
            responseBody = pageRequest.getResponseBodyAsStream();

            output = new FileOutputStream( outputFile );

            IOUtils.copy( responseBody, output );
        }
        finally
        {
            pageRequest.releaseConnection();
            IOUtils.closeQuietly( responseBody );
            IOUtils.closeQuietly( output );
        }

        LOGGER.exiting( TestCaseSerializer.class.getName(), "serialize" );
    }

    /**
     * Test code
     * 
     * @deprecated
     */
    public static void main( String[] args ) throws Exception
    {
        TestCaseSerializer.serialize( new URL(
                "http://localhost:8096/test/outputLabel-raw.jsp" ), new File(
                "./output/outputLabel-raw.html" ) );

    }
}
