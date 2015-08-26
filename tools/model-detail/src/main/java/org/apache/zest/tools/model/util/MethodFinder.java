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
package org.apache.zest.tools.model.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.composite.CompositeDescriptor;
import org.apache.zest.api.property.Property;
import org.apache.zest.tools.model.descriptor.CompositeDetailDescriptor;
import org.apache.zest.tools.model.descriptor.CompositeMethodDetailDescriptor;

public class MethodFinder
{
    public List<CompositeMethodDetailDescriptor> findMethod( CompositeDetailDescriptor<?> descriptor )
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
        List<CompositeMethodDetailDescriptor> publicList = new ArrayList<>();
        List<CompositeMethodDetailDescriptor> privateList = new ArrayList<>();

        for( CompositeMethodDetailDescriptor descriptor : iter )
        {
            CompositeDescriptor compositeDescriptor = descriptor.composite().descriptor();
            Class<?> mixinMethodClass = descriptor.descriptor().method().getDeclaringClass();
            compositeDescriptor.types().forEach( mixinType ->
            {
                if( mixinMethodClass.isAssignableFrom( mixinType ) )
                {
                    publicList.add( descriptor );
                }
                else
                {
                    privateList.add( descriptor );
                }
            } );
        }

        // combine into one list, with public listed first then private
        publicList.addAll( privateList );

        // filter Property, Association, ManyAssociation and NamedAssociation
        doFilter( publicList );

        return publicList;
    }

    /**
     * Do the filter for method return type (Property, Association, ManyAssociation, NamedAssociation)
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
            else if( NamedAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                iter.remove();
            }
        }
    }
}
