package org.qi4j.library.rest.client;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rest.client.api.ContextResourceClient;
import org.qi4j.library.rest.client.api.ContextResourceClientFactory;
import org.qi4j.library.rest.client.api.ErrorHandler;
import org.qi4j.library.rest.client.api.HandlerCommand;
import org.qi4j.library.rest.client.spi.ResponseHandler;
import org.qi4j.library.rest.client.spi.ResultHandler;
import org.qi4j.library.rest.common.Resource;
import org.qi4j.library.rest.common.ValueAssembler;
import org.qi4j.library.rest.common.link.Link;
import org.qi4j.library.rest.common.link.Links;
import org.qi4j.library.rest.common.link.LinksBuilder;
import org.qi4j.library.rest.common.link.LinksUtil;
import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.library.rest.server.api.ContextRestlet;
import org.qi4j.library.rest.server.assembler.RestServerAssembler;
import org.qi4j.library.rest.server.restlet.NullCommandResult;
import org.qi4j.library.rest.server.spi.CommandResult;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
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

import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;
import static org.qi4j.library.rest.client.api.HandlerCommand.command;
import static org.qi4j.library.rest.client.api.HandlerCommand.query;
import static org.qi4j.library.rest.client.api.HandlerCommand.refresh;

/**
 * ReST Client libraries documentation source snippets.
 */
public class ContinuousIntegrationTest
    extends AbstractQi4jTest
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
        server = new Server( Protocol.HTTP, 8888 );
        ContextRestlet restlet = module.newObject( ContextRestlet.class, new org.restlet.Context() );

        ChallengeAuthenticator guard = new ChallengeAuthenticator( null, ChallengeScheme.HTTP_BASIC, "testRealm" );
        MapVerifier mapVerifier = new MapVerifier();
        mapVerifier.getLocalSecrets().put( "rickard", "secret".toCharArray() );
        guard.setVerifier( mapVerifier );

        guard.setNext( restlet );

        server.setNext( guard );
        server.start();

        //START SNIPPET: client-create1
        Client client = new Client( Protocol.HTTP );

        ContextResourceClientFactory contextResourceClientFactory = module.newObject( ContextResourceClientFactory.class, client );
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
        Reference ref = new Reference( "http://localhost:8888/" );
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
        return applicationModel.newInstance( qi4j.api(), new MetadataService() );
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
                ValueBuilder<BuildSpec> builder = module.newValueBuilderWithPrototype( result );

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
            return module.newObject( RootResource.class, this );
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
