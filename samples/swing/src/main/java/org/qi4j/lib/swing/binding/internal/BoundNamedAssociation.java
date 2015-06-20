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
import java.util.Map;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.lib.swing.binding.Binding;
import org.qi4j.lib.swing.binding.SwingAdapter;

public class BoundNamedAssociation<T>
    extends AbstractBinding<T>
    implements Binding, NamedAssociation<T>
{

    private NamedAssociation<T> actualAssociations;

    public BoundNamedAssociation( @Uses Method method, @Structure ObjectFactory objectBuilderFactory,
                                  @Service Iterable<SwingAdapter> allAdapters
    )
        throws IllegalArgumentException
    {
        super( method, objectBuilderFactory, allAdapters );
    }

    @Override
    protected boolean requiredCapabilitySatisfied( SwingAdapter.Capabilities capabilities )
    {
        return false;
    }

    @Override
    public int count()
    {
        return actualAssociations.count();
    }

    @Override
    public boolean containsName( String name )
    {
        return actualAssociations.containsName( name );
    }

    @Override
    public boolean put( String name, T entity )
    {
        return actualAssociations.put( name, entity );
    }

    @Override
    public boolean remove( String name )
    {
        return actualAssociations.remove( name );
    }

    @Override
    public T get( String name )
    {
        return actualAssociations.get( name );
    }

    @Override
    public String nameOf( T entity )
    {
        return actualAssociations.nameOf( entity );
    }

    @Override
    public Map<String, T> toMap()
    {
        return actualAssociations.toMap();
    }

    @Override
    public Iterable<EntityReference> references()
    {
        return actualAssociations.references();
    }

    @Override
    public EntityReference referenceOf( String name )
    {
        return actualAssociations.referenceOf( name );
    }

    @Override
    public Iterator<String> iterator()
    {
        return actualAssociations.iterator();
    }

}
