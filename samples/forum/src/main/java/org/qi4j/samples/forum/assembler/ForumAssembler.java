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
package org.qi4j.samples.forum.assembler;

import java.lang.reflect.Modifier;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ClassScanner;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.file.assembly.FileEntityStoreAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreAssembler;
import org.qi4j.functional.Function;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.fileconfig.FileConfigurationAssembler;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.library.rest.common.ValueAssembler;
import org.qi4j.library.rest.server.assembler.RestServerAssembler;
import org.qi4j.library.rest.server.restlet.RequestReaderDelegator;
import org.qi4j.library.rest.server.restlet.ResponseWriterDelegator;
import org.qi4j.library.rest.server.spi.CommandResult;
import org.qi4j.samples.forum.context.Context;
import org.qi4j.samples.forum.domainevent.DomainCommandResult;
import org.qi4j.samples.forum.rest.resource.RootResource;
import org.qi4j.samples.forum.context.EventsService;
import org.qi4j.samples.forum.data.entity.User;
import org.qi4j.samples.forum.domainevent.DomainEventValue;
import org.qi4j.samples.forum.domainevent.ParameterValue;
import org.qi4j.samples.forum.rest.ForumRestlet;
import org.qi4j.samples.forum.service.BootstrapData;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.restlet.service.MetadataService;

import static org.qi4j.api.util.Classes.hasModifier;
import static org.qi4j.api.util.Classes.isAssignableFrom;
import static org.qi4j.functional.Iterables.filter;
import static org.qi4j.functional.Specifications.not;

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
                withValuesModuleFinder( new Function<Application, Module>()
            {
                @Override
                public Module map( Application app )
                {
                    return app.findModule( "REST", "Values" );
                }
            } ).
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
            for( Class<?> dataClass : filter( hasModifier( Modifier.INTERFACE ), filter( isAssignableFrom( EntityComposite.class ), ClassScanner
                .findClasses( User.class ) ) ) )
            {
                forum.entities( dataClass ).visibleIn( Visibility.application );
            }
        }

        LayerAssembly context = assembly.layer( "Context" ).uses( data );
        {
            ModuleAssembly contexts = context.module( "Context" );
            for( Class<?> contextClass : filter( not( hasModifier( Modifier.INTERFACE ) ), ClassScanner.findClasses( Context.class ) ) )
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
