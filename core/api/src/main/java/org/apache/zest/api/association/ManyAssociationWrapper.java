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
package org.apache.zest.api.association;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.zest.api.entity.EntityReference;

/**
 * If you want to catch calls to ManyAssociations, then create a GenericConcern
 * that wraps the Zest-supplied ManyAssociation instance with ManyAssociationWrappers. Override
 * methods to perform your custom code.
 */
public class ManyAssociationWrapper
    implements ManyAssociation<Object>
{
    protected ManyAssociation<Object> next;

    public ManyAssociationWrapper( ManyAssociation<Object> next )
    {
        this.next = next;
    }

    public ManyAssociation<Object> next()
    {
        return next;
    }

    @Override
    public int count()
    {
        return next.count();
    }

    @Override
    public boolean contains( Object entity )
    {
        return next.contains( entity );
    }

    @Override
    public boolean add( int i, Object entity )
    {
        return next.add( i, entity );
    }

    @Override
    public boolean add( Object entity )
    {
        return next.add( entity );
    }

    @Override
    public boolean remove( Object entity )
    {
        return next.remove( entity );
    }

    @Override
    public Object get( int i )
    {
        return next.get( i );
    }

    @Override
    public List<Object> toList()
    {
        return next.toList();
    }

    @Override
    public Set<Object> toSet()
    {
        return next.toSet();
    }

    @Override
    public Iterable<EntityReference> references()
    {
        return next.references();
    }

    @Override
    public Iterator<Object> iterator()
    {
        return next.iterator();
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
