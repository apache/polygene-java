/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.lib.swing.binding.internal;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.lib.swing.binding.Binding;
import org.qi4j.lib.swing.binding.SwingAdapter;

public class BoundManyAssociation<T> extends AbstractBinding<T>
    implements Binding, ManyAssociation<T>
{

    private ManyAssociation<T> actualAssociations;

    public BoundManyAssociation( @Uses Method method, @Structure ObjectFactory objectBuilderFactory,
                                 @Service Iterable<SwingAdapter> allAdapters
    )
        throws IllegalArgumentException
    {
        super( method, objectBuilderFactory, allAdapters );
    }

    protected boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities )
    {
        return false;
    }

    public boolean add( T o )
    {
        return actualAssociations.add( o );
    }

    public boolean remove( T o )
    {
        return actualAssociations.remove( o );
    }

    public T get( int index )
    {
        return actualAssociations.get( index );
    }

    @Override
    public List<T> toList()
    {
        return actualAssociations.toList();
    }

    @Override
    public Set<T> toSet()
    {
        return actualAssociations.toSet();
    }

    @Override
    public Iterable<EntityReference> references()
    {
        return actualAssociations.references();
    }

    @Override
    public int count()
    {
        return actualAssociations.count();
    }

    @Override
    public boolean contains( T entity )
    {
        return actualAssociations.contains( entity );
    }

    public boolean add( int index, T element )
    {
        return actualAssociations.add( index, element );
    }

    @Override
    public Iterator<T> iterator()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
