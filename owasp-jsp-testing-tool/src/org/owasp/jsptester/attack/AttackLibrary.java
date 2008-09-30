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
package org.owasp.jsptester.attack;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.owasp.jsptester.conf.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Loads the set of XSS attack test cases from an XML filed to be used to test a
 * tag library as instances of the Attack class.
 * 
 * @author Jason Li
 * 
 */
public class AttackLibrary
{
    /**
     * Map<String, Attack> of name of attack to Attack instance
     */
    private final Map attacks = new HashMap();

    /**
     * Constructs an instance of an AttackLibrary
     */
    private AttackLibrary()
    {
    }

    /**
     * Returns an instance of an AttackLibrary created using the default attack
     * XML file
     * 
     * @return an instance of an AttackLibrary created using the default attack
     *         XML file
     */
    public static AttackLibrary getInstance()
    {
        return getInstance( Configuration.getInstance().getProperty( Configuration.ATTACK_LIBRARY ) );
    }

    /**
     * Returns an instance of an AttackLibrary created using the specified
     * attack XML file
     * 
     * @param attackFile
     *            the attack XML file to load
     * @return an instance of an AttackLibrary created using the specified
     *         attack XML file
     */
    public static AttackLibrary getInstance( String attackFile )
    {

        return parseAttackFile( attackFile );
    }

    /**
     * Returns an array of all the Attack instances in this AttackLibrary
     * 
     * @return an array of all the Attack instances in this AttackLibrary
     */
    public Attack[] getAttacks()
    {
        Collection values = attacks.values();
        return (Attack[]) values.toArray( new Attack[values.size()] );
    }

    /**
     * Returns the attack string from the Attack instance with the given name
     * 
     * @param name
     *            the name of the attack
     * @return the attack string from the Attack instance with the given name
     */
    public String getAttackString( String name )
    {
        Attack attack = (Attack) attacks.get( name );
        if ( attack != null )
        {
            return attack.getAttackString();
        }

        return null;
    }

    /**
     * Returns an instance of AttackLibrary created from the specified file.
     * This method parses the specified XML file and creates instances of the
     * Attack class to populate the Map of attacks
     * 
     * @param attackFile
     *            the file to load
     * @return an instance of AttackLibrary created from the specified file.
     */
    private static AttackLibrary parseAttackFile( String attackFile )
    {
        // Create a new instance of the attack library
        AttackLibrary toReturn = new AttackLibrary();

        try
        {
            // Create a document parser
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;

            db = dbf.newDocumentBuilder();

            Document dom = null;

            // Load and parse the file.
            dom = db.parse( attackFile );

            // Get the root element
            Element attackNodes = dom.getDocumentElement();

            // Get the root's children nodes
            NodeList nodes = attackNodes.getChildNodes();

            // Iterate through each of the child nodes
            for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
            {
                Node childNode = nodes.item( nodeIdx );

                // if the node is not an element, skip to the next node
                if ( childNode.getNodeType() != Node.ELEMENT_NODE )
                {
                    continue;
                }

                // if the element is an attack element, parse the element
                // and place the corresponding attack in the map
                if ( "attack".equals( childNode.getNodeName() ) )
                {
                    Attack attack = parseAttackNode( childNode );
                    toReturn.attacks.put( attack.getName(), attack );
                }
                else
                {
                    throw new SAXException( "Unknown element encounterd" );
                }
            }
        }
        catch ( ParserConfigurationException pce )
        {
            // TODO: proper error handling
            pce.printStackTrace();
        }
        catch ( SAXException se )
        {
            // TODO: proper error handling
            se.printStackTrace();
        }
        catch ( IOException ioe )
        {
            // TODO: proper error handling
            ioe.printStackTrace();
        }

        // return the created instance of the attack library
        return toReturn;
    }

    /**
     * Returns an instance of the Attack class created by parsing the given
     * attack element node from an attack XML file
     * 
     * @param attack
     *            the XML node containing the attack element
     * @return an instance of the Attack class created by parsing the given
     *         attack element node from an attack XML file
     * @throws SAXException
     *             if an XML parsing error occurs
     */
    private static Attack parseAttackNode( Node attack ) throws SAXException
    {
        // Get the attack's child nodes
        NodeList nodes = attack.getChildNodes();

        String name = null;
        String displayName = null;
        String attackString = null;

        // iterate through all child nodes looking for the name, display-name,
        // and attack-string elements
        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            // if the node is not an element, skip to the next node
            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            // if the node is the name element, get the text value from the node
            if ( "name".equals( childNode.getNodeName() ) )
            {
                name = childNode.getTextContent();
            }
            // if the node is the display-name element, get the text value from
            // the node
            else if ( "display-name".equals( childNode.getNodeName() ) )
            {
                displayName = childNode.getTextContent();
            }
            // if the node is the attack-string element, get the text value from
            // the node
            else if ( "attack-string".equals( childNode.getNodeName() ) )
            {
                attackString = childNode.getTextContent();
            }
            else
            {
                throw new SAXException( "Unknown element encounterd" );
            }
        }

        // Verify that the attack element contained a name element
        if ( name == null )
        {
            throw new SAXException( "attack node missing required name" );
        }

        // Verify that the attack element contained a display-name element
        if ( displayName == null )
        {
            throw new SAXException( "attack node missing required display-name" );
        }

        // Verify that the attack element contained an attack-string element
        if ( attackString == null )
        {
            throw new SAXException(
                    "attack node missing required attack-string" );
        }

        // return a new Attack instance from the parsed parameters
        return new Attack( name, displayName, attackString );
    }

    /**
     * Test code to verify correct parsing of an attack XML file
     * 
     * @param args
     * @deprecated
     */
    public static void main( String[] args )
    {
        AttackLibrary library = AttackLibrary.getInstance();
        System.out.println( Arrays.toString( library.getAttacks() ) );
    }
}
