/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.tools.model.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.composite.CompositeDescriptor;
import org.qi4j.api.property.Property;
import org.qi4j.tools.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.tools.model.descriptor.CompositeMethodDetailDescriptor;

public class MethodFinder
{
    public List<CompositeMethodDetailDescriptor> findMethod( CompositeDetailDescriptor descriptor )
    {
        return findMethod( descriptor.methods() );
    }

    /**
     * The Methods tab should show all the methods of all Mixins (private and public separated)
     * that don't return one of Property, Association or ManyAssociation.
     *
     * "private" and "public" refers to if the interface they are declared in is extended by the Composite.
     * If yes, then it is a public method, meaning, clients can call it.
     * If no, then it is a private mixin type, and can only be used internally through @This injections.
     */
    private List<CompositeMethodDetailDescriptor> findMethod( Iterable<CompositeMethodDetailDescriptor> iter )
    {
        List<CompositeMethodDetailDescriptor> publicList = new ArrayList<CompositeMethodDetailDescriptor>();
        List<CompositeMethodDetailDescriptor> privateList = new ArrayList<CompositeMethodDetailDescriptor>();

        for( CompositeMethodDetailDescriptor descriptor : iter )
        {
            CompositeDescriptor compositeDescriptor = descriptor.composite().descriptor();
            Iterable<Class<?>> compositeType = compositeDescriptor.types();
            Class mixinMethodClass = descriptor.descriptor().method().getDeclaringClass();
            for( Class<?> compositeClass : compositeType )
            if( mixinMethodClass.isAssignableFrom( compositeClass ) )
            {
                publicList.add( descriptor );
            }
            else
            {
                privateList.add( descriptor );
            }
        }

        // combine into one list, with public listed first then private
        publicList.addAll( privateList );

        // filter Property, Association, and ManyAssociation
        doFilter( publicList );

        return publicList;
    }

    /**
     * Do the filter for method return type (Property, Association, ManyAssociation)
     * by removing the entry from the list.
     *
     * @param list list of CompositeMethodDetailDescriptor
     */
    private void doFilter( List<CompositeMethodDetailDescriptor> list )
    {
        if( list.isEmpty() )
        {
            return;
        }

        Iterator<CompositeMethodDetailDescriptor> iter = list.iterator();
        while( iter.hasNext() )
        {
            CompositeMethodDetailDescriptor descriptor = iter.next();
            Method method = descriptor.descriptor().method();
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                iter.remove();
            }
            else if( Association.class.isAssignableFrom( method.getReturnType() ) )
            {
                iter.remove();
            }
            else if( ManyAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                iter.remove();
            }
        }
    }
}
