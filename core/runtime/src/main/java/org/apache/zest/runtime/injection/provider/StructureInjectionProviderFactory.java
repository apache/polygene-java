/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.runtime.injection.provider;

import java.lang.reflect.Type;
import org.apache.zest.api.ZestAPI;
import org.apache.zest.api.composite.TransientBuilderFactory;
import org.apache.zest.api.object.ObjectFactory;
import org.apache.zest.api.query.QueryBuilderFactory;
import org.apache.zest.api.service.ServiceFinder;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.api.structure.Layer;
import org.apache.zest.api.structure.LayerDescriptor;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.structure.ModuleDescriptor;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.apache.zest.bootstrap.InvalidInjectionException;
import org.apache.zest.runtime.injection.DependencyModel;
import org.apache.zest.runtime.injection.InjectionContext;
import org.apache.zest.runtime.injection.InjectionProvider;
import org.apache.zest.runtime.injection.InjectionProviderFactory;
import org.apache.zest.runtime.model.Resolution;
import org.apache.zest.runtime.structure.ApplicationInstance;

public final class StructureInjectionProviderFactory
    implements InjectionProviderFactory
{
    @Override
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        return new StructureInjectionProvider( dependencyModel );
    }

    private static class StructureInjectionProvider
        implements InjectionProvider
    {
        private final DependencyModel dependencyModel;

        private StructureInjectionProvider( DependencyModel dependencyModel )
        {
            this.dependencyModel = dependencyModel;
        }

        @Override
        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            Type type1 = dependencyModel.injectionType();
            if( !( type1 instanceof Class ) )
            {
                throw new InjectionProviderException( "Type [" + type1 + "] can not be injected from the @Structure injection scope: " + context );
            }
            Class clazz = (Class) type1;
            if( clazz.equals( TransientBuilderFactory.class ) )
            {
                return context.module().instance();
            }
            else if( clazz.equals( ObjectFactory.class ) )
            {
                return context.module().instance();
            }
            else if( clazz.equals( ValueBuilderFactory.class ) )
            {
                return context.module().instance();
            }
            else if( clazz.equals( UnitOfWorkFactory.class ) )
            {
                return context.module().instance().unitOfWorkFactory();
            }
            else if( clazz.equals( QueryBuilderFactory.class ) )
            {
                return context.module().instance();
            }
            else if( clazz.equals( ServiceFinder.class ) )
            {
                return context.module().instance();
            }
            else if( Module.class.isAssignableFrom( clazz ) )
            {
                return context.module().instance();
            }
            else if( ModuleDescriptor.class.isAssignableFrom( clazz ) )
            {
                return context.module();
            }
            else if( Layer.class.isAssignableFrom( clazz ) )
            {
                return context.module().layer().instance();
            }
            else if( LayerDescriptor.class.isAssignableFrom( clazz ) )
            {
                return context.module().layer();
            }
            else if( Application.class.isAssignableFrom( clazz ) )
            {
                return context.module().layer().instance().application();
            }
            else if( ApplicationDescriptor.class.isAssignableFrom( clazz ) )
            {
                return context.module().layer().instance().application().descriptor();
            }
            else if( ZestAPI.class.isAssignableFrom( clazz ) )
            {
                return (( ApplicationInstance) context.module().layer().instance().application()).runtime();
            }
            return null;
        }
    }
}
