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
package org.owasp.jsptester.parser;

import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

/**
 * Concrete implementation of the <code>TagLibrary</code> class.
 * 
 * @author Jason Li
 * 
 */
public class TagLibraryInfoImpl extends TagLibraryInfo
{

    /**
     * Creates an instance of the <code>TagLibraryInfoImpl</code> with the
     * given prefix and uri.
     * 
     * @param prefix
     *            the tag prefix
     * @param uri
     *            the tag library uri
     */
    TagLibraryInfoImpl( String prefix, String uri )
    {
        super( prefix, uri );

        shortname = prefix;
    }

    /**
     * Returns an array of <code>TagLibraryInfo</code> objects representing
     * the entire set of tag libraries (including this
     * <code>TagLibraryInfo</code>) imported by taglib directives in the
     * translation unit that references this <code>TagLibraryInfo</code>.
     * 
     * @return an array of <code>TagLibraryInfo</code> objects representing
     *         the entire set of tag libraries (including this
     *         <code>TagLibraryInfo</code>) imported by taglib directives in
     *         the translation unit that references this
     *         <code>TagLibraryInfo</code>.
     * @see javax.servlet.jsp.tagext.TagLibraryInfo#getTagLibraryInfos()
     */
    public TagLibraryInfo[] getTagLibraryInfos()
    {
        return new TagLibraryInfo[]
            { this };
    }

    /**
     * Sets the library's tags
     * 
     * @param newTags
     *            the libary's tags
     */
    void setTags( TagInfo[] newTags )
    {
        tags = newTags;
    }

    /**
     * Sets the version of the tag library
     * 
     * @param newTlibVersion
     *            the tag library version
     */
    void setTlibVersion( String newTlibVersion )
    {
        tlibversion = newTlibVersion;
    }

    /**
     * Sets the JSP version
     * 
     * @param newJspVersion
     *            the JSP version
     */
    void setJspVersion( String newJspVersion )
    {
        jspversion = newJspVersion;
    }

    /**
     * Sets the information for the tag library
     * 
     * @param newInfo
     *            the information for the tag library
     */
    void setInfo( String newInfo )
    {
        info = newInfo;
    }
}
