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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.DefaultValues;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.InvalidPropertyTypeException;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.ValueCompositeType;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.util.Classes;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.functional.Visitable;
import org.qi4j.functional.Visitor;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.types.ValueTypeFactory;

import static org.qi4j.functional.Iterables.empty;
import static org.qi4j.functional.Iterables.first;

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
    public Object initialValue( Module module )
    {
        // Use supplied value from assembly
        Object value = initialValue;

        // Check for @UseDefaults annotation
        if( value == null && useDefaults )
        {
            if( valueType instanceof ValueCompositeType )
            {
                return module.newValue( first( valueType().types() ) );
            }
            else
            {
                value = DefaultValues.getDefaultValueOf( type );
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
        Class<?> mainType = first( resolution.model().types() );
        valueType = factory.newValueType( type(), declaringClass, mainType, resolution.layer(), resolution.module() );

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
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( value );
            if( !violations.isEmpty() )
            {
                Iterable<Class<?>> empty = empty();
                throw new ConstraintViolationException( "<new instance>", empty, ( (Member) accessor ), violations );
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
                    Iterable<Class<?>> empty = empty();
                    throw new ConstraintViolationException( "<new instance>", empty, ( (Member) accessor ), violations );
                }
            }
        }
    }
}