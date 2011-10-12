package org.qi4j.library.rest.client;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.service.importer.NewObjectImporter;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rest.client.api.ContextResourceClient;
import org.qi4j.library.rest.client.api.ContextResourceClientFactory;
import org.qi4j.library.rest.client.api.ErrorHandler;
import org.qi4j.library.rest.client.spi.NullResponseHandler;
import org.qi4j.library.rest.client.spi.ResponseHandler;
import org.qi4j.library.rest.client.spi.ResultHandler;
import org.qi4j.library.rest.common.Resource;
import org.qi4j.library.rest.common.ValueAssembler;
import org.qi4j.library.rest.server.api.ContextResource;
import org.qi4j.library.rest.server.api.ContextRestlet;
import org.qi4j.library.rest.server.api.InteractionConstraintsService;
import org.qi4j.library.rest.server.api.InteractionValidation;
import org.qi4j.library.rest.server.api.ObjectSelection;
import org.qi4j.library.rest.server.api.Requires;
import org.qi4j.library.rest.server.api.RequiresValid;
import org.qi4j.library.rest.server.api.SubResource;
import org.qi4j.library.rest.server.api.SubResources;
import org.qi4j.library.rest.server.api.dci.DeleteContext;
import org.qi4j.library.rest.server.api.dci.Role;
import org.qi4j.library.rest.server.assembler.RestServerAssembler;
import org.qi4j.library.rest.server.restlet.NullCommandResult;
import org.qi4j.library.rest.server.spi.CommandResult;
import org.qi4j.test.AbstractQi4jTest;
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

import static org.qi4j.bootstrap.ImportedServiceDeclaration.*;

/**
 * TODO
 */
public class ContextResourceClientFactoryTest
    extends AbstractQi4jTest
{
    private Server server;
    private ContextResourceClient crc;

    public static String command = null; // Commands will set this

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        // General setup of client and server
        new ClientAssembler().assemble( module );

        new ValueAssembler().assemble( module );
        new RestServerAssembler().assemble( module );

        module.objects( NullCommandResult.class );
        module.importedServices( CommandResult.class ).importedBy( NEW_OBJECT );

        module.importedServices( MetadataService.class ).importedBy( NEW_OBJECT );
        module.objects( MetadataService.class );

        // Test specific setup
        module.values( TestQuery.class, TestResult.class, TestCommand.class );
        module.forMixin( TestQuery.class ).declareDefaults().abc().set( "def" );

        module.objects( RootRestlet.class, RootResource.class, RootContext.class );


        module.objects( DescribableContext.class );
        module.transients( TestComposite.class );
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

        ChallengeAuthenticator guard = new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "testRealm");
        MapVerifier mapVerifier = new MapVerifier();
        mapVerifier.getLocalSecrets().put("rickard", "secret".toCharArray());
        guard.setVerifier(mapVerifier);

        guard.setNext(restlet);

        server.setNext( guard );
        server.start();

        Client client = new Client( Protocol.HTTP );
        Reference ref = new Reference( "http://localhost:8888/" );
        ContextResourceClientFactory contextResourceClientFactory = module.newObject( ContextResourceClientFactory.class, client, new NullResponseHandler() );
        contextResourceClientFactory.setAcceptedMediaTypes( MediaType.APPLICATION_JSON );

        contextResourceClientFactory.setErrorHandler( new ErrorHandler().onError( ErrorHandler.AUTHENTICATION_REQUIRED, new ResponseHandler()
        {
            boolean tried = false;

            @Override
            public void handleResponse( Response response, ContextResourceClient client )
            {
                    if (tried)
                        throw new ResourceException( response.getStatus() );

                    tried = true;
                    client.getContextResourceClientFactory().getInfo().setUser( new User("rickard", "secret") );

                    // Try again
                    client.refresh();
            }
        } ).onError( ErrorHandler.RECOVERABLE_ERROR, new ResponseHandler()
        {
            @Override
            public void handleResponse( Response response, ContextResourceClient client )
            {
                // Try to restart
                client.refresh();
            }
        } ) );

        crc = contextResourceClientFactory.newClient( ref );
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
    public void testQueryWithoutValue()
    {
        crc.onResource( new ResultHandler<Resource>()
        {
            @Override
            public void handleResult( Resource result, ContextResourceClient client )
            {
                client.query( "querywithoutvalue", null );
            }
        } ).
        onQuery( "querywithoutvalue", TestResult.class, new ResultHandler<TestResult>()
        {
            @Override
            public void handleResult( TestResult result, ContextResourceClient client )
            {
                Assert.assertThat( result.xyz().get(), CoreMatchers.equalTo( "bar" ) );
            }
        } );

        crc.refresh();
    }

    @Test
    public void testQueryAndCommand()
    {
        crc.onResource( new ResultHandler<Resource>()
        {
            @Override
            public void handleResult( Resource result, ContextResourceClient client )
            {
                client.query( "querywithvalue", null );
            }
        } ).onProcessingError( "querywithvalue", TestQuery.class, new ResultHandler<TestQuery>()
        {
            @Override
            public void handleResult( TestQuery result, ContextResourceClient client )
            {
                ValueBuilder<TestQuery> builder = module.newValueBuilderWithPrototype( result );

                builder.prototype().abc().set( "abc" + builder.prototype().abc().get() );

                client.query( "querywithvalue", builder.newInstance() );
            }
        } ).onQuery( "querywithvalue", TestResult.class, new ResultHandler<TestResult>()
        {
            @Override
            public void handleResult( TestResult result, ContextResourceClient client )
            {
                client.command( "commandwithvalue", null );
            }
        } ).onProcessingError( "commandwithvalue", Form.class, new ResultHandler<Form>()
        {
            @Override
            public void handleResult( Form result, ContextResourceClient client )
            {
                result.set( "abc", "right" );

                client.command( "commandwithvalue", result );
            }
        } );

        crc.refresh();
    }

    public interface TestQuery
        extends ValueComposite
    {
        @UseDefaults
        Property<String> abc();
    }

    public interface TestCommand
        extends ValueComposite
    {
        Property<String> abc();
    }

    public interface TestResult
        extends ValueComposite
    {
        Property<String> xyz();
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
        implements SubResources
    {
        private static TestComposite instance;

        public RootResource()
        {
            super( RootContext.class );
        }

        public TestResult querywithvalue( TestQuery testQuery )
            throws Throwable
        {
            return context( RootContext.class ).queryWithValue( testQuery );
        }

        public TestResult querywithoutvalue()
            throws Throwable
        {
            return context( RootContext.class ).queryWithoutValue();
        }

        public String querywithstringresult( TestQuery query )
            throws Throwable
        {
            return context( RootContext.class ).queryWithStringResult( query );
        }

        public void commandwithvalue( TestCommand command )
            throws Throwable
        {
            context( RootContext.class ).commandWithValue( command );
        }

        public void resource( String currentSegment )
        {
            ObjectSelection objectSelection = ObjectSelection.current();

            objectSelection.select( new File( "" ) );

            if( instance == null )
            {
                objectSelection.select( instance = module.newTransient( TestComposite.class ) );
            }
            else
            {
                objectSelection.select( instance );
            }

            subResource( SubResource1.class );
        }
    }

    public static class SubResource1
        extends ContextResource
    {
        public SubResource1()
        {
            super( SubContext.class, SubContext2.class, DescribableContext.class );
        }

        public TestResult genericquery( TestQuery query )
            throws Throwable
        {
            return context( SubContext2.class ).genericQuery( query );
        }

        public TestResult querywithvalue( TestQuery query )
            throws Throwable
        {
            return context( SubContext.class ).queryWithValue( query );
        }

        @SubResource
        public void subresource1()
        {
            subResource( SubResource1.class );
        }

        @SubResource
        public void subresource2()
        {
            subResource( SubResource1.class );
        }
    }

    public static class RootContext
        implements DeleteContext
    {
        private static int count = 0;

        @Structure
        Module module;

        public TestResult queryWithValue( TestQuery query )
        {
            return module.newValueFromJSON( TestResult.class, "{'xyz':'"+query.abc().get()+"'}" );
        }

        public TestResult queryWithoutValue()
        {
            return module.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
        }

        public String queryWithStringResult( TestQuery query )
        {
            return "bar";
        }

        public int queryWithIntegerResult( TestQuery query )
        {
            return 7;
        }

        public void commandWithValue( TestCommand command )
        {
            if( !command.abc().get().equals( "right" ) )
            {
                throw new IllegalArgumentException( "Wrong argument" );
            }

            // Done
        }

        public void idempotentCommandWithValue( TestCommand command )
            throws ConcurrentEntityModificationException
        {
            // On all but every third invocation, throw a concurrency exception
            // This is to test retries on the server-side
            count++;
            if( count % 3 != 0 )
            {
                module.currentUnitOfWork().addUnitOfWorkCallback( new UnitOfWorkCallback()
                {
                    public void beforeCompletion()
                        throws UnitOfWorkCompletionException
                    {
                        throw new ConcurrentEntityModificationException( Collections.<EntityComposite>emptyList() );
                    }

                    public void afterCompletion( UnitOfWorkStatus status )
                    {
                    }
                } );
            }

            if( !command.abc().get().equals( "right" ) )
            {
                throw new IllegalArgumentException( "Wrong argument" );
            }

            // Done
        }

        public void delete()
            throws ResourceException, IOException
        {
            // Ok!
            command = "delete";
        }
    }

    public static class SubContext
        implements InteractionValidation
    {
        @Structure
        Module module;

        public TestResult queryWithValue( TestQuery query )
        {
            return module.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
        }

        // Test interaction constraints

        @Requires( File.class )
        public TestResult queryWithRoleRequirement( TestQuery query )
        {
            return module.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
        }

        @Requires( File.class )
        public void commandWithRoleRequirement()
        {
        }

        // Interaction validation
        private static boolean xyzValid = true;

        @RequiresValid( "xyz" )
        public void xyz( @Name( "valid" ) boolean valid )
        {
            xyzValid = valid;
        }

        @RequiresValid( "notxyz" )
        public void notxyz( @Name( "valid" ) boolean valid )
        {
            xyzValid = valid;
        }

        public boolean isValid( String name )
        {
            if( name.equals( "xyz" ) )
            {
                return xyzValid;
            }
            else if( name.equals( "notxyz" ) )
            {
                return !xyzValid;
            }
            else
            {
                return false;
            }
        }
    }

    public static class SubContext2
    {
        @Structure
        Module module;

        public TestResult genericQuery( TestQuery query )
        {
            return module.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
        }
    }

    public static class DescribableContext
    {
        @Structure
        Module module;

        Describable describable = new Describable();

        public void bind( @Uses DescribableData describableData )
        {
            describable.bind( describableData );
        }

        public String description()
        {
            return describable.description();
        }

        public void changeDescription( @Name( "description" ) String newDesc )
        {
            describable.changeDescription( newDesc );
        }

        public static class Describable
            extends Role<DescribableData>
        {
            public void changeDescription( String newDesc )
            {
                self.description().set( newDesc );
            }

            public String description()
            {
                return self.description().get();
            }
        }
    }

    public interface DescribableData
    {
        @UseDefaults
        Property<String> description();
    }

    public interface TestComposite
        extends TransientComposite, DescribableData
    {
        @Optional
        Property<String> foo();
    }
}
