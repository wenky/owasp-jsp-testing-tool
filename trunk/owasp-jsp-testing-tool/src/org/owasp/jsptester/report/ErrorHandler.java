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
package org.owasp.jsptester.report;

/**
 * Utility class to construct stack traces for the template error.jsp page.
 * 
 * @author Jason Li
 *
 */
public class ErrorHandler
{
    /**
     * Line separator string to use for HTML
     */
    private static final String HTML_NEW_LINE_CHAR = "<br>";

    /**
     * Line separator string to use for tooltips. Works in Mozilla Firefox.
     * Internet Explorer truncates tooltips and has no known newline string.
     */
    private static final String TOOLTIP_NEW_LINE_CHAR = "&#013;";

    /**
     * Returns a text representation of a throwable's stack trace formatted for
     * a tooltip
     * 
     * @param throwable
     *            the throwable to create the stack trace for
     * @return a text representation of a throwable's stack trace formatted for
     *         a tooltip
     */
    public static String buildTooltipStackTrace( Throwable throwable )
    {
        return buildStackTrace( throwable, TOOLTIP_NEW_LINE_CHAR );
    }

    /**
     * Returns a text representation of a throwable's stack trace formatted for
     * HTML
     * 
     * @param throwable
     *            the throwable to create the stack trace for
     * @return a text representation of a throwable's stack trace formatted for
     *         HTML
     */
    public static String buildHtmlStackTrace( Throwable throwable )
    {
        return buildStackTrace( throwable, HTML_NEW_LINE_CHAR );
    }

    /**
     * Returns a text representation of the stack trace for the given throwable
     * using the given newLineChar to separate lines
     * 
     * @param throwable
     *            the throwable to build the stack trace for
     * @param newLineChar
     *            the string to use to separate lines
     * @return a text representation of the stack trace for the given throwable
     *         using the given newLineChar to separate lines
     */
    private static String buildStackTrace( Throwable throwable,
            String newLineChar )
    {

        if ( throwable != null )
        {
            // Start with the throwable's top level message
            StringBuffer errorMessage = new StringBuffer(
                    htmlEntityEncode( throwable.getMessage() ) );

            // for each element in the stack trace, append the stack trace
            // message
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            for ( int traceIdx = 0; traceIdx < stackTrace.length; traceIdx++ )
            {
                errorMessage.append( newLineChar
                        + htmlEntityEncode( stackTrace[traceIdx].toString() ) );
            }

            return errorMessage.toString();
        }

        // default error message
        return "Unknown Error";
    }

    /**
     * A helper method for HTML entity-encoding a String value.
     * 
     * @param value
     *            A String containing HTML control characters.
     * @return An HTML-encoded String.
     */
    public static String htmlEntityEncode( String value )
    {

        StringBuffer buff = new StringBuffer();

        if ( value == null )
        {
            return null;
        }

        for ( int i = 0; i < value.length(); i++ )
        {

            char ch = value.charAt( i );

            if ( ch == '&' )
            {
                buff.append( "&amp;" );
            }
            else if ( ch == '<' )
            {
                buff.append( "&lt;" );
            }
            else if ( ch == '>' )
            {
                buff.append( "&gt;" );
            }
            else if ( Character.isWhitespace( ch ) )
            {
                buff.append( ch );
            }
            else if ( Character.isLetterOrDigit( ch ) )
            {
                buff.append( ch );
            }
            else if ( (int) ch >= 20 && (int) ch <= 126 )
            {
                buff.append( "&#" + (int) ch + ";" );
            }

        }

        return buff.toString();

    }
}
