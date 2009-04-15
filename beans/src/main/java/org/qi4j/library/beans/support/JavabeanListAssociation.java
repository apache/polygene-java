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

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.association.GenericAssociationInfo;
import org.qi4j.api.entity.association.ManyAssociation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;

public class JavabeanListAssociation<T>
    implements ManyAssociation<T>
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

    public QualifiedName qualifiedName()
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
        return delegate.isImmutable();
    }

    public int count()
    {
        return delegate().size();
    }

    public List toList()
    {
        return delegate();
    }

    public Set toSet()
    {
        return new HashSet(toList());
    }

    public boolean contains( T object )
    {
        return delegate().contains( Wrapper.unwrap( object ) );
    }

    public Iterator iterator()
    {
        return new DelegatingIterator( delegate().iterator(), this, javabeanMixin.cbf );
    }

    public boolean remove( T object )
    {
        throw new UnsupportedOperationException( "Read/only." );
    }

    public T get( int index )
    {
        return (T) Wrapper.wrap( delegate().get( index ), this, javabeanMixin.cbf );
    }

    public boolean add( int index, Object element )
    {
        throw new UnsupportedOperationException( "Read/only." );
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
