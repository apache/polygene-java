/*
 * Copyright 2007, 2008 Niclas Hedhman.
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
package org.qi4j.runtime.injection.provider;

import java.io.Serializable;
import java.lang.reflect.Type;
import org.qi4j.api.Qi4j;
import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Layer;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.bootstrap.InvalidInjectionException;
import org.qi4j.runtime.injection.DependencyModel;
import org.qi4j.runtime.injection.InjectionContext;
import org.qi4j.runtime.injection.InjectionProvider;
import org.qi4j.runtime.injection.InjectionProviderFactory;
import org.qi4j.runtime.model.Resolution;

public final class StructureInjectionProviderFactory
    implements InjectionProviderFactory, Serializable
{
    public InjectionProvider newInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        throws InvalidInjectionException
    {
        return new StructureInjectionProvider( resolution, dependencyModel );
    }

    private static class StructureInjectionProvider
        implements InjectionProvider, Serializable
    {
        final Resolution resolution;
        private final DependencyModel dependencyModel;

        private StructureInjectionProvider( Resolution resolution, DependencyModel dependencyModel )
        {
            this.resolution = resolution;
            this.dependencyModel = dependencyModel;
        }

        public Object provideInjection( InjectionContext context )
            throws InjectionProviderException
        {
            Type type = dependencyModel.injectionType();

            if( type.equals( TransientBuilderFactory.class ) )
            {
                return context.moduleInstance().transientBuilderFactory();
            }
            else if( type.equals( ObjectBuilderFactory.class ) )
            {
                return context.moduleInstance().objectBuilderFactory();
            }
            else if( type.equals( ValueBuilderFactory.class ) )
            {
                return context.moduleInstance().valueBuilderFactory();
            }
            else if( type.equals( UnitOfWorkFactory.class ) )
            {
                return context.moduleInstance().unitOfWorkFactory();
            }
            else if( type.equals( QueryBuilderFactory.class ) )
            {
                return context.moduleInstance().queryBuilderFactory();
            }
            else if( type.equals( ServiceFinder.class ) )
            {
                return context.moduleInstance().serviceFinder();
            }
            else if( Module.class.isAssignableFrom( (Class<?>) type ) )
            {
                return context.moduleInstance();
            }
            else if( Layer.class.isAssignableFrom( (Class<?>) type ) )
            {
                return context.moduleInstance().layerInstance();
            }
            else if( Application.class.isAssignableFrom( (Class<?>) type ) )
            {
                return context.moduleInstance().layerInstance().applicationInstance();
            }
            else if( Qi4j.class.isAssignableFrom( (Class<?>) type ) )
            {
                return context.moduleInstance().layerInstance().applicationInstance().runtime();
            }

            return null;
        }
    }
}
