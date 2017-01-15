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
package org.apache.polygene.sample.forum.assembler;

import java.lang.reflect.Modifier;
import java.util.List;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.ApplicationAssembler;
import org.apache.polygene.bootstrap.ApplicationAssembly;
import org.apache.polygene.bootstrap.ApplicationAssemblyFactory;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ClassScanner;
import org.apache.polygene.bootstrap.LayerAssembly;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.entitystore.file.assembly.FileEntityStoreAssembler;
import org.apache.polygene.entitystore.memory.assembly.MemoryEntityStoreAssembler;
import org.apache.polygene.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationAssembler;
import org.apache.polygene.library.rdf.repository.NativeConfiguration;
import org.apache.polygene.library.rest.common.ValueAssembler;
import org.apache.polygene.library.rest.server.assembler.RestServerAssembler;
import org.apache.polygene.library.rest.server.restlet.RequestReaderDelegator;
import org.apache.polygene.library.rest.server.restlet.ResponseWriterDelegator;
import org.apache.polygene.library.rest.server.spi.CommandResult;
import org.apache.polygene.sample.forum.context.Context;
import org.apache.polygene.sample.forum.context.EventsService;
import org.apache.polygene.sample.forum.data.entity.User;
import org.apache.polygene.sample.forum.domainevent.DomainCommandResult;
import org.apache.polygene.sample.forum.domainevent.DomainEventValue;
import org.apache.polygene.sample.forum.domainevent.ParameterValue;
import org.apache.polygene.sample.forum.rest.ForumRestlet;
import org.apache.polygene.sample.forum.rest.resource.RootResource;
import org.apache.polygene.sample.forum.service.BootstrapData;
import org.apache.polygene.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.restlet.service.MetadataService;

import static java.util.stream.Collectors.toList;
import static org.apache.polygene.api.util.Classes.hasModifier;
import static org.apache.polygene.api.util.Classes.isAssignableFrom;

/**
 * TODO
 */
public class ForumAssembler
    implements ApplicationAssembler
{
    @Override
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException
    {
        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();

        assembly.setName( "Forum" );

        ModuleAssembly configModule;
        LayerAssembly configuration = assembly.layer( "Configuration" );
        {
            configModule = configuration.module( "Configuration" );
            new OrgJsonValueSerializationAssembler().assemble( configModule );
            new MemoryEntityStoreAssembler().assemble( configModule );
            new FileConfigurationAssembler().visibleIn( Visibility.application ).assemble( configModule );
        }

        LayerAssembly infrastructure = assembly.layer( "Infrastructure" ).uses( configuration );
        {
            ModuleAssembly serialization = infrastructure.module( "Serialization" );
            new OrgJsonValueSerializationAssembler().
                visibleIn( Visibility.application ).
                assemble( serialization );

            ModuleAssembly entityStore = infrastructure.module( "EntityStore" );
            new FileEntityStoreAssembler()
                .visibleIn( Visibility.application )
                .withConfig( configModule, Visibility.application )
                .assemble( entityStore );

            ModuleAssembly indexQuery = infrastructure.module( "IndexQuery" );
            new RdfNativeSesameStoreAssembler( Visibility.application, Visibility.application )
                .assemble( indexQuery );
            configModule.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
        }

        LayerAssembly data = assembly.layer( "Data" ).uses( infrastructure );
        {
            ModuleAssembly forum = data.module( "Forum" );
            ClassScanner.findClasses( User.class )
                        .filter( isAssignableFrom( EntityComposite.class ) )
                        .filter( hasModifier( Modifier.INTERFACE ) )
                        .forEach( dataClass -> forum.entities( dataClass ).visibleIn( Visibility.application ) );
        }

        LayerAssembly context = assembly.layer( "Context" ).uses( data );
        {
            ModuleAssembly contexts = context.module( "Context" );
            List<? extends Class<?>> contextClasses = ClassScanner.findClasses( Context.class )
                                                                  .filter( hasModifier( Modifier.INTERFACE ).negate() )
                                                                  .collect( toList() );
            for( Class<?> contextClass : contextClasses )
            {
                if( contextClass.getName().contains( "$" ) )
                {
                    contexts.transients( contextClass ).visibleIn( Visibility.application );
                }
                else
                {
                    contexts.objects( contextClass ).visibleIn( Visibility.application );
                }
            }

            ClassScanner.findClasses( Context.class )
                        .filter( isAssignableFrom( ValueComposite.class ) )
                        .forEach( valueClass -> contexts.values( valueClass ).visibleIn( Visibility.application ) );

            contexts.services( EventsService.class );

            context.module( "Domain events" )
                .values( DomainEventValue.class, ParameterValue.class )
                .visibleIn( Visibility.application );
        }

        LayerAssembly services = assembly.layer( "Service" ).uses( data );
        {
            ModuleAssembly bootstrap = services.module( "Bootstrap" );
            bootstrap.services( BootstrapData.class ).identifiedBy( "bootstrap" ).instantiateOnStartup();
        }

        LayerAssembly rest = assembly.layer( "REST" ).uses( context, data );
        {
            ModuleAssembly values = rest.module( "Values" );
            {
                new ValueAssembler().assemble( values );
            }

            ModuleAssembly transformation = rest.module( "Transformation" );
            {
                new RestServerAssembler().assemble( transformation );
                transformation.objects( RequestReaderDelegator.class, ResponseWriterDelegator.class )
                    .visibleIn( Visibility.layer );
                new OrgJsonValueSerializationAssembler().assemble( transformation );
            }

            ModuleAssembly resources = rest.module( "Resources" );
            List<? extends Class<?>> resourceClasses = ClassScanner.findClasses( RootResource.class ).collect( toList() );
            for( Class<?> resourceClass : resourceClasses )
            {
                resources.objects( resourceClass ).visibleIn( Visibility.layer );
            }

            ModuleAssembly restlet = rest.module( "Restlet" );
            restlet.objects( ForumRestlet.class );
            restlet.importedServices( CommandResult.class ).setMetaInfo( new DomainCommandResult() );
            restlet.importedServices( MetadataService.class );
        }

        return assembly;
    }
}
