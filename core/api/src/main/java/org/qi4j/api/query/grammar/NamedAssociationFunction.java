/*
 * Copyright 2011-2012 Niclas Hedhman.
 * Copyright 2014 Paul Merlin.
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
 * ied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.api.query.grammar;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Proxy;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.functional.Function;

/**
 * Function to get Entity NamedAssociations.
 */
public class NamedAssociationFunction<T>
    implements Function<Composite, NamedAssociation<T>>
{
    private final AssociationFunction<?> traversedAssociation;
    private final ManyAssociationFunction<?> traversedManyAssociation;
    private final NamedAssociationFunction<?> traversedNamedAssociation;
    private final AccessibleObject accessor;

    public NamedAssociationFunction( AssociationFunction<?> traversedAssociation,
                                     ManyAssociationFunction<?> traversedManyAssociation,
                                     NamedAssociationFunction<?> traversedNamedAssociation,
                                     AccessibleObject accessor
    )
    {
        this.traversedAssociation = traversedAssociation;
        this.traversedManyAssociation = traversedManyAssociation;
        this.traversedNamedAssociation = traversedNamedAssociation;
        this.accessor = accessor;
    }

    public AssociationFunction<?> traversedAssociation()
    {
        return traversedAssociation;
    }

    public ManyAssociationFunction<?> traversedManyAssociation()
    {
        return traversedManyAssociation;
    }

    public NamedAssociationFunction<?> traversedNamedAssociation()
    {
        return traversedNamedAssociation;
    }

    public AccessibleObject accessor()
    {
        return accessor;
    }

    @Override
    public NamedAssociation<T> map( Composite entity )
    {
        try
        {
            Object target = entity;
            if( traversedAssociation != null )
            {
                target = traversedAssociation.map( entity ).get();
            }
            if( traversedManyAssociation != null )
            {
                throw new IllegalArgumentException( "Cannot traverse ManyAssociations" );
            }
            if( traversedNamedAssociation != null )
            {
                throw new IllegalArgumentException( "Cannot traverse NamedAssociations" );
            }

            CompositeInstance handler = (CompositeInstance) Proxy.getInvocationHandler( target );
            return ( (AssociationStateHolder) handler.state() ).namedAssociationFor( accessor );
        }
        catch( IllegalArgumentException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    @Override
    public String toString()
    {
        if( traversedAssociation != null )
        {
            return traversedAssociation.toString() + "." + ( (Member) accessor ).getName();
        }
        else
        {
            return ( (Member) accessor ).getName();
        }
    }
}
