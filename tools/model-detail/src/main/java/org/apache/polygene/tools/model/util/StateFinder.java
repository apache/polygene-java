/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.tools.model.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.tools.model.descriptor.CompositeDetailDescriptor;
import org.apache.polygene.tools.model.descriptor.CompositeMethodDetailDescriptor;

public class StateFinder
{
    public List<CompositeMethodDetailDescriptor> findState( CompositeDetailDescriptor<?> descriptor )
    {
        return findState( descriptor.methods() );
    }

    private List<CompositeMethodDetailDescriptor> findState( Iterable<CompositeMethodDetailDescriptor> iter )
    {
        List<CompositeMethodDetailDescriptor> publicList = new ArrayList<>();
        List<CompositeMethodDetailDescriptor> privateList = new ArrayList<>();

        for( CompositeMethodDetailDescriptor descriptor : iter )
        {
            Class<?> compositeClass = descriptor.composite().descriptor().types().findFirst().orElse( null );
            Class<?> mixinMethodClass = descriptor.descriptor().method().getDeclaringClass();
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

        // filter Property, Association, ManyAssociation and NamedAssociation
        doFilter( publicList );

        return publicList;
    }

    /**
     * Do the filter for method return type (Property, Association, ManyAssociation, NamedAssociation)
     * by removing the entry from the list if not the above.
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
            if( Property.class.isAssignableFrom( method.getReturnType() )
                || Association.class.isAssignableFrom( method.getReturnType() )
                || ManyAssociation.class.isAssignableFrom( method.getReturnType() )
                || NamedAssociation.class.isAssignableFrom( method.getReturnType() ) )
            {
                continue;
            }
            iter.remove();
        }
    }
}
