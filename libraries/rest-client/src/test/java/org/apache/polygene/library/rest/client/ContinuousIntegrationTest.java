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
package org.apache.polygene.library.rest.client;

import org.apache.polygene.test.util.FreePortFinder;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.injection.scope.Structure;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.ApplicationDescriptor;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.rest.client.api.ContextResourceClient;
import org.apache.polygene.library.rest.client.api.ContextResourceClientFactory;
import org.apache.polygene.library.rest.client.api.ErrorHandler;
import org.apache.polygene.library.rest.client.api.HandlerCommand;
import org.apache.polygene.library.rest.client.spi.ResponseHandler;
import org.apache.polygene.library.rest.client.spi.ResultHandler;
import org.apache.polygene.library.rest.common.Resource;
import org.apache.polygene.library.rest.common.ValueAssembler;
import org.apache.polygene.library.rest.common.link.Link;
import org.apache.polygene.library.rest.common.link.Links;
import org.apache.polygene.library.rest.common.link.LinksBuilder;
import org.apache.polygene.library.rest.common.link.LinksUtil;
import org.apache.polygene.library.rest.server.api.ContextResource;
import org.apache.polygene.library.rest.server.api.ContextRestlet;
import org.apache.polygene.library.rest.server.assembler.RestServerAssembler;
import org.apache.polygene.library.rest.server.restlet.NullCommandResult;
import org.apache.polygene.library.rest.server.spi.CommandResult;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.Uniform;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;
import org.restlet.security.User;
import org.restlet.service.MetadataService;

import static org.apache.polygene.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;
import static org.apache.polygene.library.rest.client.api.HandlerCommand.command;
import static org.apache.polygene.library.rest.client.api.HandlerCommand.query;
import static org.apache.polygene.library.rest.client.api.HandlerCommand.refresh;

/**
 * ReST Client libraries documentation source snippets.
 */
public class ContinuousIntegrationTest
    extends AbstractPolygeneTest
{
    private Server server;
    private ContextResourceClient crc;

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // General setup of client and server
        new OrgJsonValueSerializationAssembler().assemble( module );
        new ClientAssembler().assemble( module );
        new ValueAssembler().assemble( module );
        new RestServerAssembler().assemble( module );

        module.objects( NullCommandResult.class );
        module.importedServices( CommandResult.class ).importedBy( NEW_OBJECT );

        module.importedServices( MetadataService.class ).importedBy( NEW_OBJECT );
        module.objects( MetadataService.class );

        // Test specific setup
        module.values( BuildSpec.class, BuildResult.class, ServerStatus.class, TagBuildCommand.class, RunBuildCommand.class );

        module.objects( RootRestlet.class, RootResource.class, RootContext.class );
    }

    @Override
    protected void initApplication( Application app )
        throws Exception
    {
    }

    @Before
    public void startWebServer()
        throws Exception
    {
        int port = FreePortFinder.findFreePortOnLoopback();
        server = new Server( Protocol.HTTP, port );
        ContextRestlet restlet = objectFactory.newObject( ContextRestlet.class, new org.restlet.Context() );

        ChallengeAuthenticator guard = new ChallengeAuthenticator( null, ChallengeScheme.HTTP_BASIC, "testRealm" );
        MapVerifier mapVerifier = new MapVerifier();
        mapVerifier.getLocalSecrets().put( "rickard", "secret".toCharArray() );
        guard.setVerifier( mapVerifier );

        guard.setNext( restlet );

        server.setNext( guard );
        server.start();

        //START SNIPPET: client-create1
        Client client = new Client( Protocol.HTTP );

        ContextResourceClientFactory contextResourceClientFactory = objectFactory.newObject( ContextResourceClientFactory.class, client );
        contextResourceClientFactory.setAcceptedMediaTypes( MediaType.APPLICATION_JSON );
        //END SNIPPET: client-create1

        //START SNIPPET: client-create2
        contextResourceClientFactory.setErrorHandler( new ErrorHandler().onError( ErrorHandler.AUTHENTICATION_REQUIRED, new ResponseHandler()
        {
            boolean tried = false;

            @Override
            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
            {
                if( tried )
                {
                    throw new ResourceException( response.getStatus() );
                }

                tried = true;
                client.getContextResourceClientFactory().getInfo().setUser( new User( "rickard", "secret" ) );

                // Try again
                return refresh();
            }
        } ).onError( ErrorHandler.RECOVERABLE_ERROR, new ResponseHandler()
        {
            @Override
            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
            {
                // Try to restart
                return refresh();
            }
        } ) );
        //END SNIPPET: client-create2

        //START SNIPPET: client-create3
        Reference ref = new Reference( "http://localhost:" + port + '/' );
        crc = contextResourceClientFactory.newClient( ref );
        //END SNIPPET: client-create3
    }

    @After
    public void stopWebServer()
        throws Exception
    {
        server.stop();
    }

    @Override
    protected Application newApplicationInstance( ApplicationDescriptor applicationModel )
    {
        return applicationModel.newInstance( polygene.api(), new MetadataService() );
    }

    @Test
    public void testServerStatus()
    {
        //START SNIPPET: query-without-value
        crc.onResource( new ResultHandler<Resource>()
        {
            @Override
            public HandlerCommand handleResult( Resource result, ContextResourceClient client )
            {
                return query( "status" );
            }
        } ).onQuery( "status", new ResultHandler<ServerStatus>()
            {
                @Override
                public HandlerCommand handleResult( ServerStatus result, ContextResourceClient client )
                {
                    Assert.assertThat( result.currentStatus().get(), CoreMatchers.equalTo( "Idle" ) );
                    return null;
                }
            } );

        crc.start();
        //END SNIPPET: query-without-value
    }

    @Test
    public void testBuildStatusAndTag()
    {
        //START SNIPPET: query-and-command
        crc.onResource( new ResultHandler<Resource>()
        {
            @Override
            public HandlerCommand handleResult( Resource result, ContextResourceClient client )
            {
                return query( "buildstatus", null );
            }
        } ).onProcessingError( "buildstatus", new ResultHandler<BuildSpec>()
        {
            @Override
            public HandlerCommand handleResult( BuildSpec result, ContextResourceClient client )
            {
                ValueBuilder<BuildSpec> builder = valueBuilderFactory.newValueBuilderWithPrototype( result );

                builder.prototype().buildNo().set( "#28" );

                return query( "buildstatus", builder.newInstance() );
            }
        } ).onQuery( "buildstatus", new ResultHandler<BuildResult>()
        {
            @Override
            public HandlerCommand handleResult( BuildResult result, ContextResourceClient client )
            {
                return command( "tagbuild", null );
            }
        } ).onProcessingError( "tagbuild", new ResultHandler<Form>()
        {
            @Override
            public HandlerCommand handleResult( Form result, ContextResourceClient client )
            {
                result.set( "tag", "RC1" );
                result.set( "buildNo", "#28" );

                return command( "tagbuild", result );
            }
        } );

        crc.start();
        //END SNIPPET: query-and-command
    }

    @Test
    public void testRunBuild()
    {
        //START SNIPPET: query-list-and-command
        crc.onResource( new ResultHandler<Resource>()
        {
            @Override
            public HandlerCommand handleResult( Resource result, ContextResourceClient client )
            {
                return query( "runbuild" );
            }
        } ).onQuery( "runbuild", new ResultHandler<Links>()
        {
            @Override
            public HandlerCommand handleResult( Links result, ContextResourceClient client )
            {
                Link link = LinksUtil.withId( "any", result );

                return command( link );
            }
        } ).onCommand( "runbuild", new ResponseHandler()
        {
            @Override
            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
            {
                System.out.println( "Done" );
                return null;
            }
        } );

        crc.start();
        //END SNIPPET: query-list-and-command
    }

    @Test
    public void testRunBuildProgressive()
    {
        //START SNIPPET: query-list-and-command-progressive
        crc.onResource( new ResultHandler<Resource>()
        {
            @Override
            public HandlerCommand handleResult( Resource result, ContextResourceClient client )
            {
                return query( "runbuild" ).onSuccess( new ResultHandler<Links>()
                {
                    @Override
                    public HandlerCommand handleResult( Links result, ContextResourceClient client )
                    {
                        Link link = LinksUtil.withId( "any", result );

                        return command( link ).onSuccess( new ResponseHandler()
                        {
                            @Override
                            public HandlerCommand handleResponse( Response response, ContextResourceClient client )
                            {
                                System.out.println( "Done" );
                                return null;
                            }
                        } );
                    }
                } );
            }
        } );

        crc.start();
        //END SNIPPET: query-list-and-command-progressive
    }

    public interface TagBuildCommand
        extends ValueComposite
    {
        Property<String> buildNo();

        Property<String> tag();

        @Optional
        Property<String> comment();
    }

    public interface BuildResult
        extends ValueComposite
    {
        Property<String> buildNo();

        Property<Integer> testsPassed();

        Property<Integer> testsFailed();
    }

    public interface BuildSpec
        extends ValueComposite
    {
        @Optional
        Property<String> buildNo();

        @Optional
        Property<String> tag();
    }

    public interface ServerStatus
        extends ValueComposite
    {
        Property<String> currentStatus();

        Property<Integer> availableAgents();
    }

    public interface RunBuildCommand
        extends ValueComposite
    {
        Property<String> entity();
    }

    public static class RootRestlet
        extends ContextRestlet
    {
        @Override
        protected Uniform createRoot( Request request, Response response )
        {
            return objectFactory.newObject( RootResource.class, this );
        }
    }

    public static class RootResource
        extends ContextResource
    {

        private RootContext rootContext()
        {
            return context( RootContext.class );
        }

        public RootResource()
        {
        }

        public BuildResult buildstatus( BuildSpec build )
            throws Throwable
        {
            return rootContext().buildStatus( build );
        }

        public ServerStatus status()
            throws Throwable
        {
            return rootContext().serverStatus();
        }

        public void tagbuild( TagBuildCommand command )
            throws Throwable
        {
            rootContext().tagBuild( command );
        }

        public void runbuild( RunBuildCommand run )
        {
            rootContext().runBuildOn( run.entity().get() );
        }

        public Links runbuild()
        {
            return new LinksBuilder( module ).
                command( "runbuild" ).
                addLink( "On available agent", "any" ).
                addLink( "On LinuxAgent", "LinuxAgent" ).
                addLink( "On WinAgent", "WinAgent" ).newLinks();
        }
    }

    public static class RootContext
    {

        @Structure
        Module module;

        public BuildResult buildStatus( BuildSpec build )
        {
            String buildNo = build.buildNo().get(); // or lookup by tag
            return module.newValueFromSerializedState( BuildResult.class, "{ 'buildNo':'" + buildNo + "', 'testsPassed': 37, 'testsFailed': 1}" );
        }

        public ServerStatus serverStatus()
        {
            return module.newValueFromSerializedState( ServerStatus.class, "{'currentStatus':'Idle', 'availableAgents': 2  }" );
        }

        public void tagBuild( TagBuildCommand command )
        {
            // tagged
        }

        public void runBuildOn( String agent )
        {
            // build started
        }
    }
}
