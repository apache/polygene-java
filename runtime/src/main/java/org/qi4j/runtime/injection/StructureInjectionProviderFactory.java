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
import org.qi4j.composite.CompositeBuilderFactory;
import org.qi4j.composite.ObjectBuilderFactory;
import org.qi4j.spi.dependency.InjectionContext;
import org.qi4j.spi.dependency.InjectionProvider;
import org.qi4j.spi.dependency.InjectionProviderFactory;
import org.qi4j.spi.dependency.InjectionResolution;
import org.qi4j.spi.dependency.InvalidInjectionException;
import org.qi4j.spi.structure.ModuleBinding;

public class StructureInjectionProviderFactory
    implements InjectionProviderFactory
{
    public InjectionProvider newInjectionProvider( InjectionResolution resolution ) throws InvalidInjectionException
    {
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

            if( type.equals( CompositeBuilderFactory.class ) )
            {
                return context.getCompositeBuilderFactory();
            }
            else if( type.equals( ObjectBuilderFactory.class ) )
            {
                return context.getObjectBuilderFactory();
            }
            else if( type.equals( ModuleBinding.class ) )
            {
                return context.getModuleBinding();
            }

            return null;
        }
    }
}
