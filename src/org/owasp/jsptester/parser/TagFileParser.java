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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.owasp.jsptester.report.ReportGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Jason Li
 * 
 */
public class TagFileParser
{

    private TagFileParser() {}
    
    public static TagLibraryInfo loadTagFile( File tldFile )
            throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;

        db = dbf.newDocumentBuilder();

        Document dom = null;

        /*
         * Load and parse the file.
         */
        dom = db.parse( tldFile );

        Element taglib = dom.getDocumentElement();

        return parseTagLibElement( taglib );
    }

    private static TagLibraryInfo parseTagLibElement( Element taglib )
    {
        NodeList nodes = taglib.getChildNodes();

        String tlibversion = null;
        String jspversion = null;
        String shortname = null;
        String uri = null;
        String info = null;

        Vector tagNodes = new Vector();

        // JSP 1.2 elements
        String displayName = null;
        String smallIcon = null;
        String largeIcon = null;
        String description = null;

        Node validator = null;
        Vector listenerNodes = new Vector();

        // JSP 2.1 elements
        Vector tagFileNodes = new Vector();
        Vector functionNodes = new Vector();
        Vector taglibExtensionNodes = new Vector();

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "tlibversion".equals( childNode.getNodeName() )
                    || "tlib-version".equals( childNode.getNodeName() ) )
            {
                tlibversion = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "jspversion".equals( childNode.getNodeName() )
                    || "jsp-version".equals( childNode.getNodeName() ) )
            {
                jspversion = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "shortname".equals( childNode.getNodeName() )
                    || "short-name".equals( childNode.getNodeName() ) )
            {
                shortname = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "uri".equals( childNode.getNodeName() ) )
            {
                uri = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "info".equals( childNode.getNodeName() ) )
            {
                info = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "tag".equals( childNode.getNodeName() ) )
            {
                tagNodes.add( childNode );
            }
            else if ( "display-name".equals( childNode.getNodeName() ) )
            {
                displayName = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "small-icon".equals( childNode.getNodeName() ) )
            {
                smallIcon = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "large-icon".equals( childNode.getNodeName() ) )
            {
                largeIcon = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "description".equals( childNode.getNodeName() ) )
            {
                description = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "validator".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse validators
                validator = childNode;
            }
            else if ( "listener".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse listeners
                listenerNodes.add( childNode );
            }
            else if ( "icon".equals( childNode.getNodeName() ) )
            {
                String[] iconInfo = parseIconElement( childNode );
                smallIcon = iconInfo[0];
                largeIcon = iconInfo[1];
            }
            else if ( "tag-file".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse tagFiles
                tagFileNodes.add( childNode );
            }
            else if ( "function".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse functions
                functionNodes.add( childNode );
            }
            else if ( "taglib-extension".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse taglib-extensions
                taglibExtensionNodes.add( childNode );
            }
            else
            {
                // TODO: throw an appropriate error
                System.err.println( "Unknown element encountered: "
                        + childNode.getNodeName() );
            }
        }

        if ( tlibversion == null )
        {
            // TODO: throw an appropriate error
            System.err
                    .println( "TagLib element without required tlibversion element encountered" );
        }

        if ( shortname == null )
        {
            // TODO: throw an appropriate error
            System.err
                    .println( "TagLib element without required shortname element encountered" );
        }

        // technically JSP Version is required in version 1.2

        TagLibraryInfoImpl tagLibraryInfo = new TagLibraryInfoImpl( shortname,
                uri );
        tagLibraryInfo.setTlibVersion( tlibversion );
        tagLibraryInfo.setJspVersion( jspversion );
        tagLibraryInfo.setInfo( info );

        if ( tagNodes.size() < 1 )
        {
            // TODO: throw an appropriate error
            System.err
                    .println( "TagLib element without required tag element encountered" );
        }

        TagInfo[] tags = new TagInfo[tagNodes.size()];
        for ( int tagIdx = 0; tagIdx < tagNodes.size(); tagIdx++ )
        {
            tags[tagIdx] = parseTagElement( (Node) tagNodes.get( tagIdx ),
                    tagLibraryInfo );
        }
        tagLibraryInfo.setTags( tags );

        return tagLibraryInfo;
    }

    private static TagInfo parseTagElement( Node tag,
            TagLibraryInfo tagLibraryInfo )
    {
        NodeList nodes = tag.getChildNodes();

        String name = null;
        String tagclass = null;
        String teiclass = null;
        String bodycontent = "JSP";
        String info = null;

        Vector attributes = new Vector();

        // JSP 1.2 attributes
        String displayName = null;
        String smallIcon = null;
        String largeIcon = null;
        String description = null;
        String example = null;

        Vector variableNodes = new Vector();

        // JSP 2.1 attributes
        boolean dynamicAttributes;

        Vector tagExtensionNodes = new Vector();

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "name".equals( childNode.getNodeName() ) )
            {
                name = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "tagclass".equals( childNode.getNodeName() )
                    || "tag-class".equals( childNode.getNodeName() ) )
            {
                tagclass = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "teiclass".equals( childNode.getNodeName() )
                    || "tei-class".equals( childNode.getNodeName() ) )
            {
                teiclass = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "bodycontent".equals( childNode.getNodeName() )
                    || "body-content".equals( childNode.getNodeName() ) )
            {
                bodycontent = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "info".equals( childNode.getNodeName() ) )
            {
                info = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "attribute".equals( childNode.getNodeName() ) )
            {
                TagAttributeInfo attribute = parseAttributeElement( childNode );
                attributes.add( attribute );
            }
            else if ( "display-name".equals( childNode.getNodeName() ) )
            {
                displayName = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "small-icon".equals( childNode.getNodeName() ) )
            {
                smallIcon = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "large-icon".equals( childNode.getNodeName() ) )
            {
                largeIcon = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "description".equals( childNode.getNodeName() ) )
            {
                description = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "variable".equals( childNode.getNodeName() ) )
            {
                variableNodes.add( childNode );
            }
            else if ( "example".equals( childNode.getNodeName() ) )
            {
                example = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "dynamic-attributes".equals( childNode.getNodeName() ) )
            {
                dynamicAttributes = parseTldBoolean( parseElementString( parseElementString( childNode
                        .getTextContent() ) ) );
            }
            else
            {
                // TODO: throw an appropriate error
                System.err.println( "Unknown element encountered" );
            }
        }

        if ( name == null )
        {
            // TODO: throw appropriate error
            System.err
                    .println( "Tag element without required name element encountered" );
        }

        if ( tagclass == null )
        {
            // TODO: throw appropriate error
            System.err
                    .println( "Tag element without required tagclass element encountered" );
        }

        // TODO: have to do something for tag library extra info
        return new TagInfo( name, tagclass, bodycontent, info, tagLibraryInfo,
                null, (TagAttributeInfo[]) attributes
                        .toArray( new TagAttributeInfo[attributes.size()] ) );
    }

    private static TagAttributeInfo parseAttributeElement( Node attribute )
    {
        NodeList nodes = attribute.getChildNodes();

        String name = null;
        boolean required = false;
        String type = "java.lang.String";
        boolean rtexprvalue = false;

        // JSP 1.2 attributes
        String description = null;

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "name".equals( childNode.getNodeName() ) )
            {
                name = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "required".equals( childNode.getNodeName() ) )
            {
                required = parseTldBoolean( parseElementString( parseElementString( childNode
                        .getTextContent() ) ) );
            }
            else if ( "rtexprvalue".equals( childNode.getNodeName() ) )
            {
                rtexprvalue = parseTldBoolean( parseElementString( parseElementString( childNode
                        .getTextContent() ) ) );
            }
            else if ( "type".equals( childNode.getNodeName() ) )
            {
                type = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "description".equals( childNode.getNodeName() ) )
            {
                description = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else
            {
                // TODO: throw an appropriate error
                System.err.println( "Unknown element encountered: "
                        + childNode.getNodeName() );
            }
        }

        if ( name == null )
        {
            // TODO: throw an appropriate error
            System.err
                    .println( "Attribute tag did not contain the required name element" );
        }

        return new TagAttributeInfo( name, required, type, rtexprvalue );
    }

    private static void parseValidatorElement( Node validator )
    {
        NodeList nodes = validator.getChildNodes();

        String validatorClass = null;
        String description = null;

        Vector initParamNodes = new Vector();

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "validator-class".equals( childNode.getNodeName() ) )
            {
                validatorClass = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "init-param".equals( childNode.getNodeName() ) )
            {
                initParamNodes.add( childNode );
            }
            if ( "description".equals( childNode.getNodeName() ) )
            {
                description = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else
            {
                // TODO: throw an appropriate error
                System.err.println( "Unknown element encountered" );
            }
        }

        if ( validatorClass == null )
        {
            // TODO: throw an appropriate error
            System.err
                    .println( "Validator tag did not contain the required validator-class element" );
        }
    }

    private static void parseListenerElement( Node listener )
    {
        NodeList nodes = listener.getChildNodes();

        String listenerClass = null;

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "listener-class".equals( childNode.getNodeName() ) )
            {
                listenerClass = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else
            {
                // TODO: throw an appropriate error
                System.err.println( "Unknown element encountered" );
            }
        }

        if ( listenerClass == null )
        {
            // TODO: throw an appropriate error
            System.err
                    .println( "Listener tag did not contain the required listener-class element" );
        }
    }

    private static TagVariableInfo parseVariableElement( Node variable )
    {
        NodeList nodes = variable.getChildNodes();

        String nameGiven = null;
        String nameFromAttribute = null;
        String variableClass = "java.lang.String";
        boolean declare = true;
        String scope = "NESTED";
        String description = null;

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "name-given".equals( childNode.getNodeName() ) )
            {
                nameGiven = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "name-from-attribute".equals( childNode.getNodeName() ) )
            {
                nameFromAttribute = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "variable-class".equals( childNode.getNodeName() ) )
            {
                variableClass = parseElementString( childNode.getTextContent() );
            }
            else if ( "declare".equals( childNode.getNodeName() ) )
            {
                declare = Boolean.getBoolean( parseElementString( childNode
                        .getTextContent() ) );
            }
            else if ( "scope".equals( childNode.getNodeName() ) )
            {
                scope = parseElementString( childNode.getTextContent() );
            }
            else if ( "description".equals( childNode.getNodeName() ) )
            {
                description = parseElementString( childNode.getTextContent() );
            }
            else
            {
                // TODO: throw an appropriate error
                System.err.println( "Unknown element encountered" );
            }
        }

        if ( nameGiven == null && nameFromAttribute == null )
        {
            // TODO: throw an appropriate error
            System.err
                    .println( "Variable tag did not contain either the name-given or name-from-attribute element" );
        }

        return new TagVariableInfo( nameGiven, nameFromAttribute,
                variableClass, declare, parseScopeType( scope ) );
    }

    private static void parseInitParamElement( Node initParam )
    {
        NodeList nodes = initParam.getChildNodes();

        String paramName = null;
        String paramValue = null;
        String description = null;

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "name-given".equals( childNode.getNodeName() ) )
            {
                paramName = parseElementString( childNode.getTextContent() );
            }
            else if ( "name-from-attribute".equals( childNode.getNodeName() ) )
            {
                paramValue = parseElementString( childNode.getTextContent() );
            }
            else if ( "description".equals( childNode.getNodeName() ) )
            {
                description = parseElementString( childNode.getTextContent() );
            }
            else
            {
                // TODO: throw an appropriate error
                System.err.println( "Unknown element encountered" );
            }
        }

        if ( paramName == null )
        {
            // TODO: throw an appropriate error
            System.err
                    .println( "Variable tag did not contain either the required param-name element" );
        }

        if ( paramValue == null )
        {
            // TODO: throw an appropriate error
            System.err
                    .println( "Variable tag did not contain either the required param-value element" );
        }

    }

    private static String[] parseIconElement( Node icon )
    {
        NodeList nodes = icon.getChildNodes();

        String smallIcon = null;
        String largeIcon = null;

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "small-icon".equals( childNode.getNodeName() ) )
            {
                smallIcon = parseElementString( childNode.getTextContent() );
            }
            else if ( "large-icon".equals( childNode.getNodeName() ) )
            {
                largeIcon = parseElementString( childNode.getTextContent() );
            }
            else
            {
                // TODO: throw an appropriate error
                System.err.println( "Unknown element encountered" );
            }
        }

        return new String[]
        { smallIcon, largeIcon };
    }

    private static void parseTagFileElement( Node tagFile )
    {
        NodeList nodes = tagFile.getChildNodes();

        String description = null;
        String displayName = null;
        String smallIcon = null;
        String largeIcon = null;
        String name = null;
        String path = null;
        String example = null;

        Vector tagExtensionNodes = new Vector();

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "description".equals( childNode.getNodeName() ) )
            {
                description = parseElementString( childNode.getTextContent() );
            }
            else if ( "display-name".equals( childNode.getNodeName() ) )
            {
                displayName = parseElementString( childNode.getTextContent() );
            }
            else if ( "icon".equals( childNode.getNodeName() ) )
            {
                String[] icon = parseIconElement( childNode );
                smallIcon = icon[0];
                largeIcon = icon[1];
            }
            else if ( "name".equals( childNode.getNodeName() ) )
            {
                name = parseElementString( childNode.getTextContent() );
            }
            else if ( "path".equals( childNode.getNodeName() ) )
            {
                path = parseElementString( childNode.getTextContent() );
            }
            else if ( "example".equals( childNode.getNodeName() ) )
            {
                example = parseElementString( childNode.getTextContent() );
            }
            else if ( "tag-extension".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse tag extensions
                tagExtensionNodes.add( childNode );
            }
            else
            {
                // TODO: throw an appropriate error
                System.err.println( "Unknown element encountered" );
            }
        }
    }

    private static void parseFunctionElement( Node function )
    {
        NodeList nodes = function.getChildNodes();

        String description = null;
        String displayName = null;
        String smallIcon = null;
        String largeIcon = null;
        String name = null;
        String functionClass = null;
        String functionSignature = null;
        String example = null;

        Vector functionExtensionNodes = new Vector();

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "description".equals( childNode.getNodeName() ) )
            {
                description = parseElementString( childNode.getTextContent() );
            }
            else if ( "display-name".equals( childNode.getNodeName() ) )
            {
                displayName = parseElementString( childNode.getTextContent() );
            }
            else if ( "icon".equals( childNode.getNodeName() ) )
            {
                String[] icon = parseIconElement( childNode );
                smallIcon = icon[0];
                largeIcon = icon[1];
            }
            else if ( "name".equals( childNode.getNodeName() ) )
            {
                name = parseElementString( childNode.getTextContent() );
            }
            else if ( "function-class".equals( childNode.getNodeName() ) )
            {
                functionClass = parseElementString( childNode.getTextContent() );
            }
            else if ( "function-signature".equals( childNode.getNodeName() ) )
            {
                functionSignature = parseElementString( childNode
                        .getTextContent() );
            }
            else if ( "example".equals( childNode.getNodeName() ) )
            {
                example = parseElementString( childNode.getTextContent() );
            }
            else if ( "function-extension".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse function extensions
                functionExtensionNodes.add( childNode );
            }
            else
            {
                // TODO: throw an appropriate error
                System.err.println( "Unknown element encountered" );
            }
        }
    }

    private static void parseTagLibExtensionElement( Node tagLibExtension )
    {

    }

    private static void parseExtensionElement( Node extension )
    {

    }

    private static boolean parseTldBoolean( String value )
    {
        return ( "true".equals( value ) || "yes".equals( value ) );
    }

    private static int parseScopeType( String value )
    {
        if ( "AT_BEGIN".equals( value ) )
        {
            return VariableInfo.AT_BEGIN;
        }

        if ( "AT_END".equals( value ) )
        {
            return VariableInfo.AT_END;
        }

        return VariableInfo.NESTED;
    }

    private static String parseElementString( String value )
    {
        return ( value == null ? null : value.trim() );
    }

    public static void main( String[] args ) throws Exception
    {
        File f = new File( "resources/html_basic.tld" );
        
        TagLibraryInfo tld = TagFileParser.loadTagFile(f);
        
        System.out.println(tld.getInfoString());
    }
}
