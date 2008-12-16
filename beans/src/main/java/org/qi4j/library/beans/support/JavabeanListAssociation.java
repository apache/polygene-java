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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.entity.association.ListAssociation;
import org.qi4j.api.common.MetaInfo;

public class JavabeanListAssociation
    implements ListAssociation
{
    private Method pojoMethod;
    private final GenericAssociationInfo delegate;
    private JavabeanMixin javabeanMixin;

    public JavabeanListAssociation( JavabeanMixin javabeanMixin, Method method )
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

    public boolean addAll( int index, Collection collection )
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public void clear()
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public Object get( int index )
    {
        return Wrapper.wrap( delegate().get( index ), this, javabeanMixin.cbf );
    }

    public Object set( int index, Object element )
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public void add( int index, Object element )
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public Object remove( int index )
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public int indexOf( Object object )
    {
        return delegate().indexOf( Wrapper.unwrap( object ) );
    }

    public int lastIndexOf( Object object )
    {
        return delegate().lastIndexOf( Wrapper.unwrap( object ) );
    }

    public ListIterator listIterator()
    {
        return new DelegatingListIterator( delegate().listIterator(), this, javabeanMixin.cbf );
    }

    public ListIterator listIterator( int index )
    {
        return new DelegatingListIterator( delegate().listIterator( index ), this, javabeanMixin.cbf );
    }

    public List subList( int fromIndex, int toIndex )
    {
        ArrayList list = new ArrayList();
        List source = delegate().subList( fromIndex, toIndex );
        for( Object obj : source )
        {
            list.add( Wrapper.wrap( obj, this, javabeanMixin.cbf ) );
        }
        return list;
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

    private List delegate()
    {
        try
        {
            Object resultObject = pojoMethod.invoke( javabeanMixin.pojo );
            if( resultObject == null )
            {
                return null;
            }
            if( resultObject.getClass().isArray() )
            {
                return Arrays.asList( (Object[]) resultObject );
            }
            return (List) resultObject;
        }
        catch( IllegalAccessException e )
        {
            throw new IllegalArgumentException( "Javabean is not compatible with JavaBeans specification. Method must be public: " + pojoMethod );
        }
        catch( ClassCastException e )
        {
            throw new IllegalArgumentException( "Javabean and Qi4j model are not compatible. Expected either an array or a java.util.List in return type of " + pojoMethod );
        }
        catch( InvocationTargetException e )
        {
            throw new UndeclaredThrowableException( e.getTargetException() );
        }
    }
}
