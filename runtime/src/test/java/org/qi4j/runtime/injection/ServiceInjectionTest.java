/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.injection;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.Test;
import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.service.qualifier.AnnotationQualifier;
import org.qi4j.api.service.qualifier.IdentifiedBy;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.service.qualifier.Qualifier;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceImporterException;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.ServiceDeclaration;
import org.qi4j.bootstrap.SingletonAssembler;

import static junit.framework.Assert.*;
import static org.qi4j.api.common.Visibility.*;

/**
 * Test the @Service injection annotation
 */
public class ServiceInjectionTest
{
    @Test
    public void testInjectService()
        throws Exception
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addServices( MyServiceComposite.class ).identifiedBy( "Foo" ).setMetaInfo( new ServiceName( "Foo" ) );
                module.addServices( MyServiceComposite.class ).identifiedBy( "Bar" ).setMetaInfo( new ServiceName( "Bar" ) );
                module.addObjects( ServiceUser.class );
            }
        };

        testInjection( assembly );
    }

    private void testInjection( SingletonAssembler assembly )
    {
        ObjectBuilderFactory builderFactory = assembly.objectBuilderFactory();
        ServiceUser user = builderFactory.newObject( ServiceUser.class );

        assertEquals( "X", user.testSingle() );
        assertEquals( "Foo", user.testIdentity() );
        assertEquals( "XX", user.testIterable() );
        assertEquals( "FooX", user.testServiceReference() );
        assertEquals( "FooXBarX", user.testIterableServiceReferences() );
        assertEquals( "Bar", user.testQualifier() );
    }

    @Test
    public void testInjectionServiceBetweenModules()
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addServices( MyServiceComposite.class ).identifiedBy( "Foo" ).setMetaInfo( new ServiceName( "Foo" ) );
                module.addObjects( ServiceUser.class );

                ModuleAssembly module2 = module.layerAssembly().moduleAssembly( "Other module" );
                ServiceDeclaration service2Decl = module2.addServices( MyServiceComposite.class );
                service2Decl.identifiedBy( "Bar" ).setMetaInfo( new ServiceName( "Bar" ) ).visibleIn( layer );

                ServiceDeclaration service3Decl = module2.addServices( MyServiceComposite.class );
                service3Decl.identifiedBy( "Boo" ).setMetaInfo( new ServiceName( "Boo" ) );
            }
        };

        testInjection( assembly );
    }

    @Test
    public void testInjectionServiceBetweenLayers()
    {
        SingletonAssembler assembly = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addServices( MyServiceComposite.class ).identifiedBy( "Foo" ).setMetaInfo( new ServiceName( "Foo" ) );
                LayerAssembly layerAssembly = module.layerAssembly();
                module.addObjects( ServiceUser.class );

                ApplicationAssembly applicationAssembly = layerAssembly.applicationAssembly();
                LayerAssembly layer2Assembly = applicationAssembly.layerAssembly( "Other layer" );
                layerAssembly.uses( layer2Assembly );

                ModuleAssembly module2 = layer2Assembly.moduleAssembly( "Other module" );

                ServiceDeclaration service2Decl = module2.addServices( MyServiceComposite.class );
                service2Decl.identifiedBy( "Bar" ).setMetaInfo( new ServiceName( "Bar" ) ).visibleIn( application );
            }
        };

        testInjection( assembly );
    }

    @Test( expected = ConstructionException.class )
    public void testMissingServiceDependency()
    {
        // No service fulfils the dependency injection -> fail to create application
        new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addObjects( ServiceUser.class );
            }
        };
    }

    @Mixins( MyServiceMixin.class )
    public static interface MyServiceComposite
        extends MyService, ServiceComposite
    {
    }

    public static interface MyService
    {
        String doStuff();
    }

    public static class MyServiceMixin
        implements MyService
    {

        public String doStuff()
        {
            return "X";
        }
    }

    public static class ServiceUser
    {
        @Service
        MyService service;
        @Service
        MyServiceComposite serviceComposite;
        @Service
        Iterable<MyService> services;
        @Service
        ServiceReference<MyService> serviceRef;
        @Service
        Iterable<ServiceReference<MyService>> serviceRefs;

        @Service @IdentifiedBy("Bar")
        ServiceReference<MyService> qualifiedService;

        @Service @IdentifiedBy("Bar")
        Iterable<ServiceReference<MyService>> qualifiedServiceRefs;

        @Optional
        @Service
        MyServiceMixin optionalService12;

        public String testSingle()
        {
            return service.doStuff();
        }

        public String testIdentity()
        {
            return serviceComposite.identity().get();
        }

        public String testIterable()
        {
            String str = "";
            for( MyService myService : services )
            {
                str += myService.doStuff();
            }
            return str;
        }

        public String testServiceReference()
            throws ServiceImporterException
        {
            ServiceName info = serviceRef.metaInfo( ServiceName.class );
            return info.getName() + serviceRef.get().doStuff();
        }

        public String testIterableServiceReferences()
            throws ServiceImporterException
        {
            String str = "";
            for( ServiceReference<MyService> serviceReference : serviceRefs )
            {
                str += serviceReference.metaInfo( ServiceName.class ).getName();
                str += serviceReference.get().doStuff();
            }
            return str;
        }
        
        public String testQualifier()
        {
            return qualifiedService.metaInfo( ServiceName.class ).getName();
        }

        public String testQualifiedServices()
        {
            String str = "";
            for( ServiceReference<MyService> qualifiedServiceRef : qualifiedServiceRefs )
            {
                str += qualifiedServiceRef.metaInfo( ServiceName.class ).getName();
            }
            return str;
        }
    }

    @Qualifier( NamedSelector.class )
    @Retention( RetentionPolicy.RUNTIME)
    public @interface Named
    {
        public abstract String value();
    }

    public static final class NamedSelector
        implements AnnotationQualifier<Named>
    {
        public <T> ServiceQualifier qualifier( final Named named )
        {
            return new ServiceQualifier()
            {
                @Override
                public boolean qualifies( ServiceReference<?> service )
                {
                    ServiceName serviceName = service.metaInfo( ServiceName.class );
                    return ( serviceName != null && serviceName.getName().equals(named.value() ));
                }
            };
        }
    }

    public static class ServiceName
        implements Serializable
    {
        private String name;

        public ServiceName( String name )
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }
}
