/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.library.beans.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.entity.association.SetAssociation;

public class JavabeanSetAssociation
    implements SetAssociation
{
    private Method pojoMethod;
    private GenericAssociationInfo delegate;
    private final JavabeanMixin javabeanMixin;

    public JavabeanSetAssociation( JavabeanMixin javabeanMixin, Method method )
    {
        this.javabeanMixin = javabeanMixin;
        delegate = new GenericAssociationInfo( method, new MetaInfo() );
    }

    void setPojoMethod( Method pojoMethod )
    {
        this.pojoMethod = pojoMethod;
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return delegate.metaInfo( infoType );
    }

    public String name()
    {
        return delegate.name();
    }

    public String qualifiedName()
    {
        return delegate.qualifiedName();
    }

    public Type type()
    {
        return delegate.type();
    }

    public boolean isImmutable()
    {
        return delegate.isImmutable();
    }

    public boolean isAggregated()
    {
        return delegate.isAggregated();
    }

    public int size()
    {
        return delegate().size();
    }

    public boolean isEmpty()
    {
        return delegate().isEmpty();
    }

    public boolean contains( Object object )
    {
        return delegate().contains( Wrapper.unwrap( object ) );
    }

    public Iterator iterator()
    {
        return new DelegatingIterator( delegate().iterator(), this, javabeanMixin.cbf );
    }

    public Object[] toArray()
    {
        Object[] objects = delegate().toArray();
        Object[] wrapped = new Object[objects.length];
        for( int i = 0; i < objects.length; i++ )
        {
            wrapped[ i ] = Wrapper.wrap( objects[ i ], this, javabeanMixin.cbf );
        }
        return wrapped;
    }

    public boolean add( Object object )
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public boolean remove( Object object )
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public boolean addAll( Collection collection )
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public void clear()
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public boolean retainAll( Collection collection )
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public boolean removeAll( Collection collection )
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public boolean containsAll( Collection collection )
    {
        for( Object obj : collection )
        {
            if( !contains( Wrapper.unwrap( obj ) ) )
            {
                return false;
            }
        }
        return true;
    }

    public Object[] toArray( Object[] objects )
    {
        Object[] array = delegate().toArray( objects );
        for( int i = 0; i < array.length; i++ )
        {
            array[ i ] = Wrapper.wrap( array[ i ], this, javabeanMixin.cbf );
        }
        return array;
    }

    private Set delegate()
    {
        try
        {
            Object resultObject = pojoMethod.invoke( javabeanMixin.pojo );
            return (Set) resultObject;
        }
        catch( IllegalAccessException e )
        {
            throw new IllegalArgumentException( "Javabean is not compatible with JavaBeans specification. Method must be public: " + pojoMethod );
        }
        catch( ClassCastException e )
        {
            throw new IllegalArgumentException( "Javabean and Qi4j models are not compatible. Expected a java.util.Set in return type of " + pojoMethod );
        }
        catch( InvocationTargetException e )
        {
            throw new UndeclaredThrowableException( e.getTargetException() );
        }
    }


}
