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
package org.apache.polygene.bootstrap;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.identity.IdentityGenerator;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceDescriptor;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.api.unitofwork.UnitOfWorkFactory;
import org.apache.polygene.api.util.HierarchicalVisitorAdapter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * TODO
 */
public class ApplicationAssemblerTest
{
    @Test
    public void testApplicationAssembler()
        throws AssemblyException
    {
        Energy4Java polygene = new Energy4Java();

        ApplicationDescriptor model = polygene.newApplicationModel( factory -> {
            ApplicationAssembly assembly = factory.newApplicationAssembly();

            LayerAssembly layer1 = assembly.layer( "Layer1" );

            ModuleAssembly module = layer1.module( "Module1" );

            module.services( TestService.class );

            module.entities( TestEntity.class );

            layer1.services( AssemblySpecifications.ofAnyType( TestService.class ) ).instantiateOnStartup();

            layer1.services( s -> true ).visibleIn( Visibility.layer );

            layer1.entities( s -> true ).visibleIn( Visibility.application );

            return assembly;
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
                    if( serviceDescriptor.hasType( UnitOfWorkFactory.class )
                        || serviceDescriptor.hasType( IdentityGenerator.class )
                        || serviceDescriptor.hasType( Serialization.class ) )
                    {
                        return false;
                    }
                    assertThat( serviceDescriptor.isInstantiateOnStartup(), is( true ) );
                    assertThat( serviceDescriptor.visibility(), equalTo( Visibility.layer ) );
                    return false;
                }
                else if( visited instanceof EntityDescriptor )
                {
                    EntityDescriptor entityDescriptor = (EntityDescriptor) visited;
                    assertThat( entityDescriptor.visibility(), equalTo( Visibility.application ) );
                    return false;
                }

                return true;
            }
        } );
        model.newInstance( polygene.spi() );
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
