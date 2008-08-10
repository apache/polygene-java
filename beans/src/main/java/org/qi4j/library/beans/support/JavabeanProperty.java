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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Arrays;
import java.util.List;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.property.ComputedPropertyInstance;

public class JavabeanProperty extends ComputedPropertyInstance
{
    private Method pojoMethod;
    private JavabeanMixin javabeanMixin;

    public JavabeanProperty( JavabeanMixin javabeanMixin, Method qi4jPropertyMethod )
    {
        super( qi4jPropertyMethod );
        this.javabeanMixin = javabeanMixin;
    }

    public Object get()
    {
        try
        {
            Object resultObject = pojoMethod.invoke( javabeanMixin.pojo );
            return wrap( javabeanMixin.cbf, resultObject );
        }
        catch( IllegalAccessException e )
        {
            throw new IllegalArgumentException( "POJO is not compatible with JavaBeans specification. Method must be public: " + pojoMethod );
        }
        catch( InvocationTargetException e )
        {
            throw new UndeclaredThrowableException( e.getTargetException() );
        }
    }

    private Object wrap( CompositeBuilderFactory factory, Object resultObject )
    {
        Type type = type();
        if( type instanceof Class )
        {
            Class clazz = (Class) type;
            if( clazz.isInterface() )
            {
                if( clazz.equals( List.class ) )
                {
                    if( resultObject.getClass().isArray() )
                    {
                        resultObject = Arrays.asList( (Object[]) resultObject );
                    }
                }
                if( clazz.isArray() )
                {
                    if( List.class.isAssignableFrom( resultObject.getClass() ) )
                    {
                        resultObject = ( (List) resultObject ).toArray();
                    }
                }
                CompositeBuilder<?> builder = factory.newCompositeBuilder( clazz );
                builder.use( resultObject );
                return builder.newInstance();
            }
        }
        if( type instanceof ParameterizedType )
        {
            if( !resultObject.getClass().equals( type ) )
            {
                ParameterizedType paramtype = (ParameterizedType) type;
                Type rawType = paramtype.getRawType();
                Type actType = paramtype.getActualTypeArguments()[ 0 ];
                if( List.class.isAssignableFrom( (Class<?>) rawType ) )
                {
                    if( !( actType instanceof Class ) ||
                        ( (Class) actType ).isInstance( resultObject ) )
                    {
                        String message = "The type " + paramtype + " is not compatible with " + resultObject.getClass();
                        throw new IllegalArgumentException( message );
                    }
                    if( resultObject.getClass().isArray() )
                    {
                        resultObject = Arrays.asList( (Object[]) resultObject );
                    }
                }
            }
        }
        return resultObject;
    }

    void setPojoMethod( Method pojoMethod )
    {
        this.pojoMethod = pojoMethod;
    }
}
