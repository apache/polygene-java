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
package org.apache.zest.api.query.grammar;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.function.Function;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.composite.CompositeInstance;
import org.apache.zest.api.property.GenericPropertyInfo;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.query.NotQueryableException;
import org.apache.zest.api.query.QueryExpressionException;
import org.apache.zest.api.util.Classes;

import static org.apache.zest.api.util.Classes.typeOf;

/**
 * Function to get Entity Properties.
 */
public class PropertyFunction<T>
    implements Function<Composite, Property<T>>
{
    private final PropertyFunction<?> traversedProperty;
    private final AssociationFunction<?> traversedAssociation;
    private final ManyAssociationFunction<?> traversedManyAssociation;
    private final NamedAssociationFunction<?> traversedNamedAssociation;
    private final AccessibleObject accessor;

    public PropertyFunction( PropertyFunction<?> traversedProperty,
                             AssociationFunction<?> traversedAssociation,
                             ManyAssociationFunction<?> traversedManyAssociation,
                             NamedAssociationFunction<?> traversedNamedAssociation,
                             AccessibleObject accessor
    )
    {
        this.traversedProperty = traversedProperty;
        this.traversedAssociation = traversedAssociation;
        this.traversedManyAssociation = traversedManyAssociation;
        this.traversedNamedAssociation = traversedNamedAssociation;
        this.accessor = accessor;

        // Verify that the property accessor is not marked as non queryable
        NotQueryableException.throwIfNotQueryable( accessor );
        // Verify that the property type itself (value composites) is not marked as non queryable

        Type returnType = typeOf( accessor );
        if( !Property.class.isAssignableFrom( Classes.RAW_CLASS.apply( returnType ) ) )
        {
            throw new QueryExpressionException( "Not a property type:" + returnType );
        }
        Type propertyTypeAsType = GenericPropertyInfo.toPropertyType( returnType );
        if( propertyTypeAsType instanceof ParameterizedType )
        {
            propertyTypeAsType = ( (ParameterizedType) propertyTypeAsType ).getRawType();
        }

        if( !( propertyTypeAsType instanceof Class ) )
        {
            throw new QueryExpressionException( "Unsupported property type:" + propertyTypeAsType );
        }
        @SuppressWarnings( "unchecked" )
        Class<T> type = (Class<T>) propertyTypeAsType;
        NotQueryableException.throwIfNotQueryable( type );
    }

    public PropertyFunction<?> traversedProperty()
    {
        return traversedProperty;
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
    public Property<T> apply( Composite entity )
    {
        try
        {
            Object target = entity;
            if( traversedProperty != null )
            {
                Property<?> property = traversedProperty.apply( entity );
                if( property == null )
                {
                    return null;
                }
                target = property.get();
            }
            else if( traversedAssociation != null )
            {
                Association<?> association = traversedAssociation.apply( entity );
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
            return handler.state().propertyFor( accessor );
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
        if( traversedProperty != null )
        {
            return traversedProperty.toString() + "." + ( (Member) accessor ).getName();
        }
        else if( traversedAssociation != null )
        {
            return traversedAssociation.toString() + "." + ( (Member) accessor ).getName();
        }
        else
        {
            return ( (Member) accessor ).getName();
        }
    }
}
