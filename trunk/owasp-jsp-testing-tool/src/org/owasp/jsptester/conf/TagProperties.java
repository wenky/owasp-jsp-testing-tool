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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Jason Li
 * 
 */
public class TagProperties
{
    /*
     * tagName.prop_name=value tagName.prefix=value tagName.suffix=value
     */

    private static final String DEFAULT_PREFIX = "<%@ taglib uri=\"http://java.sun.com/jsf/core\" prefix=\"f\" %>\n<f:view>";

    private static final String DEFAULT_SUFFIX = "</f:view>";

    private final Properties tagProperties;

    public TagProperties()
    {
        tagProperties = new Properties();
    }

    public String getTagPrefix( String tagName )
    {
        return this.tagProperties.getProperty( tagName + "-prefix",
                DEFAULT_PREFIX );
    }

    public void setTagPrefix( String tagName, String prefix )
    {
        this.tagProperties.setProperty( tagName + "-prefix", prefix );
    }

    public String getTagSuffix( String tagName )
    {
        return this.tagProperties.getProperty( tagName + "-suffix",
                DEFAULT_SUFFIX );
    }

    public void setTagSuffix( String tagName, String suffix )
    {
        this.tagProperties.setProperty( tagName + "-suffix", suffix );
    }

    public boolean hasTagProperty( String tagName, String propertyName )
    {
        return this.tagProperties.containsKey( tagName + "." + propertyName );
    }

    public String getTagProperty( String tagName, String propertyName )
    {
        return this.tagProperties.getProperty( tagName + "." + propertyName );
    }

    public void setTagProperty( String tagName, String propertyName,
            String property )
    {
        this.tagProperties.setProperty( tagName + "." + propertyName, property );
    }

    public void load( File file ) throws IOException
    {
        this.tagProperties.clear();
        this.tagProperties.loadFromXML( new FileInputStream( file ) );
    }

    public void save( File file ) throws IOException
    {
        this.tagProperties.storeToXML( new FileOutputStream( file ),
                "OWASP JSP Testing Tool Tag Properties" );
    }
}
