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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AttackLibrary
{

    private static final String DEFAULT_ATTACK_FILE = "resources/attacks.xml";

    private final Map attacks = new HashMap();

    private AttackLibrary()
    {
    }

    public static AttackLibrary getInstance()
    {
        return getInstance( DEFAULT_ATTACK_FILE );
    }

    public static AttackLibrary getInstance( String attackFile )
    {

        return parseAttackFile( attackFile );
    }

    public Attack[] getAttacks()
    {
        Collection values = attacks.values();
        return (Attack[]) values.toArray( new Attack[values.size()] );
    }

    public String getAttackString( String name )
    {
        return ( (Attack) attacks.get( name ) ).getAttackString();
    }

    private static AttackLibrary parseAttackFile( String attackFile )
    {
        AttackLibrary toReturn = new AttackLibrary();

        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;

            db = dbf.newDocumentBuilder();

            Document dom = null;

            /*
             * Load and parse the file.
             */
            dom = db.parse( attackFile );

            Element attackNodes = dom.getDocumentElement();

            NodeList nodes = attackNodes.getChildNodes();

            for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
            {
                Node childNode = nodes.item( nodeIdx );

                if ( childNode.getNodeType() != Node.ELEMENT_NODE )
                {
                    continue;
                }

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
        catch ( ParserConfigurationException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( SAXException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return toReturn;
    }

    private static Attack parseAttackNode( Node attack ) throws SAXException
    {
        NodeList nodes = attack.getChildNodes();

        String name = null;
        String displayName = null;
        String attackString = null;

        for ( int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++ )
        {
            Node childNode = nodes.item( nodeIdx );

            if ( childNode.getNodeType() != Node.ELEMENT_NODE )
            {
                continue;
            }

            if ( "name".equals( childNode.getNodeName() ) )
            {
                name = childNode.getTextContent();
            }
            else if ( "display-name".equals( childNode.getNodeName() ) )
            {
                displayName = childNode.getTextContent();
            }
            else if ( "attack-string".equals( childNode.getNodeName() ) )
            {
                attackString = childNode.getTextContent();
            }
            else
            {
                throw new SAXException( "Unknown element encounterd" );
            }
        }

        if ( name == null )
        {
            throw new SAXException( "attack node missing required name" );
        }

        if ( displayName == null )
        {
            throw new SAXException( "attack node missing required display-name" );
        }

        if ( attackString == null )
        {
            throw new SAXException(
                    "attack node missing required attack-string" );
        }

        return new Attack( name, displayName, attackString );
    }

    public static void main( String[] args )
    {
        AttackLibrary library = AttackLibrary.getInstance();
        System.out.println( Arrays.toString( library.getAttacks() ) );
    }
}
