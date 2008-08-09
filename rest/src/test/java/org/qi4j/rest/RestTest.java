/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.qi4j.entity.memory.IndexedMemoryEntityStoreService;
import org.qi4j.injection.scope.Service;
import org.qi4j.property.Property;
import org.qi4j.rest.assembly.RestAssembler;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.structure.Application;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.library.http.Servlets;

public class RestTest extends AbstractQi4jTest
{

    protected Application newApplication()
        throws AssemblyException
    {
        Assembler[][][] assemblers = new Assembler[][][]
            {
                {
                    {
                        this,
                        new RestAssembler(),
                        new RdfMemoryStoreAssembler()
                    }
                }
            };
        return qi4j.newApplication( assemblers );
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addObjects( RestTester.class );
        module.addEntities( PersonEntity.class );
        module.addServices( RestServerComposite.class ).instantiateOnStartup();
        module.addServices( IndexedMemoryEntityStoreService.class ).identifiedBy( "store" );
        module.addServices( UuidIdentityGeneratorService.class );
    }

    @Override
    @Before public void setUp() throws Exception
    {
        super.setUp();
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity maryDoe = uow.newEntity( "P2", PersonEntity.class );
            maryDoe.firstname().set( "Mary" );
            maryDoe.lastname().set( "Doe" );

            PersonEntity joeDoe = uow.newEntity( "P1", PersonEntity.class );
            joeDoe.firstname().set( "Joe" );
            joeDoe.lastname().set( "Doe" );
            joeDoe.mother().set( maryDoe );

            uow.complete();
        }
        catch( Exception e )
        {
            uow.discard();
            throw e;
        }
    }

    @Test @Ignore
    public void givenAnIdentityWhenExecutingGetCommandThenExpectTheCorrectXml()
        throws Exception
    {
        RestTester restTester = objectBuilderFactory.newObject( RestTester.class );
        String xml = restTester.getEntity( "P1" );
        assertEquals( "Incorrect XML produced", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><entity><type>org.qi4j.rest.RestTest$PersonEntity</type><identity>P1</identity><properties><identity>P1</identity><firstname>Joe</firstname><lastname>Doe</lastname></properties><associations><mother href=\"/entity/org.qi4j.rest.RestTest$PersonEntity/P2\">P2</mother></associations></entity>", xml );
    }

    @Test @Ignore
    public void givenExistingIdentityWhenExecutingPutCommandThenNewValuesInEntity()
        throws Throwable
    {
        RestTester restTester = objectBuilderFactory.newObject( RestTester.class );
        restTester.putEntity( "P1", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><entity><identity>P1</identity><properties><identity>P1</identity><firstname>Jack</firstname><lastname>Doe</lastname></properties></entity>" );
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity entity = work.find( "P1", PersonEntity.class );
            assertEquals( "FirstName not changed.", "Jack", entity.firstname().get() );
            assertEquals( "LastName not changed.", "Doe", entity.lastname().get() );
            work.complete();
        }
        catch( Throwable e )
        {
            work.discard();
            throw e;
        }
    }

    @Test  @Ignore
    public void givenExistingIdentityWhenExecutingDeleteCommandThenEntityIsRemoved()
        throws Throwable
    {
        RestTester restTester = objectBuilderFactory.newObject( RestTester.class );
        restTester.deleteEntity( "P1" );
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        try
        {
            PersonEntity entity = null;
            try
            {
                entity = work.find( "P1", PersonEntity.class );
            }
            catch( EntityCompositeNotFoundException expected )
            {
                // expected
            }
            assertNull( "Entity not removed.", entity );
            work.complete();
        }
        catch( Throwable e )
        {
            work.discard();
            throw e;
        }
    }

    @Test  @Ignore
    public void givenAnTypeWhenExecutingGetCommandThenExpectTheCorrectXml()
        throws Exception
    {
        final RestTester restTester = objectBuilderFactory.newObject( RestTester.class );
        final String result = restTester.getEntities( PersonEntity.class );
        assertThat(
            "Returned XML", result,
            anyOf(
                equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?><entities><entity href=\"/entity/org.qi4j.rest.RestTest$PersonEntity/P1\">P1</entity><entity href=\"/entity/org.qi4j.rest.RestTest$PersonEntity/P2\">P2</entity></entities>" ),
                equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><entities><entity href=\"/entity/org.qi4j.rest.RestTest$PersonEntity/P1\">P1</entity><entity href=\"/entity/org.qi4j.rest.RestTest$PersonEntity/P2\">P2</entity></entities>" ) )
        );
    }

    public static class RestTester
    {
        @Service RestServer server;

        public String getEntity( String id )
            throws IOException
        {
            URL url = new URL( "http://localhost:8182/entity/" + PersonEntity.class.getName() + "/" + id );
            InputStream in = (InputStream) url.getContent();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copyStream( in, baos );
            in.close();
            baos.close();
            return baos.toString();
        }

        public void putEntity( String identity, String xml )
            throws IOException
        {
            HttpClient client = new HttpClient();
            PutMethod method = new PutMethod();
            HostConfiguration host = new HostConfiguration();
            host.setHost( "localhost", 8182, "http" );
            RequestEntity entity = new StringRequestEntity( xml, "text/xml", "UTF-8" );
            method.setRequestEntity( entity );
            method.setPath( "/entity/" + PersonEntity.class.getName() + "/" + identity );
            client.executeMethod( host, method );
        }

        public void deleteEntity( String identity )
            throws IOException
        {
            HttpClient client = new HttpClient();
            DeleteMethod method = new DeleteMethod();
            HostConfiguration host = new HostConfiguration();
            host.setHost( "localhost", 8182, "http" );
            method.setPath( "/entity/" + PersonEntity.class.getName() + "/" + identity );
            client.executeMethod( host, method );
        }

        public String getEntities( Class type )
            throws IOException
        {
            URL url = new URL( "http://localhost:8182/entity/" + type.getName() );
            InputStream in = (InputStream) url.getContent();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copyStream( in, baos );
            in.close();
            baos.close();
            return baos.toString();
        }

        private void copyStream( InputStream in, OutputStream baos )
            throws IOException
        {
            int data = in.read();
            while( data != -1 )
            {
                baos.write( data );
                data = in.read();
            }
        }
    }

    public interface PersonEntity extends EntityComposite, Person
    {
    }

    public interface Person
    {
        Property<String> firstname();

        Property<String> lastname();

        Association<Person> mother();
    }
}
