/*
 * Copyright 2009 Georg Ragaller. All rights reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.tools.codesnippet.servlet;

import java.util.HashSet;
import java.util.Set;

/**
 * An enumeration for the supported response types.
 *
 * @see CodesnippetServlet
 */
public enum CodesnippetResponseType
{
    /**
     * Response type used for codesnippets which are rendered as a XHTML fragment (a {@literal <div>} containing the
     * snippet).
     */
    xml( "application/xml" ),
    /**
     * Response type used for codesnippets which are rendered as a XHTML document.
     */
    xhtml( "application/xhtml+xml" ),
    /**
     * Response type used for codesnippets which are rendered as plain text documents.
     */
    txt( "text/plain" );

    static final Set<String> validNames = new HashSet<String>();

    static
    {
        for( CodesnippetResponseType crt : values() )
        {
            validNames.add( crt.name() );
        }
    }

    private final String contentType;

    private CodesnippetResponseType( String contentType )
    {
        this.contentType = contentType;
    }

    /**
     * Gets the content type used for the response of a snippet URL with this suffix.
     *
     * @return the content type for this suffix
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * Checks if the argument is a valid enum constant name.
     *
     * @param name name of the enum constant
     *
     * @return {@code true} if the {@link #valueOf(String)} can safely be used with the argument string
     */
    public static boolean isValidName( String name )
    {
        return validNames.contains( name );
    }
}
