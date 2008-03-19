/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.runtime.injection;

import java.lang.reflect.Type;
import org.qi4j.Qi4j;
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.entity.UnitOfWorkFactory;
import org.qi4j.runtime.Qi4jRuntime;
import org.qi4j.service.ServiceLocator;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.injection.BindingContext;
import org.qi4j.spi.injection.InjectionContext;
import org.qi4j.spi.injection.InjectionProvider;
import org.qi4j.spi.injection.InjectionProviderFactory;
import org.qi4j.spi.injection.InjectionResolution;
import org.qi4j.spi.injection.InvalidInjectionException;
import org.qi4j.spi.injection.StructureContext;
import org.qi4j.spi.structure.ModuleBinding;

public final class StructureInjectionProviderFactory
    implements InjectionProviderFactory
{
    private Qi4jRuntime runtime;

    public StructureInjectionProviderFactory( Qi4jRuntime runtime )
    {
        this.runtime = runtime;
    }

    public InjectionProvider newInjectionProvider( BindingContext bindingContext ) throws InvalidInjectionException
    {
        InjectionResolution resolution = bindingContext.getInjectionResolution();
        return new StructureInjectionProvider( resolution );
    }

    private class StructureInjectionProvider
        implements InjectionProvider
    {
        InjectionResolution resolution;

        public StructureInjectionProvider( InjectionResolution resolution )
        {
            this.resolution = resolution;
        }

        public Object provideInjection( InjectionContext context )
        {
            Type type = resolution.getInjectionModel().getInjectionType();

            StructureContext structureContext = context.getStructureContext();

            if( type.equals( CompositeBuilderFactory.class ) )
            {
                return structureContext.getCompositeBuilderFactory();
            }
            else if( type.equals( ObjectBuilderFactory.class ) )
            {
                return structureContext.getObjectBuilderFactory();
            }
            else if( type.equals( UnitOfWorkFactory.class ) )
            {
                return structureContext.getUnitOfWorkFactory();
            }
            else if( type.equals( ServiceLocator.class ) )
            {
                return structureContext.getServiceLocator();
            }
            else if( type.equals( ModuleBinding.class ) )
            {
                return context.getModuleBinding();
            }
            else if( type.equals( Qi4j.class ) || type.equals( Qi4jSPI.class ) || type.equals( Qi4jRuntime.class ) )
            {
                return runtime;
            }

            return null;
        }
    }
}
