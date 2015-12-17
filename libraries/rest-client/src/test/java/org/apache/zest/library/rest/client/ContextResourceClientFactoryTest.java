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
package org.apache.zest.library.rest.client;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.apache.zest.api.type.HasTypes;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.api.value.ValueBuilderFactory;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.common.UseDefaults;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.constraint.Name;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.Uses;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.ConcurrentEntityModificationException;
import org.apache.zest.api.unitofwork.UnitOfWorkCallback;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.library.rest.client.api.ContextResourceClient;
import org.apache.zest.library.rest.client.api.ContextResourceClientFactory;
import org.apache.zest.library.rest.client.api.ErrorHandler;
import org.apache.zest.library.rest.client.api.HandlerCommand;
import org.apache.zest.library.rest.client.spi.ResponseHandler;
import org.apache.zest.library.rest.client.spi.ResultHandler;
import org.apache.zest.library.rest.common.Resource;
import org.apache.zest.library.rest.common.ValueAssembler;
import org.apache.zest.library.rest.common.link.Link;
import org.apache.zest.library.rest.common.link.Links;
import org.apache.zest.library.rest.common.link.LinksBuilder;
import org.apache.zest.library.rest.common.link.LinksUtil;
import org.apache.zest.library.rest.server.api.ContextResource;
import org.apache.zest.library.rest.server.api.ContextRestlet;
import org.apache.zest.library.rest.server.api.ObjectSelection;
import org.apache.zest.library.rest.server.api.ResourceDelete;
import org.apache.zest.library.rest.server.api.ResourceIndex;
import org.apache.zest.library.rest.server.api.SubResource;
import org.apache.zest.library.rest.server.api.SubResources;
import org.apache.zest.library.rest.server.api.constraint.InteractionValidation;
import org.apache.zest.library.rest.server.api.constraint.Requires;
import org.apache.zest.library.rest.server.api.constraint.RequiresValid;
import org.apache.zest.library.rest.server.api.dci.Role;
import org.apache.zest.library.rest.server.assembler.RestServerAssembler;
import org.apache.zest.library.rest.server.restlet.NullCommandResult;
import org.apache.zest.library.rest.server.spi.CommandResult;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationAssembler;
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

import static org.apache.zest.bootstrap.ImportedServiceDeclaration.*;
import static org.apache.zest.library.rest.client.api.HandlerCommand.*;

public class ContextResourceClientFactoryTest
    extends AbstractZestTest
{
    private Server server;
    private ContextResourceClient crc;

    public static String command = null; // Commands will set this

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
        module.values( TestQuery.class, TestResult.class, TestCommand.class );
        module.forMixin( TestQuery.class ).declareDefaults().abc().set( "def" );

        module.objects( RootRestlet.class, RootResource.class, RootContext.class, SubResource1.class, PagesResource.class );


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
        ContextRestlet restlet = objectFactory.newObject( ContextRestlet.class, new org.restlet.Context() );

        ChallengeAuthenticator guard = new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "testRealm");
        MapVerifier mapVerifier = new MapVerifier();
        mapVerifier.getLocalSecrets().put("rickard", "secret".toCharArray());
        guard.setVerifier(mapVerifier);

        guard.setNext(restlet);

        server.setNext( guard );
        server.start();

        //START SNIPPET: client-create1
        Client client =   new Client( Protocol.HTTP );

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
                    if (tried)
                        throw new ResourceException( response.getStatus() );

                    tried = true;
                    client.getContextResourceClientFactory().getInfo().setUser( new User("rickard", "secret") );

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
        return applicationModel.newInstance( zest.api(), new MetadataService() );
    }

    @Test
    public void testQueryWithoutValue()
    {
        //START SNIPPET: query-without-value
        crc.onResource( new ResultHandler<Resource>()
        {
            @Override
            public HandlerCommand handleResult( Resource result, ContextResourceClient client )
            {
                return query( "querywithoutvalue" );
            }
        } ).
        onQuery( "querywithoutvalue", new ResultHandler<TestResult>()
        {
            @Override
            public HandlerCommand handleResult( TestResult result, ContextResourceClient client )
            {
                Assert.assertThat( result.xyz().get(), CoreMatchers.equalTo( "bar" ) );
                return null;
            }
        } );

        crc.start();
        //END SNIPPET: query-without-value
    }

    @Test
    public void testQueryAndCommand()
    {
        //START SNIPPET: query-and-command
        crc.onResource( new ResultHandler<Resource>()
        {
            @Override
            public HandlerCommand handleResult( Resource result, ContextResourceClient client )
            {
                return query( "querywithvalue", null );
            }
        } ).onProcessingError( "querywithvalue", new ResultHandler<TestQuery>()
        {
            @Override
            public HandlerCommand handleResult( TestQuery result, ContextResourceClient client )
            {
                ValueBuilder<TestQuery> builder = valueBuilderFactory.newValueBuilderWithPrototype( result );

                builder.prototype().abc().set( "abc" + builder.prototype().abc().get() );

                return query( "querywithvalue", builder.newInstance() );
            }
        } ).onQuery( "querywithvalue", new ResultHandler<TestResult>()
        {
            @Override
            public HandlerCommand handleResult( TestResult result, ContextResourceClient client )
            {
                return command( "commandwithvalue", null );
            }
        } ).onProcessingError( "commandwithvalue", new ResultHandler<Form>()
        {
            @Override
            public HandlerCommand handleResult( Form result, ContextResourceClient client )
            {
                result.set( "abc", "right" );

                return command( "commandwithvalue", result );
            }
        } );

        crc.start();
        //END SNIPPET: query-and-command
    }

    @Test
    public void testQueryListAndCommand()
    {
        //START SNIPPET: query-list-and-command
        crc.onResource( new ResultHandler<Resource>()
        {
            @Override
            public HandlerCommand handleResult( Resource result, ContextResourceClient client )
            {
                return query( "commandwithvalue" );
            }
        } ).onQuery( "commandwithvalue", new ResultHandler<Links>()
        {
            @Override
            public HandlerCommand handleResult( Links result, ContextResourceClient client )
            {
                Link link = LinksUtil.withId( "right", result );

                return command( link );
            }
        } ).onCommand( "commandwithvalue", new ResponseHandler()
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
    public void testQueryListAndCommandProgressive()
    {
        //START SNIPPET: query-list-and-command-progressive
        crc.onResource( new ResultHandler<Resource>()
        {
            @Override
            public HandlerCommand handleResult( Resource result, ContextResourceClient client )
            {
                return query( "commandwithvalue" ).onSuccess( new ResultHandler<Links>()
                {
                    @Override
                    public HandlerCommand handleResult( Links result, ContextResourceClient client )
                    {
                        Link link = LinksUtil.withId( "right", result );

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

    @Test
    public void testIndexedResource()
    {
        crc.newClient("subcontext/pages/").onResource( new ResultHandler<Resource>()
        {
            @Override
            public HandlerCommand handleResult( Resource result, ContextResourceClient client )
            {
                return query( "index" );
            }
        } ).onQuery( "index", new ResultHandler<Links>()
                {
                    @Override
                    public HandlerCommand handleResult( Links result, ContextResourceClient client )
                    {
                        Assert.assertEquals( result.links().get().size(), 3 );
                        return null;
                    }
                } )
        .start();

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
        Property<String> entity();
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
            return objectFactory.newObject( RootResource.class, this );
        }
    }

    public static class RootResource
        extends ContextResource
        implements SubResources, ResourceDelete
    {
        private static TestComposite instance;

        private RootContext rootContext()
        {
            return context( RootContext.class );
        }

        public RootResource()
        {
        }

        public TestResult querywithvalue( TestQuery testQuery )
            throws Throwable
        {
            return rootContext().queryWithValue( testQuery );
        }

        public TestResult querywithoutvalue()
            throws Throwable
        {
            return rootContext().queryWithoutValue();
        }

        public String querywithstringresult( TestQuery query )
            throws Throwable
        {
            return rootContext().queryWithStringResult( query );
        }

        public void commandwithvalue( TestCommand command )
            throws Throwable
        {
            rootContext().commandWithValue( command );
        }

        public Links commandwithvalue()
        {
            return new LinksBuilder(module).
                command( "commandwithvalue" ).
                addLink( "Command ABC","right" ).
                addLink( "Command XYZ", "wrong" ).newLinks();
        }

        @Override
        public void delete()
            throws IOException
        {
            rootContext().delete();
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
        implements InteractionValidation
    {
        public SubResource1()
        {
        }

        @Requires( File.class )
        public void commandWithRoleRequirement()
        {
            context( SubContext.class ).commandWithRoleRequirement();
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

        @Requires( File.class )
        public TestResult queryWithRoleRequirement(TestQuery query)
        {
            return context( SubContext.class ).queryWithRoleRequirement( query );
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

        @SubResource
        public void pages() {
            subResource( PagesResource.class );
        }
    }

    public static class RootContext
    {
        private static int count = 0;

        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        public TestResult queryWithValue( TestQuery query )
        {
            return vbf.newValueFromSerializedState( TestResult.class, "{'xyz':'"+query.abc().get()+"'}" );
        }

        public TestResult queryWithoutValue()
        {
            return vbf.newValueFromSerializedState( TestResult.class, "{'xyz':'bar'}" );
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
            if( !command.entity().get().equals( "right" ) )
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
                uowf.currentUnitOfWork().addUnitOfWorkCallback( new UnitOfWorkCallback()
                {
                    public void beforeCompletion()
                        throws UnitOfWorkCompletionException
                    {
                        throw new ConcurrentEntityModificationException( Collections.<EntityComposite, HasTypes>emptyMap(),
                                                                         UsecaseBuilder.newUsecase( "Testing" ) );
                    }

                    public void afterCompletion( UnitOfWorkStatus status )
                    {
                    }
                } );
            }

            if( !command.entity().get().equals( "right" ) )
            {
                throw new IllegalArgumentException( "Wrong argument" );
            }

            // Done
        }

        public void delete()
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
            return module.newValueFromSerializedState( TestResult.class, "{'xyz':'bar'}" );
        }

        // Test interaction constraints

        @Requires( File.class )
        public TestResult queryWithRoleRequirement( TestQuery query )
        {
            return module.newValueFromSerializedState( TestResult.class, "{'xyz':'bar'}" );
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
            return module.newValueFromSerializedState( TestResult.class, "{'xyz':'bar'}" );
        }
    }

    public static class PagesResource extends ContextResource
        implements ResourceIndex<Links>
    {
        @Override
        public Links index()
        {
            return new LinksBuilder(module)
                    .addLink( "Page1", "page1")
                    .addLink( "Page2", "page2")
                    .addLink( "Page3", "page3")
                    .newLinks();
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
