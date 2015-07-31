/*
 * Copyright 2007-2011 Rickard Öberg.
 * Copyright 2007-2010 Niclas Hedhman.
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
import java.lang.reflect.Type;
import org.qi4j.api.association.Association;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.association.GenericAssociationInfo;
import org.qi4j.api.association.ManyAssociation;
import org.qi4j.api.association.NamedAssociation;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeInstance;
import org.qi4j.api.query.QueryExpressionException;
import org.qi4j.api.util.Classes;
import org.qi4j.functional.Function;

import static org.qi4j.api.util.Classes.typeOf;

/**
 * Function to get Entity Associations
 */
public class AssociationFunction<T>
    implements Function<Composite, Association<T>>
{
    private final AssociationFunction<?> traversedAssociation;
    private final ManyAssociationFunction<?> traversedManyAssociation;
    private final NamedAssociationFunction<?> traversedNamedAssociation;
    private final AccessibleObject accessor;

    public AssociationFunction( AssociationFunction<?> traversedAssociation,
                                ManyAssociationFunction<?> traversedManyAssociation,
                                NamedAssociationFunction<?> traversedNamedAssociation,
                                AccessibleObject accessor
    )
    {
        this.traversedAssociation = traversedAssociation;
        this.traversedManyAssociation = traversedManyAssociation;
        this.traversedNamedAssociation = traversedNamedAssociation;
        this.accessor = accessor;

        Type returnType = typeOf( accessor );
        if( !Association.class.isAssignableFrom( Classes.RAW_CLASS.map( returnType ) )
            && !ManyAssociation.class.isAssignableFrom( Classes.RAW_CLASS.map( returnType ) )
            && !NamedAssociation.class.isAssignableFrom( Classes.RAW_CLASS.map( returnType ) ) )
        {
            throw new QueryExpressionException( "Unsupported association type:" + returnType );
        }
        Type associationTypeAsType = GenericAssociationInfo.toAssociationType( returnType );
        if( !( associationTypeAsType instanceof Class ) )
        {
            throw new QueryExpressionException( "Unsupported association type:" + associationTypeAsType );
        }
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
    public Association<T> map( Composite entity )
    {
        try
        {
            Object target = entity;
            if( traversedAssociation != null )
            {
                Association<?> association = traversedAssociation.map( entity );
                if( association == null )
                {
                    return null;
                }
                target = association.get();
            }
            else if( traversedManyAssociation != null )
            {
                throw new IllegalArgumentException( "Cannot evaluate a ManyAssociation" );
            }
            else if( traversedNamedAssociation != null )
            {
                throw new IllegalArgumentException( "Cannot evaluate a NamedAssociation" );
            }

            if( target == null )
            {
                return null;
            }

            CompositeInstance handler = (CompositeInstance) Proxy.getInvocationHandler( target );
            return ( (AssociationStateHolder) handler.state() ).associationFor( accessor );
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
