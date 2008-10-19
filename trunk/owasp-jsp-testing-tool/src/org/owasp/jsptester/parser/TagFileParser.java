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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagVariableInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Encapsulates the logic to parse a TLD file and create an instance of
 * <code>TagLibraryInfoImpl</code>.
 * 
 * @author Jason Li
 * 
 */
public class TagFileParser
{

    /**
     * Logger
     */
    private static Logger LOGGER = Logger.getLogger( TagFileParser.class
            .getName() );

    /**
     * Constructs an instance of TagLibraryParser
     */
    private TagFileParser()
    {
    }

    /**
     * Returns an instance of <code>TagLibraryInfo</code> created by loading
     * the given TLD file
     * 
     * @param tldFile
     *            the TLD file to load
     * @return an instance of TagLibraryInfo</code> created by loading the
     *         given TLD file
     * @throws SAXException
     *             if an error occurs parsing the XML
     * @throws IOException
     *             if an I/O error occurs
     */
    public static TagLibraryInfo loadTagFile( File tldFile )
            throws SAXException, IOException
    {
        // Create a document parser
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;

        try
        {
            db = dbf.newDocumentBuilder();
        }
        catch ( ParserConfigurationException pce )
        {
            throw new SAXException( pce );
        }

        Document dom = null;

        // Load and parse the file.
        dom = db.parse( tldFile );

        LOGGER.fine( "Parsed the TLD XML" );

        // Get the root element (tag-lib) of the XML document
        Element taglib = dom.getDocumentElement();

        return parseTagLibElement( taglib );
    }

    /**
     * Returns an instance of <code>TagLibraryInfo</code> created from parsing
     * the given tag-lib element
     * 
     * @param taglib
     *            the tag-lib element to parse
     * @return an instance of <code>TagLibraryInfo</code> created from parsing
     *         the given tag-lib element
     */
    private static TagLibraryInfo parseTagLibElement( Element taglib )
    {
        LOGGER.entering( TagFileParser.class.getName(), "parseTagLibElement",
                taglib );

        // JSP 1.1 elements to parse
        String tlibVersion = null;
        String jspVersion = null;
        String shortName = null;
        String uri = null;
        String info = null;

        List tagNodes = new ArrayList();

        // JSP 1.2 elements to parse
        String displayName = null;
        String smallIcon = null;
        String largeIcon = null;
        String description = null;

        Node validator = null;
        List listenerNodes = new ArrayList();

        // JSP 2.1 elements to parse
        List tagFileNodes = new ArrayList();
        List functionNodes = new ArrayList();
        List taglibExtensionNodes = new ArrayList();

        // Get the tag-lib node's child nodes
        NodeList nodes = taglib.getChildNodes();

        // iterate through all child nodes and look for the above JSP elements
        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            // if the node is not an element, skip to the next node
            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            // if the node is a tlibversion element (JSP 1.1) or tlib-version
            // element (JSP 1.2+), populate the tlibVersion from the text string
            // of the node
            if ( "tlibversion".equals( childNode.getNodeName() )
                    || "tlib-version".equals( childNode.getNodeName() ) )
            {
                tlibVersion = parseElementString( childNode.getTextContent() );
            }
            // if the node is a jspversion element (JSP 1.1) or jsp-version
            // element (JSP 1.2+), populate the jspVersion from the text string
            // of the node
            else if ( "jspversion".equals( childNode.getNodeName() )
                    || "jsp-version".equals( childNode.getNodeName() ) )
            {
                jspVersion = parseElementString( childNode.getTextContent() );
            }
            // if the node is a shortname element (JSP 1.1) or short-name
            // element (JSP 1.2+), populate the shortName from the text string
            // of the node
            else if ( "shortname".equals( childNode.getNodeName() )
                    || "short-name".equals( childNode.getNodeName() ) )
            {
                shortName = parseElementString( childNode.getTextContent() );
            }
            // if the node is a uri element, populate the uri from the text
            // string of the node
            else if ( "uri".equals( childNode.getNodeName() ) )
            {
                uri = parseElementString( childNode.getTextContent() );
            }
            // if the node is an info element, populate the info from the text
            // string of the node
            else if ( "info".equals( childNode.getNodeName() ) )
            {
                info = parseElementString( childNode.getTextContent() );
            }
            // if the node is a tag element, add the node to the list of tag
            // nodes to parse
            else if ( "tag".equals( childNode.getNodeName() ) )
            {
                tagNodes.add( childNode );
            }
            // if the node is a display-name element, populate the display-name
            // from the text string of the node
            else if ( "display-name".equals( childNode.getNodeName() ) )
            {
                displayName = parseElementString( childNode.getTextContent() );
            }
            // if the node is a small-icon element, populate the smallIcon from
            // the text string of the node
            else if ( "small-icon".equals( childNode.getNodeName() ) )
            {
                smallIcon = parseElementString( childNode.getTextContent() );
            }
            // if the node is a large-icon element, populate the largeIcon from
            // the text string of the node
            else if ( "large-icon".equals( childNode.getNodeName() ) )
            {
                largeIcon = parseElementString( childNode.getTextContent() );
            }
            // if the node is a description element, populate the description
            // from the text string of the node
            else if ( "description".equals( childNode.getNodeName() ) )
            {
                description = parseElementString( parseElementString( childNode
                        .getTextContent() ) );
            }
            // if the node is a validator element, parse the validator node to
            // create the tag's validator
            else if ( "validator".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse validator
                validator = childNode;
            }
            // if the node is a listener element, add the node to the list of
            // listeners to parse
            else if ( "listener".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse listener
                listenerNodes.add( childNode );
            }
            // if the node is an icon element, populate the smallIcon and
            // largeIcon from the element
            else if ( "icon".equals( childNode.getNodeName() ) )
            {
                String[] iconInfo = parseIconElement( childNode );
                smallIcon = iconInfo[0];
                largeIcon = iconInfo[1];
            }
            // if the node is a tag-file element, add the node to the list of
            // tag files to parse
            else if ( "tag-file".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse tag file
                tagFileNodes.add( childNode );
            }
            // if the node is a function element, add the node to the list of
            // functions to parse
            else if ( "function".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse functions
                functionNodes.add( childNode );
            }
            // if the node is a taglib-extension element, add the node to the
            // list of extensions to parse
            else if ( "taglib-extension".equals( childNode.getNodeName() ) )
            {
                // TODO: need to parse taglib-extensions
                taglibExtensionNodes.add( childNode );
            }
            else
            {
                LOGGER.fine( "Unknown element encountered: "
                        + childNode.getNodeName() );
            }
        }

        // Verify that the TLD file contained the required tlib version
        if ( tlibVersion == null )
        {
            LOGGER.warning( "TagLib element without required tlibversion"
                    + " element encountered" );
        }

        // Verify that the TLD file contained the required short name version
        if ( shortName == null )
        {
            LOGGER.warning( "TagLib element without required shortname"
                    + " element encountered" );
        }

        // Verify the TLD file contained at least one tag definition
        if ( tagNodes.size() < 1 )
        {
            LOGGER.warning( "TagLib element without required tag"
                    + " element encountered" );
        }

        // technically jsp-version is required in JSP 1.2 but since it's not
        // required in JSP 1.1, for flexibility, ignore this if not present

        // Create an instance of TagLibraryInfo and set the appropriate
        // parameters
        TagLibraryInfoImpl tagLibraryInfo = new TagLibraryInfoImpl( shortName,
                uri );
        tagLibraryInfo.setTlibVersion( tlibVersion );
        tagLibraryInfo.setJspVersion( jspVersion );
        tagLibraryInfo.setInfo( info );

        // parse the tag elements and create an array of TagInfo instances
        TagInfo[] tags = new TagInfo[tagNodes.size()];
        for ( int tagIdx = 0; tagIdx < tagNodes.size(); tagIdx++ )
        {
            tags[tagIdx] = parseTagElement( (Node) tagNodes.get( tagIdx ),
                    tagLibraryInfo );
        }

        tagLibraryInfo.setTags( tags );

        LOGGER.exiting( TagFileParser.class.getName(), "parseTagLibElement",
                tagLibraryInfo );

        return tagLibraryInfo;
    }

    /**
     * Returns an instance of the <code>TagInfo</code> class created from the
     * given tag node using the given <code>TagLibraryInfo</code>
     * 
     * @param tag
     *            the tag node to parse
     * @param tagLibraryInfo
     *            the <code>TagLibraryInfo this tag will be part of
     * @return an instance of the <code>TagInfo</code> class created from the
     *         given tag node using the given <code>TagLibraryInfo</code>
     */
    private static TagInfo parseTagElement( Node tag,
            TagLibraryInfo tagLibraryInfo )
    {
        LOGGER.entering( TagFileParser.class.getName(), "parseTagElement",
                new Object[]
                    { tag, tagLibraryInfo } );

        // JSP 1.1 elements
        String name = null;
        String tagClass = null;
        String teiClass = null;
        String bodyContent = "JSP"; // JSP is default value
        String info = null;

        // List<TagAttributeInfo>
        List attributes = new ArrayList();

        // JSP 1.2 elements
        String displayName = null;
        String smallIcon = null;
        String largeIcon = null;
        String description = null;
        String example = null;

        // List<TagVariableInfo>
        List variables = new ArrayList();

        // JSP 2.1 attributes
        boolean dynamicAttributes = false; // false is default value

        List tagExtensionNodes = new ArrayList();

        // Get the tag node's child nodes
        NodeList nodes = tag.getChildNodes();

        // Iterate through all the child nodes and look for the above JSP
        // elements
        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            // if the node is not an element, skip to the next node
            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            // if the node is a name element, populate the name from the text
            // string of the node
            if ( "name".equals( childNode.getNodeName() ) )
            {
                name = parseElementString( childNode.getTextContent() );
            }
            // if the node is a tagclass element (JSP 1.1) or a tag-class
            // element (JSP 1.2+), populate the tagClass from the text string of
            // the node
            else if ( "tagclass".equals( childNode.getNodeName() )
                    || "tag-class".equals( childNode.getNodeName() ) )
            {
                tagClass = parseElementString( childNode.getTextContent() );
            }
            // if the node is a teiclass element (JSP 1.1) or a tei-class
            // element (JSP 1.2+), populate the teiClass from the text string of
            // the node
            else if ( "teiclass".equals( childNode.getNodeName() )
                    || "tei-class".equals( childNode.getNodeName() ) )
            {
                teiClass = parseElementString( childNode.getTextContent() );
            }
            // if the node is a bodycontent element (JSP 1.1) or a body-content
            // element (JSP 1.2+), populate the bodyContent from the text string
            // of the node
            else if ( "bodycontent".equals( childNode.getNodeName() )
                    || "body-content".equals( childNode.getNodeName() ) )
            {
                bodyContent = parseElementString( childNode.getTextContent() );
            }
            // if the node is an info element, populate the info from the text
            // string of the node
            else if ( "info".equals( childNode.getNodeName() ) )
            {
                info = parseElementString( childNode.getTextContent() );
            }
            // if the node is an attribute element, parse the attribute node and
            // add it to the list of attributes
            else if ( "attribute".equals( childNode.getNodeName() ) )
            {
                TagAttributeInfo attribute = parseAttributeElement( childNode );
                attributes.add( attribute );
            }
            // if the node is a display-name element, populate the displayName
            // from the text string of the node
            else if ( "display-name".equals( childNode.getNodeName() ) )
            {
                displayName = parseElementString( childNode.getTextContent() );
            }
            // if the node is a small-icon element, populate the smallIcon from
            // the text string of the node
            else if ( "small-icon".equals( childNode.getNodeName() ) )
            {
                smallIcon = parseElementString( childNode.getTextContent() );
            }
            // if the node is a large-icon element, populate the largeIcon from
            // the text string of the node
            else if ( "large-icon".equals( childNode.getNodeName() ) )
            {
                largeIcon = parseElementString( childNode.getTextContent() );
            }
            // if the node is a description element, populate the description
            // from the text string of the node
            else if ( "description".equals( childNode.getNodeName() ) )
            {
                description = parseElementString( childNode.getTextContent() );
            }
            // if the node is a variable element, add the node to the list of
            // variables
            else if ( "variable".equals( childNode.getNodeName() ) )
            {
                TagVariableInfo variable = parseVariableElement( childNode );
                variables.add( variable );
            }
            // if the node is a example element, populate the example from the
            // text string of the node
            else if ( "example".equals( childNode.getNodeName() ) )
            {
                example = parseElementString( childNode.getTextContent() );
            }
            // if the node is a dynamic-attributes element, populate the
            // dynamicAttributes from the text string of the node
            else if ( "dynamic-attributes".equals( childNode.getNodeName() ) )
            {
                dynamicAttributes = parseTldBoolean( parseElementString( childNode
                        .getTextContent() ) );
            }
            else
            {
                LOGGER.fine( "Unknown element encountered" );
            }
        }

        // Verify the tag node had the required name element
        if ( name == null )
        {
            LOGGER.warning( "Tag element without required name"
                    + " element encountered" );
        }

        // Verify the tag node had the required tagclass/tag-class element
        if ( tagClass == null )
        {
            LOGGER.warning( "Tag element without required tagclass"
                    + " element encountered" );
        }

        TagInfo toReturn = new TagInfo( name, tagClass, bodyContent, info,
                tagLibraryInfo, null, (TagAttributeInfo[]) attributes
                        .toArray( new TagAttributeInfo[attributes.size()] ),
                displayName, smallIcon, largeIcon,
                (TagVariableInfo[]) variables
                        .toArray( new TagVariableInfo[variables.size()] ),
                dynamicAttributes );

        LOGGER.exiting( TagFileParser.class.getName(), "parseTagElement",
                toReturn );

        // TODO: have to do something for TagExtraInfo
        return toReturn;
    }

    /**
     * Returns an instance of the <code>TagAttributeInfo</code> class created
     * from the given tag attribute node
     * 
     * @param attribute
     *            the attribute node to parse
     * @return an instance of the <code>TagAttributeInfo</code> class created
     *         from the given tag attribute node
     */
    private static TagAttributeInfo parseAttributeElement( Node attribute )
    {
        LOGGER.entering( TagFileParser.class.getName(),
                "parseAttributeElement", attribute );

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
                LOGGER.fine( "Unknown element encountered: "
                        + childNode.getNodeName() );
            }
        }

        if ( name == null )
        {
            LOGGER.warning( "Attribute tag did not contain the required "
                    + "name element" );
        }

        TagAttributeInfo toReturn = new TagAttributeInfo( name, required, type,
                rtexprvalue );

        LOGGER.exiting( TagFileParser.class.getName(), "parseAttributeElement",
                toReturn );

        return toReturn;
    }

    /**
     * Parses the given validator node
     * 
     * @param validator
     *            the validator node to parse
     */
    private static void parseValidatorElement( Node validator )
    {
        LOGGER.entering( TagFileParser.class.getName(),
                "parseValidatorElement", validator );

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
                LOGGER.fine( "Unknown element encountered" );
            }
        }

        if ( validatorClass == null )
        {
            LOGGER.warning( "Validator tag did not contain the required "
                    + "validator-class element" );
        }

        // TODO: need to do something with the validator info

        LOGGER.exiting( TagFileParser.class.getName(), "parseValidatorElement" );
    }

    /**
     * Returns the listener from the given listener node
     * 
     * @param listener
     *            the listener node to parse
     * @return the listener from the given listener node
     */
    private static String parseListenerElement( Node listener )
    {
        LOGGER.entering( TagFileParser.class.getName(), "parseListenerElement",
                listener );

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
                LOGGER.fine( "Unknown element encountered" );
            }
        }

        if ( listenerClass == null )
        {
            LOGGER.warning( "Listener tag did not contain the required"
                    + " listener-class element" );
        }

        LOGGER.exiting( TagFileParser.class.getName(), "parseListenerElement",
                listenerClass );

        return listenerClass;
    }

    /**
     * Returns an instance of the <code>TagVariableInfo</code> class created
     * from the given variable node
     * 
     * @param variable
     *            the variable node to parse
     * @return an instance of the <code>TagVariableInfo</code> class created
     *         from the given variable node
     */
    private static TagVariableInfo parseVariableElement( Node variable )
    {

        LOGGER.entering( TagFileParser.class.getName(), "parseVariableElement",
                variable );

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
                LOGGER.fine( "Unknown element encountered" );
            }
        }

        if ( nameGiven == null && nameFromAttribute == null )
        {
            LOGGER.warning( "Variable tag did not contain either "
                    + "the name-given or name-from-attribute element" );
        }

        TagVariableInfo toReturn = new TagVariableInfo( nameGiven,
                nameFromAttribute, variableClass, declare,
                parseScopeType( scope ) );

        LOGGER.exiting( TagFileParser.class.getName(), "parseVariableElement",
                toReturn );

        return toReturn;
    }

    /**
     * Returns the init parameter from the given initParam node.
     * 
     * @param initParam
     *            the init parameter node to parse
     * @return the init parameter from the given initParam node
     */
    private static void parseInitParamElement( Node initParam )
    {
        LOGGER.entering( TagFileParser.class.getName(),
                "parseInitParamElement", initParam );

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
                LOGGER.fine( "Unknown element encountered" );
            }
        }

        if ( paramName == null )
        {
            LOGGER.warning( "Variable tag did not contain "
                    + "either the required param-name element" );
        }

        if ( paramValue == null )
        {
            LOGGER.warning( "Variable tag did not contain "
                    + "either the required param-value element" );
        }

        LOGGER.exiting( TagFileParser.class.getName(), "parseInitParamElement" );
    }

    /**
     * Returns a <code>String[]</code> parsed from the icon node. The first
     * element is the small icon, the second element is the large icon
     * 
     * @param icon
     *            the icon node
     * @return a <code>String[]</code> parsed from the icon node
     */
    private static String[] parseIconElement( Node icon )
    {
        LOGGER.entering( TagFileParser.class.getName(), "parseIconElement",
                icon );

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
                LOGGER.fine( "Unknown element encountered" );
            }
        }

        String[] toReturn = new String[]
            { smallIcon, largeIcon };
        LOGGER.exiting( TagFileParser.class.getName(), "parseIconElement",
                toReturn );

        return toReturn;
    }

    /**
     * Parses the given tag file node.
     * 
     * @param tagFile
     *            the tag file node to parse
     */
    private static void parseTagFileElement( Node tagFile )
    {
        LOGGER.entering( TagFileParser.class.getName(), "parseTagFileElement",
                tagFile );

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
                LOGGER.fine( "Unknown element encountered" );
            }
        }

        LOGGER.exiting( TagFileParser.class.getName(), "parseTagFileElement" );

    }

    /**
     * Parses the function node
     * 
     * @param function
     *            the function node
     */
    private static void parseFunctionElement( Node function )
    {
        LOGGER.entering( TagFileParser.class.getName(), "parseFunctionElement",
                function );

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
                LOGGER.fine( "Unknown element encountered" );
            }
        }

        LOGGER.exiting( TagFileParser.class.getName(), "parseFunctionElement" );
    }

    /**
     * Parse the tag lib extension node
     * 
     * @param tagLibExtension
     *            the tag lib extension node
     */
    private static void parseTagLibExtensionElement( Node tagLibExtension )
    {
        LOGGER.entering( TagFileParser.class.getName(),
                "parseTagLibExtensionElement", tagLibExtension );

        // TODO: parse the tag lib extension

        LOGGER.exiting( TagFileParser.class.getName(),
                "parseTagLibExtensionElement" );
    }

    /**
     * Parse the extension node
     * 
     * @param extension
     *            the extension node
     */
    private static void parseExtensionElement( Node extension )
    {
        LOGGER.entering( TagFileParser.class.getName(),
                "parseExtensionElement", extension );

        // TODO: parse the extension element

        LOGGER.exiting( TagFileParser.class.getName(), "parseExtensionElement" );

    }

    /**
     * Returns true if the given value is "true" or "yes"; false otherwise
     * 
     * @param value
     *            the value
     * @return true if the given value is "true" or "yes"; false otherwise
     */
    private static boolean parseTldBoolean( String value )
    {
        return ( "true".equals( value ) || "yes".equals( value ) );
    }

    /**
     * Returns the proper scope type corresponding to the given value
     * 
     * @param value
     *            the value
     * @return the proper scope type corresponding to the given value
     */
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

    /**
     * Returns the trimmed <code>String</code> of the given value or null if
     * the value is null
     * 
     * @param value
     *            the value
     * @return the trimmed <code>String</code> of the given value or null if
     *         the value is null
     */
    private static String parseElementString( String value )
    {
        return ( value == null ? null : value.trim() );
    }

    /**
     * Test code to confirm parsing of TLD files
     * 
     * @deprecated
     * @param args
     * @throws Exception
     */
    public static void main( String[] args ) throws Exception
    {
        File f = new File( "resources/html_basic.tld" );

        TagLibraryInfo tld = TagFileParser.loadTagFile( f );

        TagInfo[] tags = tld.getTags();

        for ( int tagIdx = 0; tagIdx < tags.length; tagIdx++ )
        {
            System.out.println( tags[tagIdx].getTagName() );
        }
    }
}
