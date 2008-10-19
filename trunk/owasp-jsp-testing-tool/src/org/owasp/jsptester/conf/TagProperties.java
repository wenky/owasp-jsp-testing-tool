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
 * Encapsulates the tag property configuration values for each tag's attributes
 * as well as any necessary prefixes or suffixes to the construction of the JSP
 * tag.
 * 
 * @author Jason Li
 * 
 */
public class TagProperties
{
    /**
     * The tag properties
     */
    private final Properties tagProperties = new Properties();;

    /**
     * Returns the prefix to use for the given tag
     * 
     * @param tagName
     *            the tag to look up the prefix for
     * @return the prefix to use for the given tag
     */
    public String getTagPrefix( String tagName )
    {
        return this.tagProperties.getProperty( tagName + "-prefix", "" );
    }

    /**
     * Sets the prefix for the given tag to the given prefix
     * 
     * @param tagName
     *            the tag to set
     * @param prefix
     *            the prefix to set to
     */
    public void setTagPrefix( String tagName, String prefix )
    {
        this.tagProperties.setProperty( tagName + "-prefix", prefix );
    }

    /**
     * Returns the suffix to use for the given tag
     * 
     * @param tagName
     *            the tag to look up the suffix for
     * @return the suffix to use for the given tag
     */
    public String getTagSuffix( String tagName )
    {
        return this.tagProperties.getProperty( tagName + "-suffix", "" );
    }

    /**
     * Sets the suffix for the given tag to the given suffix
     * 
     * @param tagName
     *            the tag to set
     * @param suffix
     *            the suffix to set to
     */
    public void setTagSuffix( String tagName, String suffix )
    {
        this.tagProperties.setProperty( tagName + "-suffix", suffix );
    }

    /**
     * Returns true if the given tag has a configured prefix value; false
     * otherwise
     * 
     * @param tagName
     *            the tag to check
     * @return true if the given tag has a configured prefix value; false
     *         otherwise
     */
    public boolean hasTagPrefix( String tagName )
    {
        return this.tagProperties.containsKey( tagName + "-prefix" );
    }

    /**
     * Returns true if the given tag has a configured suffix value; false
     * otherwise
     * 
     * @param tagName
     *            the tag to check
     * @return true if the given tag has a configured suffix value; false
     *         otherwise
     */
    public boolean hasTagSuffix( String tagName )
    {
        return this.tagProperties.containsKey( tagName + "-suffix" );
    }

    /**
     * Returns true if the given tag has a configured value for the given
     * property; false otherwise
     * 
     * @param tagName
     *            the tag to check
     * @param propertyName
     *            the property to check
     * @return true if the given tag has a configured value for the given
     *         property; false otherwise
     */
    public boolean hasTagProperty( String tagName, String propertyName )
    {
        return this.tagProperties.containsKey( tagName + "." + propertyName );
    }

    /**
     * Returns the configuration value for the given property for the given tag
     * 
     * @param tagName
     *            the tag
     * @param propertyName
     *            the name of the property
     * @return the configuration value for the given property for the given tag
     */
    public String getTagProperty( String tagName, String propertyName )
    {
        return this.tagProperties
                .getProperty( tagName + "." + propertyName, "" );
    }

    /**
     * Sets the configuration value of the given property for the given tag to
     * the given value
     * 
     * @param tagName
     *            the tag
     * @param propertyName
     *            the name of the property
     * @param property
     *            the value of the property
     */
    public void setTagProperty( String tagName, String propertyName,
            String property )
    {
        this.tagProperties.setProperty( tagName + "." + propertyName, property );
    }

    /**
     * Loads the tag properties from the given file
     * 
     * @param file
     *            the file to load
     * @throws IOException
     *             if an I/O error occurs
     */
    public void load( File file ) throws IOException
    {
        this.tagProperties.clear();
        this.tagProperties.loadFromXML( new FileInputStream( file ) );
    }

    /**
     * Saves the tag properties to the given file
     * 
     * @param file
     *            the file to save to
     * @throws IOException
     *             if an I/O error occurs
     */
    public void save( File file ) throws IOException
    {
        this.tagProperties.storeToXML( new FileOutputStream( file ),
                "OWASP JSP Testing Tool Tag Properties" );
    }
}
