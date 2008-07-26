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

package org.qi4j.library.framework.javabean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.AppliesToFilter;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.NoSuchCompositeException;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.injection.scope.Uses;
import org.qi4j.property.ComputedPropertyInstance;
import org.qi4j.property.Property;

@AppliesTo( { JavabeanMixin.PojoBackedFilter.class } )
public class JavabeanMixin
    implements JavabeanBacked, InvocationHandler
{
    @Structure CompositeBuilderFactory factory;
    private HashMap<Method, Property> properties;
    private Object pojo;

    public JavabeanMixin( @This Composite thisComposite, @Uses Object pojo )
    {
        this.pojo = pojo;
        properties = new HashMap<Method, Property>();
        Class<? extends Composite> type = thisComposite.type();
        for( Method method : type.getMethods() )
        {
            Class<?> returnType = method.getReturnType();
            if( Property.class.isAssignableFrom( returnType ) )
            {
                PojoProperty prop = new PojoProperty( method );
                String methodName = computePojoMethodName( prop.name() );
                Method pojoMethod;
                try
                {
                    pojoMethod = pojo.getClass().getMethod( methodName );
                }
                catch( NoSuchMethodException e )
                {
                    throw new IllegalArgumentException( methodName + " is not present in " + pojo.getClass() );
                }
                prop.pojoMethod = pojoMethod;
                properties.put( method, prop );
            }
        }
    }

    public Object getJavabean()
    {
        return pojo;
    }

    public void setJavabean( Object data )
    {
        pojo = data;
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        synchronized( this )
        {
            return properties.get( method );
        }
    }

    private String computePojoMethodName( String propertyName )
    {
        return "get" + Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 );
    }

    public class PojoProperty extends ComputedPropertyInstance
    {
        private Method pojoMethod;

        public PojoProperty( Method qi4jPropertyMethod )
        {
            super( qi4jPropertyMethod );
        }

        public Object get()
        {
            try
            {
                Object resultObject = pojoMethod.invoke( pojo );
                Class type = (Class) type();
                if( type.isInterface() )
                {
                    try
                    {
                        CompositeBuilder<?> builder = factory.newCompositeBuilder( type );
                        builder.use( resultObject );
                        return builder.newInstance();
                    }
                    catch( NoSuchCompositeException e )
                    {
                        // ignore
                    }
                }
                return resultObject;
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
    }

    public static class PojoBackedFilter
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> modifierClass )
        {
            String methodName = method.getName();
            return Property.class.isAssignableFrom( method.getReturnType() ) ||
                   "getJavabean".equals( methodName ) || "setJavabean".equals( methodName );
        }
    }

}
