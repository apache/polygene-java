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

package org.apache.polygene.runtime.property;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.apache.polygene.api.common.MetaInfo;
import org.apache.polygene.api.common.QualifiedName;
import org.apache.polygene.api.constraint.ConstraintViolation;
import org.apache.polygene.api.constraint.ConstraintViolationException;
import org.apache.polygene.api.entity.Queryable;
import org.apache.polygene.api.property.DefaultValues;
import org.apache.polygene.api.property.GenericPropertyInfo;
import org.apache.polygene.api.property.InvalidPropertyTypeException;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.property.PropertyDescriptor;
import org.apache.polygene.api.serialization.Deserializer;
import org.apache.polygene.api.service.ServiceFinder;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.structure.ModuleDescriptor;
import org.apache.polygene.api.type.ValueCompositeType;
import org.apache.polygene.api.type.ValueType;
import org.apache.polygene.api.util.Classes;
import org.apache.polygene.api.util.Visitable;
import org.apache.polygene.api.util.Visitor;
import org.apache.polygene.bootstrap.BindingException;
import org.apache.polygene.runtime.composite.ValueConstraintsInstance;
import org.apache.polygene.runtime.model.Binder;
import org.apache.polygene.runtime.model.Resolution;
import org.apache.polygene.runtime.type.ValueTypeFactoryInstance;

/**
 * Model for a Property.
 * <p>
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

    private final InitialValueProvider initialValueProvider;

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
        this.accessor = accessor;
        type = GenericPropertyInfo.propertyTypeOf( accessor );
        checkTypeValidity( type );
        qualifiedName = QualifiedName.fromAccessor( accessor );
        initialValueProvider = new DefaultInitialValueProvider( useDefaults, initialValue );
        this.constraints = constraints;
        final Queryable queryable = accessor.getAnnotation( Queryable.class );
        this.queryable = queryable == null || queryable.value();
    }

    private void checkTypeValidity( Type type )
    {
        // Make sure certain data types doesn't take hold in Polygene applications.
        String typeName = type.getTypeName();
        if( typeName.contains( "java.util.Date" )
            || typeName.contains( "java.util.Calendar" )
            || typeName.contains( "java.util.GregorianCalendar" )
            || typeName.contains( "java.sql.Date" )
            || typeName.contains( "java.sql.Time" )
            || typeName.contains( "java.sql.Timestamp" )
            || typeName.contains( "org.joda.time" )
            )
        {
            throw new InvalidPropertyTypeException( type + " is not allowed in Polygene. Please use Java Time API instead." );
        }
        if( typeName.contains( "java.util.Dictionary" )
            || typeName.contains( "java.util.Hashtable" )
            || typeName.contains( "java.util.Vector" )
            )
        {
            throw new InvalidPropertyTypeException( type + " is not allowed in Polygene. Please use the modern Java Collection API instead." );
        }
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
    public Object resolveInitialValue( ModuleDescriptor moduleDescriptor )
    {
        return initialValueProvider.apply( moduleDescriptor.instance(), this );
    }

    @Override
    public void bind( Resolution resolution )
        throws BindingException
    {
        ValueTypeFactoryInstance factory = ValueTypeFactoryInstance.instance();
        Class<?> declaringClass = ( (Member) accessor() ).getDeclaringClass();
        Class<?> mainType = resolution.model().types().findFirst().orElse( null );
        valueType = factory.newValueType( type(), declaringClass, mainType, resolution.module() );
        builderInfo = new BuilderPropertyInfo();
        if( type instanceof TypeVariable )
        {
            type = Classes.resolveTypeVariable( (TypeVariable) type, declaringClass, mainType );
        }
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

    private interface InitialValueProvider extends BiFunction<Module, PropertyDescriptor, Object>
    {
    }

    private class DefaultInitialValueProvider
        implements InitialValueProvider
    {
        private final boolean useDefaults;
        private final Object initialValue;

        private DefaultInitialValueProvider( boolean useDefaults, Object initialValue )
        {
            this.useDefaults = useDefaults;
            this.initialValue = initialValue;
        }

        @Override
        public Object apply( Module module, PropertyDescriptor property )
        {
            return initialValue( module.descriptor(), initialValue, useDefaults );
        }

        private Object initialValue( ModuleDescriptor module, Object initialValue, boolean useDefaults )
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
                        value = module.instance().newValue( valueType.primaryType() );
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
                        ServiceFinder serviceFinder = module.instance().serviceFinder();
                        Deserializer deserializer = serviceFinder.findService( Deserializer.class ).get();
                        if( deserializer != null )
                        {
                            value = deserializer.deserialize( module, valueType, (String) value );
                        }
                    }
                }
            }
            return value;
        }
    }
}