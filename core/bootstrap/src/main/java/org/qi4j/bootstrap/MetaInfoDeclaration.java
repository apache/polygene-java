/*
 * Copyright (c) 2008, Michael Hunger All Rights Reserved.
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

package org.qi4j.bootstrap;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.property.Property;

/**
 * Declaration of a Property or Association.
 */
public final class MetaInfoDeclaration
    implements StateDeclarations, AssociationDeclarations, ManyAssociationDeclarations, NamedAssociationDeclarations
{
    Map<Class<?>, InfoHolder<?>> mixinPropertyDeclarations = new HashMap<>();

    public MetaInfoDeclaration()
    {
    }

    public <T> MixinDeclaration<T> on( Class<T> mixinType )
    {
        @SuppressWarnings( "unchecked" )
        InfoHolder<T> propertyDeclarationHolder = (InfoHolder<T>) mixinPropertyDeclarations.get( mixinType );
        if( propertyDeclarationHolder == null )
        {
            propertyDeclarationHolder = new InfoHolder<>( mixinType );
            mixinPropertyDeclarations.put( mixinType, propertyDeclarationHolder );
        }
        return propertyDeclarationHolder;
    }

    @Override
    public MetaInfo metaInfoFor( AccessibleObject accessor )
    {
        for( Map.Entry<Class<?>, InfoHolder<?>> entry : mixinPropertyDeclarations.entrySet() )
        {
            InfoHolder<?> holder = entry.getValue();
            MetaInfo metaInfo = holder.metaInfoFor( accessor );
            if( metaInfo != null )
            {
                Class<?> mixinType = entry.getKey();
                return metaInfo.withAnnotations( mixinType )
                    .withAnnotations( accessor )
                    .withAnnotations( accessor instanceof Method ? ( (Method) accessor ).getReturnType() : ( (Field) accessor )
                        .getType() );
            }
        }
        // TODO is this code reached at all??
        Class<?> declaringType = ( (Member) accessor ).getDeclaringClass();
        return new MetaInfo().withAnnotations( declaringType )
            .withAnnotations( accessor )
            .withAnnotations( accessor instanceof Method ? ( (Method) accessor ).getReturnType() : ( (Field) accessor ).getType() );
    }

    @Override
    public Object initialValueOf( AccessibleObject accessor )
    {
        for( InfoHolder<?> propertyDeclarationHolder : mixinPropertyDeclarations.values() )
        {
            final Object initialValue = propertyDeclarationHolder.initialValueOf( accessor );
            if( initialValue != null )
            {
                return initialValue;
            }
        }
        return null;
    }

    @Override
    public boolean useDefaults( AccessibleObject accessor )
    {
        for( InfoHolder<?> propertyDeclarationHolder : mixinPropertyDeclarations.values() )
        {
            final boolean useDefaults = propertyDeclarationHolder.useDefaults( accessor );
            if( useDefaults )
            {
                return useDefaults;
            }
        }
        return false;
    }

    private static class InfoHolder<T>
        implements InvocationHandler, StateDeclarations, MixinDeclaration<T>
    {
        private final static class MethodInfo
        {
            Object initialValue;
            boolean useDefaults;
            MetaInfo metaInfo;

            private MethodInfo( MetaInfo metaInfo )
            {
                this.metaInfo = metaInfo;
            }
        }

        private final Class<T> mixinType;
        private final Map<AccessibleObject, MethodInfo> methodInfos = new HashMap<>();
        // temporary holder
        private MetaInfo metaInfo = null;

        private InfoHolder( Class<T> mixinType )
        {
            this.mixinType = mixinType;
        }

        @Override
        @SuppressWarnings( "raw" )
        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            final MethodInfo methodInfo = new MethodInfo( metaInfo );
            methodInfo.useDefaults = true;
            methodInfos.put( method, methodInfo );
            metaInfo = null; // reset
            final Class<?> returnType = method.getReturnType();
            try
            {
                return Proxy.newProxyInstance( returnType.getClassLoader(), new Class[]{ returnType },
                                               new InvocationHandler()
                                               {
                                                   @Override
                                                   public Object invoke( Object o, Method method, Object[] objects )
                                                       throws Throwable
                                                   {
                                                       if( method.getName().equals( "set" ) )
                                                       {
                                                           methodInfo.initialValue = objects[ 0 ];
                                                       }
                                                       return null;
                                                   }
                                               } );
            }
            catch( IllegalArgumentException e )
            {
                throw new IllegalArgumentException(
                    "Only methods with " + Property.class.getName() + " as return type can have declareDefaults()" );
            }
        }

        public MethodInfo matches( AccessibleObject accessor )
        {
            return methodInfos.get( accessor );
        }

        @Override
        public MetaInfo metaInfoFor( AccessibleObject accessor )
        {
            final MethodInfo methodInfo = matches( accessor );
            if( methodInfo == null )
            {
                return null;
            }
            return methodInfo.metaInfo;
        }

        @Override
        public Object initialValueOf( AccessibleObject accessor )
        {
            final MethodInfo methodInfo = matches( accessor );
            if( methodInfo == null )
            {
                return null;
            }
            return methodInfo.initialValue;
        }

        @Override
        public boolean useDefaults( AccessibleObject accessor )
        {
            final MethodInfo methodInfo = matches( accessor );
            if( methodInfo == null )
            {
                return false;
            }
            return methodInfo.useDefaults;
        }

        // DSL Interface

        @Override
        @SuppressWarnings( "raw" )
        public T declareDefaults()
        {
            return mixinType.cast(
                Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, this ) );
        }

        @Override
        public MixinDeclaration<T> setMetaInfo( Object info )
        {
            if( metaInfo == null )
            {
                metaInfo = new MetaInfo();
            }
            metaInfo.set( info );
            return this;
        }
    }
}