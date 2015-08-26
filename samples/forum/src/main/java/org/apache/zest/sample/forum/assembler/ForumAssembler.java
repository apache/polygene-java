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
package org.apache.zest.sample.forum.assembler;

import java.lang.reflect.Modifier;
import java.util.function.Function;
import org.apache.zest.api.common.Visibility;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.ApplicationAssembler;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.ApplicationAssemblyFactory;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ClassScanner;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.file.assembly.FileEntityStoreAssembler;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.library.fileconfig.FileConfigurationService;
import org.apache.zest.library.rest.common.ValueAssembler;
import org.apache.zest.library.rest.server.assembler.RestServerAssembler;
import org.apache.zest.library.rest.server.restlet.RequestReaderDelegator;
import org.apache.zest.library.rest.server.restlet.ResponseWriterDelegator;
import org.apache.zest.library.rest.server.spi.CommandResult;
import org.apache.zest.sample.forum.context.Context;
import org.apache.zest.sample.forum.domainevent.DomainCommandResult;
import org.apache.zest.sample.forum.rest.resource.RootResource;
import org.apache.zest.sample.forum.context.EventsService;
import org.apache.zest.sample.forum.data.entity.User;
import org.apache.zest.sample.forum.domainevent.DomainEventValue;
import org.apache.zest.sample.forum.domainevent.ParameterValue;
import org.apache.zest.sample.forum.rest.ForumRestlet;
import org.apache.zest.sample.forum.service.BootstrapData;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.restlet.service.MetadataService;

import static org.apache.zest.api.util.Classes.hasModifier;
import static org.apache.zest.api.util.Classes.isAssignableFrom;
import static org.apache.zest.functional.Iterables.filter;

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
            configModule.services( MemoryEntityStoreService.class );
            configModule.services( UuidIdentityGeneratorService.class );
            new OrgJsonValueSerializationAssembler().assemble( configModule );
        }

        LayerAssembly infrastructure = assembly.layer( "Infrastructure" ).uses( configuration );
        {
            ModuleAssembly entityStore = infrastructure.module( "EntityStore" );
            entityStore.services( FileConfigurationService.class );
            new FileEntityStoreAssembler().withConfig( configModule, Visibility.application ).assemble( entityStore );
            entityStore.services( UuidIdentityGeneratorService.class ).visibleIn( Visibility.application );
            new OrgJsonValueSerializationAssembler().
                visibleIn( Visibility.application ).
                withValuesModuleFinder( app -> app.findModule( "REST", "Values" ) ).
                assemble( entityStore );
        }

        LayerAssembly data = assembly.layer( "Data" ).uses( infrastructure );
        {
            ModuleAssembly forum = data.module( "Forum" );
            for( Class<?> dataClass : filter( hasModifier( Modifier.INTERFACE ), filter( isAssignableFrom( EntityComposite.class ), ClassScanner
                .findClasses( User.class ) ) ) )
            {
                forum.entities( dataClass ).visibleIn( Visibility.application );
            }
        }

        LayerAssembly context = assembly.layer( "Context" ).uses( data );
        {
            ModuleAssembly contexts = context.module( "Context" );
            for( Class<?> contextClass : filter( hasModifier( Modifier.INTERFACE ).negate(), ClassScanner.findClasses( Context.class ) ) )
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

            for( Class<?> valueClass : filter( isAssignableFrom( ValueComposite.class ), ClassScanner.findClasses( Context.class ) ) )
            {
                contexts.values( valueClass ).visibleIn( Visibility.application );
            }

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
            for( Class<?> resourceClass : ClassScanner.findClasses( RootResource.class ) )
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
