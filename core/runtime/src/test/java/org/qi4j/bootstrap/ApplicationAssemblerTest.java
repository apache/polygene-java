/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.qi4j.bootstrap;

import org.junit.Assert;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.functional.HierarchicalVisitorAdapter;
import org.qi4j.functional.Specifications;

/**
 * TODO
 */
public class ApplicationAssemblerTest
{
    @Test
    public void testApplicationAssembler()
        throws AssemblyException
    {
        Energy4Java is = new Energy4Java();

        ApplicationDescriptor model = is.newApplicationModel( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();

                LayerAssembly layer1 = assembly.layer( "Layer1" );

                ModuleAssembly module = layer1.module( "Module1" );

                module.services( TestService.class );

                module.entities( TestEntity.class );

                layer1.services( AssemblySpecifications.types( TestService.class ) ).instantiateOnStartup();

                layer1.services( Specifications.<Object>TRUE() ).visibleIn( Visibility.layer );

                layer1.entities( Specifications.<Object>TRUE() ).visibleIn( Visibility.application );

                return assembly;
            }
        } );

        model.accept( new HierarchicalVisitorAdapter<Object, Object, RuntimeException>()
        {
            @Override
            public boolean visitEnter( Object visited )
                throws RuntimeException
            {
                if( visited instanceof ServiceDescriptor )
                {
                    ServiceDescriptor serviceDescriptor = (ServiceDescriptor) visited;
                    Assert.assertTrue( serviceDescriptor.isInstantiateOnStartup() );
                    Assert.assertTrue( serviceDescriptor.visibility() == Visibility.layer );
                    return false;
                }
                else if( visited instanceof EntityDescriptor )
                {
                    EntityDescriptor entityDescriptor = (EntityDescriptor) visited;
                    Assert.assertTrue( entityDescriptor.visibility() == Visibility.application );
                    return false;
                }

                return true;
            }
        } );
        model.newInstance( is.spi() );
    }

    interface TestService
        extends ServiceComposite
    {

    }

    interface TestEntity
        extends EntityComposite
    {

    }
}
