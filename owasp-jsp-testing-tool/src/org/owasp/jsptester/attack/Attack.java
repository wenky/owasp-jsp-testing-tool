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

/**
 * This class encapsulates the details of an XSS attack test case.
 * 
 * @author Jason Li
 */
public class Attack
{
    /**
     * The name of the attack (name must be safe for file names)
     */
    private final String name;

    /**
     * Display name for the attack
     */
    private final String displayName;

    /**
     * The actual attack string
     */
    private final String attackString;

    /**
     * Constructs an Attack test case using the given parameters.
     * 
     * @param name
     *            the name (file name safe) of the attack
     * @param displayName
     *            the display name for the attack
     * @param attackString
     *            the actual attack string
     */
    public Attack( String name, String displayName, String attackString )
    {
        this.name = name;
        this.displayName = displayName;
        this.attackString = attackString;
    }

    /**
     * Returns the name (file name safe) of the attack
     * 
     * @return the name (file name safe) of the attack
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the attack string
     * 
     * @return the attack string
     */
    public String getAttackString()
    {
        return attackString;
    }

    /**
     * Returns the display name of the attack
     * 
     * @return the display name of the attack
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[name: " + name + ", displayName: " + displayName
                + ", attackString: " + attackString + "]";
    }
}
