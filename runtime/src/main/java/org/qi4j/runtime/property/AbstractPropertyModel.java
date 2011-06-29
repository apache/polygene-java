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
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.util.Classes;
import org.qi4j.api.util.Visitable;
import org.qi4j.api.util.Visitor;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.types.ValueTypeFactory;
import org.qi4j.spi.property.DefaultValues;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.ValueCompositeType;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.structure.ModuleSPI;

import java.lang.reflect.*;
import java.util.List;

import static org.qi4j.api.util.Classes.RAW_CLASS;
import static org.qi4j.api.util.Classes.TYPE_OF;

/**
 * JAVADOC
 */
public abstract class AbstractPropertyModel
    implements PropertyDescriptor, ConstraintsCheck, Binder, Visitable<AbstractPropertyModel>
{
    private static final long serialVersionUID = 1L;

    private Type type;

    private final Class rawType;

    private transient AccessibleObject accessor; // Interface accessor

    private final QualifiedName qualifiedName;

    private final ValueConstraintsInstance constraints; // May be null

    protected final MetaInfo metaInfo;

    private final Object initialValue;

    private final boolean useDefaults;

    private final boolean immutable;

    private final boolean needsWrapper;

    private ValueType valueType;

    protected PropertyDescriptor builderInfo;

    public AbstractPropertyModel( AccessibleObject accessor, boolean immutable, ValueConstraintsInstance constraints,
                                  MetaInfo metaInfo, Object initialValue
    )
    {
        this.immutable = immutable;
        this.metaInfo = metaInfo;
        type = GenericPropertyInfo.getPropertyType( accessor );
        rawType = RAW_CLASS.map( TYPE_OF.map( accessor ));
        this.accessor = accessor;
        qualifiedName = QualifiedName.fromAccessor( accessor );

        // Check for @UseDefaults annotation
        useDefaults = this.metaInfo.get( UseDefaults.class ) != null;

        this.initialValue = initialValue;

        this.constraints = constraints;

        needsWrapper = !rawType.equals( Property.class );

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

    public Object initialValue( ModuleSPI module )
    {
        Object value = initialValue;

        // Check for @UseDefaults annotation
        if( value == null && useDefaults )
        {
            if (valueType instanceof ValueCompositeType )
            {
                return module.valueBuilderFactory().newValue( valueType().type() );
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
        valueType = ValueTypeFactory.instance().newValueType( type(), ((Member)accessor()).getDeclaringClass(), resolution.object().type(), resolution.layer(), resolution.module() );

        builderInfo = new PropertyDescriptor()
        {
            @Override
            public boolean isImmutable()
            {
                return false;
            }

            @Override
            public <T> T metaInfo( Class<T> infoType )
            {
                return metaInfo.get( infoType );
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
            public Object initialValue( ModuleSPI module )
            {
                return initialValue( module );
            }

            @Override
            public ValueType valueType()
            {
                return valueType;
            }
        };

        if (type instanceof TypeVariable)
        {
            type = Classes.resolveTypeVariable( (TypeVariable) type, ((Member)accessor).getDeclaringClass(), resolution.object().type());
        }
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super AbstractPropertyModel, ThrowableType> visitor ) throws ThrowableType
    {
        return visitor.visit( this );
    }

    public Property<?> newBuilderInstance(ModuleInstance module)
    {
        // Properties cannot be immutable during construction

        Property<?> property;
        property = new PropertyInstance<Object>( builderInfo, initialValue( module ), this );

        return wrapProperty( property );
    }

    public Property<?> newBuilderInstance( ModuleInstance module, Object initialValue )
    {
        // Properties cannot be immutable during construction

        Property<?> property;
        property = new PropertyInstance<Object>( builderInfo, initialValue, this );

        return wrapProperty( property );
    }

    public Property<?> newInitialInstance( ModuleInstance module )
    {
        // Construct instance without using a builder

        return newInstance( initialValue( module ) );
    }

    public abstract <T> Property<T> newInstance( Object value );

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

    public void checkConstraints( PropertiesInstance properties )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            Object value = properties.getProperty( accessor ).get();

            checkConstraints( value );
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

        AbstractPropertyModel that = (AbstractPropertyModel) o;
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

    protected <T> Property<T> wrapProperty( Property<T> property )
    {
        if( needsWrapper )
        {
            // Create proxy
            final ClassLoader loader = rawType.getClassLoader();
            final Class[] types = { rawType };
            property = (Property<T>) Proxy.newProxyInstance( loader, types, new PropertyHandler( property ) );
        }
        return property;
    }

    static class PropertyHandler
        implements InvocationHandler
    {
        Property p;

        public PropertyHandler( Property<?> property )
        {
            p = property;
        }

        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            try
            {
                if( method.getDeclaringClass() == Object.class )
                {
                    return invokeObject( method, args );
                }
                return method.invoke( p, args );
            }
            catch( InvocationTargetException e )
            {
                throw e.getCause();
            }
        }

        private Object invokeObject( Method method, Object[] args )
            throws Throwable
        {
            String methodName = method.getName();
            if( "equals".equals( methodName ) )
            {
                Object arg = args[ 0 ];
                if( Proxy.isProxyClass( arg.getClass() ) )
                {
                    arg = Proxy.getInvocationHandler( arg );
                    if( arg instanceof PropertyHandler )
                    {
                        arg = ( (PropertyHandler) arg ).p;
                    }
                }
                return p.equals( arg );
            }
            else if( "hashCode".equals( methodName ) )
            {
                return p.hashCode();
            }
            else if( "toString".equals( methodName ) )
            {
                return p.toString();
            }
            else if( "wait".equals( methodName ) )
            {
                if( args.length == 0 )
                {
                    p.wait();
                }
                else if( args.length == 1 )
                {
                    p.wait( (Long) args[ 0 ] );
                }
                else
                {
                    p.wait( (Long) args[ 0 ], (Integer) args[ 1 ] );
                }
            }
            else if( "getClass".equals( methodName ) )
            {
                return p.getClass();
            }
            else if( "clone".equals( methodName ) )
            {
                throw new CloneNotSupportedException();
            }
            else if( "notifyAll".equals( methodName ) )
            {
                p.notifyAll();
            }
            else if( "notify".equals( methodName ) )
            {
                p.notify();
            }
            return null;
        }
    }
}