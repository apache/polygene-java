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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.DefaultValues;
import org.qi4j.api.property.GenericPropertyInfo;
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

import java.lang.reflect.*;
import java.util.List;

import static org.qi4j.api.util.Classes.RAW_CLASS;
import static org.qi4j.api.util.Classes.TYPE_OF;

/**
 * JAVADOC
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

    public PropertyModel( AccessibleObject accessor, boolean immutable, boolean useDefaults, ValueConstraintsInstance constraints,
                          MetaInfo metaInfo, Object initialValue
    )
    {
        this.immutable = immutable;
        this.metaInfo = metaInfo;
        type = GenericPropertyInfo.getPropertyType( accessor );
        this.accessor = accessor;
        qualifiedName = QualifiedName.fromAccessor( accessor );

        this.useDefaults = useDefaults;

        this.initialValue = initialValue;

        this.constraints = constraints;

        final Queryable queryable = accessor.getAnnotation( Queryable.class );
        this.queryable = queryable == null || queryable.value();
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public String name()
    {
        return qualifiedName.name();
    }

    public QualifiedName qualifiedName()
    {
        return qualifiedName;
    }

    public Type type()
    {
        return type;
    }

    public AccessibleObject accessor()
    {
        return accessor;
    }

    @Override
    public ValueType valueType()
    {
        return valueType;
    }

    public boolean isImmutable()
    {
        return immutable;
    }

    public PropertyInfo getBuilderInfo()
    {
        return builderInfo;
    }

    public boolean queryable()
    {
        return queryable;
    }

    public Object initialValue( Module module )
    {
        // Use supplied value from assembly
        Object value = initialValue;

        // Check for @UseDefaults annotation
        if( value == null && useDefaults )
        {
            if (valueType instanceof ValueCompositeType )
            {
                return module.newValue( valueType().type() );
            } else
            {
                value = DefaultValues.getDefaultValue( type );
            }
        }

        return value;
    }

    @Override
    public void bind( Resolution resolution ) throws BindingException
    {
        valueType = ValueTypeFactory.instance().newValueType( type(), ((Member)accessor()).getDeclaringClass(), resolution.model().type(), resolution.layer(), resolution.module() );

        builderInfo = new BuilderPropertyInfo();

        if (type instanceof TypeVariable)
        {
            type = Classes.resolveTypeVariable( (TypeVariable) type, ((Member)accessor).getDeclaringClass(), resolution.model().type());
        }
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super PropertyModel, ThrowableType> visitor ) throws ThrowableType
    {
        return visitor.visit( this );
    }

    public void checkConstraints( Object value )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( value );
            if( !violations.isEmpty() )
            {
                throw new ConstraintViolationException( "<new instance>", "<unknown>", ((Member)accessor), violations );
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
        if (accessor instanceof Field)
          return ((Field)accessor).toGenericString();
        else
            return ((Method)accessor).toGenericString();
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
        public void checkConstraints( Object value ) throws ConstraintViolationException
        {
            if( constraints != null )
            {
                List<ConstraintViolation> violations = constraints.checkConstraints( value );
                if( !violations.isEmpty() )
                {
                    throw new ConstraintViolationException( "<new instance>", "<unknown>", ((Member)accessor), violations );
                }
            }
        }
    }
}