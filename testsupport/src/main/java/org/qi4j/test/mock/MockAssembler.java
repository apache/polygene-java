/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.test.mock;

import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.CompositeDeclaration;
import org.qi4j.bootstrap.EntityDeclaration;
import org.qi4j.bootstrap.ObjectDeclaration;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.bootstrap.InfoDeclaration;
import org.qi4j.service.ServiceComposite;
import org.qi4j.composite.Composite;
import org.qi4j.entity.EntityComposite;
import org.qi4j.spi.service.provider.SingletonInstanceFactory;
import org.qi4j.spi.service.provider.Singleton;
import java.util.Map;
import java.util.HashMap;

/**
 * TODO
 */
public class MockAssembler
    implements Assembler
{
    private Assembler delegate;
    private Map<Class, ServiceComposite> mockedServices = new HashMap<Class, ServiceComposite>( );

    public MockAssembler( Assembler delegate )
    {
        this.delegate = delegate;
    }

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {

    }

    public MockAssembler mock( ServiceComposite... mockService)
    {
        for( ServiceComposite serviceComposite : mockService )
        {
            mockedServices.put(serviceComposite.getClass().getInterfaces()[0], serviceComposite);
        }
        return this;
    }

    private class MockModuleAssembly
        implements ModuleAssembly
    {
        ModuleAssembly delegate;

        private MockModuleAssembly( ModuleAssembly delegate )
        {
            this.delegate = delegate;
        }

        public void addAssembler( Assembler assembler ) throws AssemblyException
        {
            delegate.addAssembler( assembler );
        }

        public LayerAssembly layerAssembly()
        {
            return delegate.layerAssembly();
        }

        public void setName( String name )
        {
            delegate.setName( name );
        }

        public String name()
        {
            return delegate.name();
        }

        public CompositeDeclaration addComposites( Class<? extends Composite>... compositeTypes ) throws AssemblyException
        {
            return delegate.addComposites( compositeTypes );
        }

        public EntityDeclaration addEntities( Class<? extends EntityComposite>... compositeTypes ) throws AssemblyException
        {
            return delegate.addEntities( compositeTypes );
        }

        public ObjectDeclaration addObjects( Class... objectTypes ) throws AssemblyException
        {
            return delegate.addObjects( objectTypes );
        }

        public ServiceDeclaration addServices( Class<? extends ServiceComposite>... serviceTypes ) throws AssemblyException
        {
            for( Class<? extends ServiceComposite> serviceType : serviceTypes )
            {
                ServiceComposite mockedService = mockedServices.get( serviceType );
                if (mockedService != null)
                {
                    delegate.addServices( serviceType ).providedBy( SingletonInstanceFactory.class ).setMetaInfo( new Singleton(mockedService) );
                } else
                {
                    delegate.addServices( serviceType );
                }
            }

            return null;
        }

        public <T> InfoDeclaration<T> on( Class<T> mixinType )
        {
            return null;
        }
    }
}
