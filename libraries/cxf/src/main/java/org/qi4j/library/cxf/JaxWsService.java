/*
 * Copyright 2010 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.cxf;

import org.apache.cxf.aegis.AegisContext;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.aegis.type.AbstractTypeCreator;
import org.apache.cxf.aegis.type.TypeCreationOptions;
import org.apache.cxf.aegis.type.TypeMapping;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.activation.Activators;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;

import static org.qi4j.functional.Iterables.first;

@Mixins( JaxWsService.JaxWsMixin.class )
@Activators( JaxWsService.Activator.class )
public interface JaxWsService extends ServiceComposite
{


    void initializeJaxWsFactory();

    public static class Activator
            extends ActivatorAdapter<ServiceReference<JaxWsService>>
    {

        @Override
        public void afterActivation( ServiceReference<JaxWsService> activated )
                throws Exception
        {
            activated.get().initializeJaxWsFactory();
        }

    }

    public abstract static class JaxWsMixin
        implements JaxWsService
    {
        @Structure
        private Module module;

        @Structure
        private ObjectFactory obf;

        @Uses
        ServiceDescriptor descriptor;

        @Override
        public void initializeJaxWsFactory()
        {
            final JaxWsServerFactoryInfo info = descriptor.metaInfo( JaxWsServerFactoryInfo.class );
            AegisDatabinding dataBinding = new AegisDatabinding();
            createQi4jTypeCreator( dataBinding );
            JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
            svrFactory.setDataBinding( dataBinding );
            svrFactory.setServiceClass( first( descriptor.types() ) );
            svrFactory.setServiceBean( findThisService() );
            if( info != null )
            {
                svrFactory.setAddress( info.address() );
                JaxWsCreator creator = info.initializer();
                if( creator != null )
                {
                    creator.initialize( svrFactory );
                }
            }
            else
            {
                svrFactory.setAddress( "http://localhost:9300/" + identity().get() );
                svrFactory.getInInterceptors().add( new LoggingInInterceptor() );
                svrFactory.getOutInterceptors().add( new LoggingOutInterceptor() );
            }
            svrFactory.create();
        }

        private void createQi4jTypeCreator( AegisDatabinding dataBinding )
        {
            Qi4jTypeCreator qi4jTypeCreator = obf.newObject( Qi4jTypeCreator.class );

            AegisContext aegisContext = dataBinding.getAegisContext();
            TypeMapping typeMapping = aegisContext.getTypeMapping();
            AbstractTypeCreator defaultCreator = (AbstractTypeCreator) typeMapping.getTypeCreator();
            TypeCreationOptions configuration = defaultCreator.getConfiguration();
            qi4jTypeCreator.setConfiguration( configuration );
            qi4jTypeCreator.setNextCreator( defaultCreator );
            qi4jTypeCreator.setTypeMapping( typeMapping );
        }

        private Object findThisService()
        {
            ServiceReference<?> reference = module.findService( first( descriptor.types()) );
            if( reference == null )
            {
                System.err.println( "Internal Error?? JaxWsService.findThisService()" );
                return null; // Should not be possible.
            }
            return reference.get();
        }

    }
}
