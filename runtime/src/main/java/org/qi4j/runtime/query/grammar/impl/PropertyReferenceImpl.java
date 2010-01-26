/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2009 Niclas Hedhman.
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
 *
 */
package org.qi4j.runtime.query.grammar.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.NotQueryableException;
import org.qi4j.api.query.QueryExpressionException;
import org.qi4j.api.query.grammar.AssociationReference;
import org.qi4j.api.query.grammar.PropertyReference;
import org.qi4j.api.util.Classes;
import org.qi4j.spi.composite.CompositeInstance;

/**
 * Default {@link org.qi4j.api.query.grammar.PropertyReference} implementation.
 */
public final class PropertyReferenceImpl<T>
    implements PropertyReference<T>
{

    /**
     * Property name.
     */
    private final String name;
    /**
     * Interface that declared the property.
     */
    private final Class<?> declaringType;
    /**
     * Property accessor method.
     */
    private final Method accessor;
    /**
     * Property type.
     */
    private final Class<T> type;
    /**
     * Traversed association.
     */
    private final AssociationReference traversedAssociation;
    /**
     * Traversed property.
     */
    private final PropertyReference traversedProperty;

    /**
     * Constructor.
     *
     * @param accessor             method that acts as property
     * @param traversedAssociation traversed association
     * @param traversedProperty    traversed property
     */
    @SuppressWarnings( "unchecked" )
    public PropertyReferenceImpl( final Method accessor,
                                  final AssociationReference traversedAssociation,
                                  final PropertyReference traversedProperty
    )
    {
        if( traversedAssociation != null
            && traversedProperty != null )
        {
            throw new IllegalArgumentException( "Only one of association or property can be set" );
        }
        this.accessor = accessor;
        name = accessor.getName();
        declaringType = accessor.getDeclaringClass();
        Type returnType = accessor.getGenericReturnType();
        if( !Property.class.isAssignableFrom( Classes.getRawClass( returnType ) ) )
        {
            throw new QueryExpressionException( "Not a property type:" + returnType );
        }
        Type propertyTypeAsType = GenericPropertyInfo.getPropertyType( returnType );
        if( !( propertyTypeAsType instanceof Class ) )
        {
            throw new QueryExpressionException( "Unsupported property type:" + propertyTypeAsType );
        }
        type = (Class<T>) propertyTypeAsType;

        // verify that the property accessor is not marked as non queryable
        NotQueryableException.throwIfNotQueryable( accessor );
        // verify that the property type itself (value composites) is not marked as non queryable
        NotQueryableException.throwIfNotQueryable( type );

        this.traversedAssociation = traversedAssociation;
        this.traversedProperty = traversedProperty;
    }

    /**
     * @see org.qi4j.api.query.grammar.PropertyReference#propertyName()
     */
    public String propertyName()
    {
        return name;
    }

    /**
     * @see PropertyReference#propertyDeclaringType()
     */
    public Class<?> propertyDeclaringType()
    {
        return declaringType;
    }

    /**
     * @see PropertyReference#propertyAccessor()
     */
    public Method propertyAccessor()
    {
        return accessor;
    }

    /**
     * @see org.qi4j.api.query.grammar.PropertyReference#propertyType()
     */
    public Class<T> propertyType()
    {
        return type;
    }

    /**
     * @see org.qi4j.api.query.grammar.PropertyReference#traversedAssociation()
     */
    public AssociationReference traversedAssociation()
    {
        return traversedAssociation;
    }

    /**
     * @see org.qi4j.api.query.grammar.PropertyReference#traversedProperty()
     */
    public PropertyReference traversedProperty()
    {
        return traversedProperty;
    }

    /**
     * @see org.qi4j.api.query.grammar.PropertyReference#eval(Object)
     */
    public Property<T> eval( final Object target )
    {
        Object actual = target;
        if( traversedAssociation() != null )
        {
            actual = traversedAssociation().eval( target );
        }
        else if( traversedProperty() != null )
        {
            actual = traversedProperty().eval( target );
        }
        if( actual != null )
        {
            try
            {
                CompositeInstance handler = (CompositeInstance) Proxy.getInvocationHandler( actual );
                return (Property) handler.invokeProxy( propertyAccessor(), new Object[0] );
            }
            catch( Throwable e )
            {
                return null;
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( traversedAssociation == null ? "" : traversedAssociation.toString() + "." )
            .append( traversedProperty == null ? "" : traversedProperty.toString() + "." )
            .append( declaringType.getSimpleName() )
            .append( ":" )
            .append( name )
            .toString();
    }
}