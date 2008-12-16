/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.property;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.Immutable;
import org.qi4j.api.property.Computed;
import org.qi4j.api.entity.RDF;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.api.common.MetaInfo;

/**
 * TODO
 */
public class PropertyModel
    implements Serializable, PropertyDescriptor, ConstraintsCheck
{
    private static final long serialVersionUID = 1L;

    private final String name;
    private final Type type;
    private final Method accessor; // Interface accessor
    private final String qualifiedName;
    private final String uri;
    private final String rdf;

    private final ValueConstraintsInstance constraints; // May be null
    private final MetaInfo metaInfo;
    private final Object defaultValue;
    private final boolean immutable;
    private final boolean computed;

    private PropertyInfo builderInfo;
    private PropertyInfo propertyInfo;

    public PropertyModel(
        Method anAccessor, ValueConstraintsInstance constraints, MetaInfo aMetaInfo, Object aPropertyDefaultValue )
    {
        metaInfo = aMetaInfo;
        name = anAccessor.getName();
        type = GenericPropertyInfo.getPropertyType( anAccessor );
        accessor = anAccessor;
        qualifiedName = GenericPropertyInfo.getQualifiedName( anAccessor );
        defaultValue = aPropertyDefaultValue;

        uri = GenericPropertyInfo.toURI( qualifiedName() );
        RDF uriAnnotation = metaInfo.get( RDF.class );
        rdf = uriAnnotation == null ? null : uriAnnotation.value();

        this.constraints = constraints;
        
        immutable = metaInfo.get( Immutable.class ) != null;
        computed = metaInfo.get( Computed.class ) != null;

        builderInfo = new GenericPropertyInfo(metaInfo, false, computed, name, qualifiedName,  type);
        propertyInfo = new GenericPropertyInfo(metaInfo, immutable, computed, name, qualifiedName,  type);
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public String name()
    {
        return name;
    }

    public String qualifiedName()
    {
        return qualifiedName;
    }

    public Type type()
    {
        return type;
    }

    public Method accessor()
    {
        return accessor;
    }

    public boolean isImmutable()
    {
        return immutable;
    }

    public boolean isComputed()
    {
        return computed;
    }

    public Object defaultValue()
    {
        return defaultValue;
    }

    public String toURI()
    {
        return uri;
    }

    public String toRDF()
    {
        return rdf;
    }

    public Property<?> newBuilderInstance()
    {
        // Properties cannot be immutable during construction
        
        Property<?> property;
        if( computed )
        {
            property = new ComputedPropertyInfo<Object>( builderInfo );
        }
        else
        {
            property = new PropertyInstance<Object>( builderInfo, defaultValue(), this );
        }

        if (!accessor.getReturnType().equals(Property.class))
        {
            // Create proxy
            property = (Property<?>) Proxy.newProxyInstance( accessor.getReturnType().getClassLoader(), new Class[] {accessor.getReturnType()}, new PropertyHandler(property) );
        }

        return property;
    }

    public Property<?> newDefaultInstance()
    {
        // Construct instance without using a builder

        return newInstance( defaultValue() );
    }

    @SuppressWarnings( "unchecked" )
    public Property<?> newInstance( Object value )
    {
        // Property was constructed using a builder

        Property property;
        if( computed )
        {
            property = new ComputedPropertyInfo<Object>( propertyInfo );
        }
        else
        {
            property = new PropertyInstance<Object>( propertyInfo, value, this );
        }
        return property;
    }

    public void checkConstraints( Object value )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( value );
            if( !violations.isEmpty() )
            {
                throw new ConstraintViolationException( accessor, violations );
            }
        }
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        PropertyModel that = (PropertyModel) o;

        if( !accessor.equals( that.accessor ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return accessor.hashCode();
    }

    @Override
    public String toString()
    {
        return accessor.toGenericString();
    }

    private static class ComputedPropertyInfo<T>
        extends ComputedPropertyInstance<T>
    {
        private ComputedPropertyInfo( PropertyInfo aPropertyInfo )
            throws IllegalArgumentException
        {
            super( aPropertyInfo );
        }

        public T get()
        {
            throw new IllegalStateException( "Property [" + name() + "] must be computed" );
        }
    }

    static class PropertyHandler
        implements InvocationHandler
    {
        Property p;

        public PropertyHandler( Property<?> property )
        {
            p = property;
        }

        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            try
            {
                return method.invoke( p, args );
            }
            catch( InvocationTargetException e )
            {
                throw e;
            }
        }
    }
}
