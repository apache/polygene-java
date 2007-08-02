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
package org.qi4j.api.validation;

public class ValidationMessage
{
    private String resourceKey;
    private Object[] arguments;

    private String source;
    private Severity severity;

    public ValidationMessage( String resourceKey, Severity severity )
    {
        this( resourceKey, null, null, severity );
    }

    public ValidationMessage( String resourceKey, String source, Severity severity )
    {
        this( resourceKey, null, source, severity );
    }

    public ValidationMessage( String resourceKey, Object[] arguments, String source, Severity severity )
    {
        this.resourceKey = resourceKey;
        this.arguments = arguments;
        this.source = source;
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

    public String getSource()
    {
        return source;
    }

    public Severity getSeverity()
    {
        return severity;
    }

    enum Severity
    {
        INFO, WARNING, ERROR
    }
}
