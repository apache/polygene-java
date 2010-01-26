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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.spi.util.MethodKeyMap;

/**
 * Declaration of a Property or Association.
 */
public final class MetaInfoDeclaration
    implements PropertyDeclarations, AssociationDeclarations, ManyAssociationDeclarations, Serializable
{
    Map<Class<?>, InfoHolder<?>> mixinPropertyDeclarations = new HashMap<Class<?>, InfoHolder<?>>();

    public MetaInfoDeclaration()
    {
    }

    public <T> MixinDeclaration<T> on( Class<T> mixinType )
    {
        InfoHolder<T> propertyDeclarationHolder = (InfoHolder<T>) mixinPropertyDeclarations.get( mixinType );
        if( propertyDeclarationHolder == null )
        {
            propertyDeclarationHolder = new InfoHolder<T>( mixinType );
            mixinPropertyDeclarations.put( mixinType, propertyDeclarationHolder );
        }
        return propertyDeclarationHolder;
    }

    public MetaInfo getMetaInfo( Method accessor )
    {
        for( Map.Entry<Class<?>, InfoHolder<?>> entry : mixinPropertyDeclarations.entrySet() )
        {
            InfoHolder<?> holder = entry.getValue();
            MetaInfo metaInfo = holder.getMetaInfo( accessor );
            if( metaInfo != null )
            {
                Class<?> mixinType = entry.getKey();
                return metaInfo.withAnnotations( mixinType )
                    .withAnnotations( accessor )
                    .withAnnotations( accessor.getReturnType() );
            }
        }
        // TODO is this code reached at all??
        Class<?> declaringType = accessor.getDeclaringClass();
        return new MetaInfo().withAnnotations( declaringType )
            .withAnnotations( accessor )
            .withAnnotations( accessor.getReturnType() );
    }

    public Object getInitialValue( Method accessor )
    {
        for( InfoHolder<?> propertyDeclarationHolder : mixinPropertyDeclarations.values() )
        {
            final Object initialValue = propertyDeclarationHolder.getInitialValue( accessor );
            if( initialValue != null )
            {
                return initialValue;
            }
        }
        return null;
    }

    private static class InfoHolder<T>
        implements InvocationHandler, PropertyDeclarations, MixinDeclaration<T>, Serializable
    {
        private final static class MethodInfo
            implements Serializable
        {
            Object initialValue;
            MetaInfo metaInfo;

            private MethodInfo( MetaInfo metaInfo )
            {
                this.metaInfo = metaInfo;
            }
        }

        private final Class<T> mixinType;
        private final Map<Method, MethodInfo> methodInfos = new MethodKeyMap<MethodInfo>();
        // temporary holder
        private MetaInfo metaInfo = null;

        public InfoHolder( Class<T> mixinType )
        {
            this.mixinType = mixinType;
        }

        public Object invoke( Object o, Method method, Object[] objects )
            throws Throwable
        {
            final MethodInfo methodInfo = new MethodInfo( metaInfo );
            methodInfos.put( method, methodInfo );
            metaInfo = null; // reset
            final Class<?> returnType = method.getReturnType();
            return Proxy.newProxyInstance( returnType.getClassLoader(), new Class[]{ returnType }, new InvocationHandler()
            {
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

        public MethodInfo matches( Method accessor )
        {
            return methodInfos.get( accessor );
        }

        public MetaInfo getMetaInfo( Method accessor )
        {
            final MethodInfo methodInfo = matches( accessor );
            if( methodInfo == null )
            {
                return null;
            }
            return methodInfo.metaInfo;
        }

        public Object getInitialValue( Method accessor )
        {
            final MethodInfo methodInfo = matches( accessor );
            if( methodInfo == null )
            {
                return null;
            }
            return methodInfo.initialValue;
        }

        // DSL Interface

        public T declareDefaults()
        {
            return mixinType.cast( Proxy.newProxyInstance( mixinType.getClassLoader(), new Class[]{ mixinType }, this ) );
        }

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