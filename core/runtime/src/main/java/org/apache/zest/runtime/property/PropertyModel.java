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

package org.apache.zest.runtime.property;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.stream.Stream;
import org.apache.zest.api.common.MetaInfo;
import org.apache.zest.api.common.QualifiedName;
import org.apache.zest.api.constraint.ConstraintViolation;
import org.apache.zest.api.constraint.ConstraintViolationException;
import org.apache.zest.api.entity.Queryable;
import org.apache.zest.api.property.DefaultValues;
import org.apache.zest.api.property.GenericPropertyInfo;
import org.apache.zest.api.property.InvalidPropertyTypeException;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.property.PropertyDescriptor;
import org.apache.zest.api.service.NoSuchServiceException;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.type.Serialization;
import org.apache.zest.api.type.ValueCompositeType;
import org.apache.zest.api.type.ValueType;
import org.apache.zest.api.util.Classes;
import org.apache.zest.api.value.MissingValueSerializationException;
import org.apache.zest.api.value.ValueDeserializer;
import org.apache.zest.api.util.Visitable;
import org.apache.zest.api.util.Visitor;
import org.apache.zest.bootstrap.BindingException;
import org.apache.zest.runtime.composite.ValueConstraintsInstance;
import org.apache.zest.runtime.model.Binder;
import org.apache.zest.runtime.model.Resolution;
import org.apache.zest.runtime.types.ValueTypeFactory;

/**
 * Model for a Property.
 *
 * <p>Equality is based on the Property accessor object (property type and name), not on the QualifiedName.</p>
 */
public class PropertyModel
    implements PropertyDescriptor, PropertyInfo, Binder, Visitable<PropertyModel>
{
    private Type type;

    private transient AccessibleObject accessor; // Interface accessor

    private final QualifiedName qualifiedName;

    private final ValueConstraintsInstance constraints; // May be null

    protected final MetaInfo metaInfo;

    private final Object initialValue;

    private final boolean useDefaults;

    private final boolean immutable;

    private ValueType valueType;

    protected PropertyInfo builderInfo;

    private final boolean queryable;

    public PropertyModel( AccessibleObject accessor,
                          boolean immutable,
                          boolean useDefaults,
                          ValueConstraintsInstance constraints,
                          MetaInfo metaInfo,
                          Object initialValue
    )
    {
        if( accessor instanceof Method )
        {
            Method m = (Method) accessor;
            if( !m.getReturnType().equals( Property.class ) )
            {
                throw new InvalidPropertyTypeException( accessor );
            }
        }
        this.immutable = immutable;
        this.metaInfo = metaInfo;
        type = GenericPropertyInfo.propertyTypeOf( accessor );
        this.accessor = accessor;
        qualifiedName = QualifiedName.fromAccessor( accessor );

        this.useDefaults = useDefaults;

        this.initialValue = initialValue;

        this.constraints = constraints;

        final Queryable queryable = accessor.getAnnotation( Queryable.class );
        this.queryable = queryable == null || queryable.value();
    }

    @Override
    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public String name()
    {
        return qualifiedName.name();
    }

    @Override
    public QualifiedName qualifiedName()
    {
        return qualifiedName;
    }

    @Override
    public Type type()
    {
        return type;
    }

    @Override
    public AccessibleObject accessor()
    {
        return accessor;
    }

    @Override
    public ValueType valueType()
    {
        return valueType;
    }

    @Override
    public boolean isImmutable()
    {
        return immutable;
    }

    public PropertyInfo getBuilderInfo()
    {
        return builderInfo;
    }

    @Override
    public boolean queryable()
    {
        return queryable;
    }

    @Override
    public Object initialValue( ModuleDescriptor module )
    {
        // Use supplied value from assembly
        Object value = initialValue;

        // Check for @UseDefaults annotation
        if( useDefaults )
        {
            if( value == null || ( ( value instanceof String ) && ( (String) value ).length() == 0 ) )
            {
                if( valueType instanceof ValueCompositeType )
                {
                    Class<?> propertyType = valueType().types().findFirst().orElse( null );
                    value = module.instance().newValue( propertyType );
                }
                else
                {
                    value = DefaultValues.getDefaultValueOf( type );
                }
            }
            else
            {
                Class<?> propertyType = valueType().types().findFirst().orElse( null );
                if( value instanceof String && !propertyType.equals( String.class ) )
                {
                    try
                    {
                        // here we could possibly deserialize json to other types...
                        ValueDeserializer deserializer = module.instance()
                            .serviceFinder()
                            .findService( ValueDeserializer.class )
                            .get();
                        if( deserializer != null )
                        {
                            value = deserializer.deserialize( module, propertyType ).apply( (String) value );
                        }
                    }
                    catch( NoSuchServiceException e )
                    {
                        throw new MissingValueSerializationException( "@UseDefaults with initialization value requires that there is a visible ValueDeserializer service available.", e);
                    }
                }
            }
        }
        return value;
    }

    @Override
    public void bind( Resolution resolution )
        throws BindingException
    {
        ValueTypeFactory factory = ValueTypeFactory.instance();
        Class<?> declaringClass = ( (Member) accessor() ).getDeclaringClass();
        Class<?> mainType = resolution.model().types().findFirst().orElse( null );
        Serialization.Variant variant = findVariant();
        valueType = factory.newValueType( type(), declaringClass, mainType, resolution.layer(), resolution.module(), variant );
        builderInfo = new BuilderPropertyInfo();
        if( type instanceof TypeVariable )
        {
            type = Classes.resolveTypeVariable( (TypeVariable) type, declaringClass, mainType );
        }
    }

    private Serialization.Variant findVariant()
    {
        Serialization serialization = metaInfo.get( Serialization.class );
        Serialization.Variant variant = null;
        if( serialization != null )
        {
            variant = serialization.value();
        }
        if( variant == null )
        {
            variant = Serialization.Variant.entry;
        }
        return variant;
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super PropertyModel, ThrowableType> visitor )
        throws ThrowableType
    {
        return visitor.visit( this );
    }

    @Override
    public void checkConstraints( Object value )
        throws ConstraintViolationException
    {
        if( constraints == null )
        {
            return;
        }
        constraints.checkConstraints( value, accessor );
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
        return accessor.equals( that.accessor );
    }

    @Override
    public int hashCode()
    {
        return accessor.hashCode();
    }

    @Override
    public String toString()
    {
        if( accessor instanceof Field )
        {
            return ( (Field) accessor ).toGenericString();
        }
        else
        {
            return ( (Method) accessor ).toGenericString();
        }
    }

    private class BuilderPropertyInfo implements PropertyInfo
    {
        @Override
        public boolean isImmutable()
        {
            return false;
        }

        @Override
        public QualifiedName qualifiedName()
        {
            return qualifiedName;
        }

        @Override
        public Type type()
        {
            return type;
        }

        @Override
        public void checkConstraints( Object value )
            throws ConstraintViolationException
        {
            if( constraints != null )
            {
                List<ConstraintViolation> violations = constraints.checkConstraints( value );
                if( !violations.isEmpty() )
                {
                    Stream<Class<?>> empty = Stream.empty();
                    throw new ConstraintViolationException( "", empty, (Member) accessor, violations );
                }
            }
        }
    }
}