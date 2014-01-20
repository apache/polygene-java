/*
 * Copyright (c) 2011-2012, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
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
package org.qi4j.api.association;

import java.util.Iterator;
import java.util.Map;

/**
 * If you want to catch calls to NamedAssociations, then create a GenericConcern
 * that wraps the Qi4j-supplied NamedAssociations instance with NamedAssociationsWrapper. Override
 * methods to perform your custom code.
 */
public class NamedAssociationWrapper
    implements NamedAssociation<Object>
{
    protected NamedAssociation<Object> next;

    public NamedAssociationWrapper( NamedAssociation<Object> next )
    {
        this.next = next;
    }

    public NamedAssociation<Object> next()
    {
        return next;
    }

    @Override
    public Iterator<String> iterator()
    {
        return next.iterator();
    }

    @Override
    public int count()
    {
        return next.count();
    }

    @Override
    public boolean containsName( String name )
    {
        return next.containsName( name );
    }

    @Override
    public boolean put( String name, Object entity )
    {
        return next.put( name, entity );
    }

    @Override
    public boolean remove( String name )
    {
        return next.remove( name );
    }

    @Override
    public Object get( String name )
    {
        return next.get( name );
    }

    @Override
    public String nameOf( Object entity )
    {
        return next.nameOf( entity );
    }

    @Override
    public Map<String, Object> toMap()
    {
        return next.toMap();
    }

    @Override
    public int hashCode()
    {
        return next.hashCode();
    }

    @Override
    public boolean equals( Object obj )
    {
        return next.equals( obj );
    }

    @Override
    public String toString()
    {
        return next.toString();
    }

}
