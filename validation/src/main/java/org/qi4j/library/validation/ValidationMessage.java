/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.validation;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class ValidationMessage
    implements Serializable
{
    private String resourceKey;
    private Object[] arguments;

    private String resourceBundle;
    private Severity severity;

    public ValidationMessage( String resourceKey, Severity severity )
    {
        this( resourceKey, null, severity );
    }

    public ValidationMessage( String resourceKey, String resourceBundle, Severity severity, Object... arguments )
    {
        this.resourceKey = resourceKey;
        this.arguments = arguments;
        this.resourceBundle = resourceBundle;
        this.severity = severity;
    }

    public String getResourceKey()
    {
        return resourceKey;
    }

    public Object[] getArguments()
    {
        return arguments;
    }

    public String getResourceBundle()
    {
        return resourceBundle;
    }

    public Severity getSeverity()
    {
        return severity;
    }

    public String getMessage()
    {
        return getMessage( Locale.getDefault() );
    }

    public String getMessage( Locale aLocale )
    {
        String resource;
        if( resourceBundle != null )
        {
            // Look up the message from a bundle
            ResourceBundle bundle = ResourceBundle.getBundle( resourceBundle, aLocale );
            resource = bundle.getString( resourceKey );
        }
        else
        {
            // Don't use i18n
            resource = resourceKey;
        }

        if( arguments == null )
        {
            return resource;
        }
        else
        {
            return MessageFormat.format( resource, arguments );
        }
    }


    @Override public String toString()
    {
        return getMessage();
    }

    public enum Severity
    {
        INFO, WARNING, ERROR
    }
}
