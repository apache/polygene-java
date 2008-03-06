/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.property;

import org.qi4j.property.Property;
import org.qi4j.property.PropertyVetoException;

public class GenericImmutableProperty
    implements Property<String>
{
    private String value;
    private String name;
    private String qualifiedName;

    public GenericImmutableProperty( String name, String qualifiedName, String value )
    {
        this.value = value;
        this.name = name;
        this.qualifiedName = qualifiedName;
    }

    public String get()
    {
        return value;
    }

    public void set( String newValue ) throws PropertyVetoException
    {
        throw new PropertyVetoException( "Property '" + getQualifiedName() + "' is read-only" );
    }

    public <T> T getPropertyInfo( Class<T> infoType )
    {
        return null;
    }

    public String getName()
    {
        return name;
    }

    public String getQualifiedName()
    {
        return qualifiedName;
    }
}
