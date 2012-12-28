/**
 *
 * Copyright 2009-2011 Rickard Öberg AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.rest.server.api;

import java.util.ArrayList;
import java.util.List;
import org.qi4j.functional.Iterables;
import org.restlet.Request;

/**
 * Manage the current object selection. An instance of ObjectSelection
 * is created for each request, and stored as an attribute.
 * Whenever an object is identified in the chain, add it to the selection.
 */
public class ObjectSelection
{
    public static ObjectSelection current()
    {
        return (ObjectSelection) Request.getCurrent().getAttributes().get( "selection" );
    }

    public static <T> T type( Class<T> type )
    {
        ObjectSelection selection = current();
        if( selection == null )
        {
            throw new IllegalStateException( "No current selection" );
        }

        return selection.get( type );
    }

    public static void newSelection()
    {
        Request.getCurrent().getAttributes().put( "selection", new ObjectSelection() );
    }

    private List<Object> selection = new ArrayList<Object>();

    /**
     * Create new root roleMap
     */
    public ObjectSelection()
    {
    }

    public void select( Object object )
    {
        selection.add( 0, object );
    }

    public <T> T get( Class<T> type )
        throws IllegalArgumentException
    {
        for( Object object : selection )
        {
            if( type.isInstance( object ) )
            {
                return (T) object;
            }
        }
        throw new IllegalArgumentException( "No object in selection for type:" + type.getSimpleName() );
    }

    public Iterable<Object> selection()
    {
        return selection;
    }

    public Object[] toArray()
    {
        return Iterables.toArray( Object.class, selection );
    }
}
