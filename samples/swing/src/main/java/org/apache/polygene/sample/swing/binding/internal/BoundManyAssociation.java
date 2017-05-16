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
package org.apache.polygene.sample.swing.binding.internal;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.object.ObjectFactory;
import org.apache.polygene.sample.swing.binding.Binding;
import org.apache.polygene.sample.swing.binding.SwingAdapter;

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

    @Override
    public boolean clear()
    {
        return actualAssociations.clear();
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
    public Stream<EntityReference> references()
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
