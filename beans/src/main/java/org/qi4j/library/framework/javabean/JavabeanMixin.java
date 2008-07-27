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
import java.lang.reflect.Method;
import java.util.HashMap;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.AppliesToFilter;
import org.qi4j.composite.Composite;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.ListAssociation;
import org.qi4j.entity.association.ManyAssociation;
import org.qi4j.entity.association.SetAssociation;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.injection.scope.Uses;
import org.qi4j.property.Property;

@AppliesTo( { JavabeanMixin.JavabeanSupportFilter.class } )
public class JavabeanMixin
    implements JavabeanSupport, InvocationHandler
{
    private HashMap<Method, Object> handlers;

    @Structure CompositeBuilderFactory cbf;
    Object pojo;

    public JavabeanMixin( @This Composite thisComposite, @Uses Object pojo )
    {
        this.pojo = pojo;
        handlers = new HashMap<Method, Object>();
        Class<? extends Composite> type = thisComposite.type();
        for( Method method : type.getMethods() )
        {
            Class<?> returnType = method.getReturnType();
            if( Property.class.isAssignableFrom( returnType ) )
            {
                JavabeanProperty prop = new JavabeanProperty( this, method );
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
                prop.setPojoMethod( pojoMethod );
                handlers.put( method, prop );
            }
            else if( SetAssociation.class.isAssignableFrom( returnType ) )
            {
                JavabeanSetAssociation association = new JavabeanSetAssociation( this, method );
                String methodName = computePojoMethodName( association.name() );
                Method pojoMethod;
                try
                {
                    pojoMethod = pojo.getClass().getMethod( methodName );
                }
                catch( NoSuchMethodException e )
                {
                    throw new IllegalArgumentException( methodName + " is not present in " + pojo.getClass() );
                }
                association.setPojoMethod( pojoMethod );
                handlers.put( method, association );
            }
            else if( ListAssociation.class.isAssignableFrom( returnType ) ||
                     ManyAssociation.class.isAssignableFrom( returnType ) )
            {
                JavabeanListAssociation association = new JavabeanListAssociation( this, method );
                String methodName = computePojoMethodName( association.name() );
                Method pojoMethod;
                try
                {
                    pojoMethod = pojo.getClass().getMethod( methodName );
                }
                catch( NoSuchMethodException e )
                {
                    throw new IllegalArgumentException( methodName + " is not present in " + pojo.getClass() );
                }
                association.setPojoMethod( pojoMethod );
                handlers.put( method, association );
            }
            else if( Association.class.isAssignableFrom( returnType ) )
            {
                JavabeanAssociation association = new JavabeanAssociation( this, method );
                String methodName = computePojoMethodName( association.name() );
                Method pojoMethod;
                try
                {
                    pojoMethod = pojo.getClass().getMethod( methodName );
                }
                catch( NoSuchMethodException e )
                {
                    throw new IllegalArgumentException( methodName + " is not present in " + pojo.getClass() );
                }
                association.pojoMethod = pojoMethod;
                handlers.put( method, association );
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

    public Object invoke( Object proxy, Method method, Object[] args )
        throws Throwable
    {
        synchronized( this )
        {
            return handlers.get( method );
        }
    }

    private String computePojoMethodName( String propertyName )
    {
        return "get" + Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 );
    }

    public static class JavabeanSupportFilter
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class<?> mixin, Class<?> compositeType, Class<?> modifierClass )
        {
            String methodName = method.getName();
            Class<?> retType = method.getReturnType();
            return Property.class.isAssignableFrom( retType ) ||
                   Association.class.isAssignableFrom( retType ) ||
                   SetAssociation.class.isAssignableFrom( retType ) ||
                   ListAssociation.class.isAssignableFrom( retType ) ||
                   ManyAssociation.class.isAssignableFrom( retType ) ||
                   "getJavabean".equals( methodName ) || "setJavabean".equals( methodName );
        }
    }

}
