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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
import org.qi4j.bootstrap.BindingException;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.model.Binder;
import org.qi4j.runtime.model.Resolution;
import org.qi4j.spi.property.DefaultValues;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.util.SerializationUtil;

/**
 * JAVADOC
 */
public abstract class AbstractPropertyModel
    implements Serializable, PropertyDescriptor, ConstraintsCheck, Binder
{
    private static final long serialVersionUID = 1L;

    private final Type type;

    private transient Method accessor; // Interface accessor

    private final QualifiedName qualifiedName;

    private final ValueConstraintsInstance constraints; // May be null

    protected final MetaInfo metaInfo;

    private final Object initialValue;

    private final boolean useDefaults;

    private final boolean immutable;

    private final boolean computed;

    private final boolean needsWrapper;

    protected final PropertyInfo builderInfo;

    public AbstractPropertyModel( Method accessor, boolean immutable, ValueConstraintsInstance constraints,
                                  MetaInfo metaInfo, Object initialValue
    )
    {
        this.immutable = immutable;
        this.metaInfo = metaInfo;
        type = GenericPropertyInfo.getPropertyType( accessor );
        this.accessor = accessor;
        qualifiedName = QualifiedName.fromMethod( accessor );

        // Check for @UseDefaults annotation
        useDefaults = this.metaInfo.get( UseDefaults.class ) != null;

        this.initialValue = initialValue;

        this.constraints = constraints;

        computed = this.metaInfo.get( Computed.class ) != null;
        needsWrapper = !this.accessor.getReturnType().equals( Property.class );

        builderInfo = new GenericPropertyInfo( this.metaInfo, false, computed, qualifiedName, type );
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

    public Object initialValue()
    {
        Object value = initialValue;

        // Check for @UseDefaults annotation
        if( value == null && useDefaults )
        {
            value = DefaultValues.getDefaultValue( type );
        }

        return value;
    }

    public void bind( Resolution resolution )
        throws BindingException
    {
        // TODO Select ValueComposite type
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
            property = new PropertyInstance<Object>( builderInfo, initialValue(), this );
        }

        return wrapProperty( property );
    }

    public Property<?> newBuilderInstance( Object initialValue )
    {
        // Properties cannot be immutable during construction

        Property<?> property;
        if( computed )
        {
            property = new ComputedPropertyInfo<Object>( builderInfo );
        }
        else
        {
            property = new PropertyInstance<Object>( builderInfo, initialValue, this );
        }

        return wrapProperty( property );
    }

    public Property<?> newInitialInstance()
    {
        // Construct instance without using a builder

        return newInstance( initialValue() );
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
                throw new ConstraintViolationException( "", "<unknown>", accessor, violations );
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
        return accessor.toGenericString();
    }

    protected <T> Property<T> wrapProperty( Property<T> property )
    {
        if( needsWrapper && !accessor.getReturnType().isInstance( property ) )
        {
            // Create proxy
            final ClassLoader loader = accessor.getReturnType().getClassLoader();
            final Class[] type = { accessor.getReturnType() };
            property = (Property<T>) Proxy.newProxyInstance( loader, type, new PropertyHandler( property ) );
        }
        return property;
    }

    private void writeObject( ObjectOutputStream out )
        throws IOException
    {
        out.defaultWriteObject();
        try
        {
            SerializationUtil.writeMethod( out, accessor );
        }
        catch( NotSerializableException e )
        {
            System.err.println( "NotSerializable in " + getClass() );
            throw e;
        }
    }

    private void readObject( ObjectInputStream in )
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        accessor = SerializationUtil.readMethod( in );
    }

    protected static class ComputedPropertyInfo<T>
        extends ComputedPropertyInstance<T>
    {
        public ComputedPropertyInfo( PropertyInfo aPropertyInfo )
            throws IllegalArgumentException
        {
            super( aPropertyInfo );
        }

        public T get()
        {
            throw new IllegalStateException( "Property [" + qualifiedName().name() + "] must be computed" );
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