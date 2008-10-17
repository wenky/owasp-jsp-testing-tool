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
package org.owasp.jsptester.conf.tagproperties.ui;

import java.awt.Component;

import javax.servlet.jsp.tagext.TagInfo;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * @author Jason Li
 *
 */
public class TagInfoListCellRenderer implements ListCellRenderer
{

    private ListCellRenderer listCellRenderer;
    
    public TagInfoListCellRenderer()
    {
        listCellRenderer = new DefaultListCellRenderer();
    }
    
    public TagInfoListCellRenderer(ListCellRenderer renderer)
    {
        this.listCellRenderer = renderer;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.DefaultListCellRenderer#getListCellRe-)ndererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent( JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus )
    {
        if (value instanceof TagInfo) {
            value = ((TagInfo) value).getTagName();
        }
        return listCellRenderer.getListCellRendererComponent( list, value, index, isSelected,
                cellHasFocus );
    }

    /**
     * @return the listCellRenderer
     */
    public ListCellRenderer getListCellRenderer()
    {
        return this.listCellRenderer;
    }

    /**
     * Sets the listCellRenderer to the value specified by listCellRenderer
     *
     * @param listCellRenderer the listCellRenderer to set
     */
    public void setListCellRenderer( ListCellRenderer listCellRenderer )
    {
        this.listCellRenderer = listCellRenderer;
    }
    
}
