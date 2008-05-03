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
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.scope.Service;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.index.rdf.RDFQueryService;
import org.qi4j.entity.memory.IndexedMemoryEntityStoreService;
import org.qi4j.property.Property;
import org.qi4j.rest.assembly.RestAssembler;
import org.qi4j.runtime.structure.ApplicationContext;
import org.qi4j.runtime.structure.ApplicationInstance;
import org.qi4j.spi.entity.UuidIdentityGeneratorService;
import org.qi4j.test.AbstractQi4jTest;

public class RestTest extends AbstractQi4jTest
{

    protected ApplicationInstance newApplication()
        throws AssemblyException
    {
        Assembler[][][] assemblers = new Assembler[][][]
            {
                {
                    {
                        this,
                        new RestAssembler()
                    }
                }
            };
        ApplicationContext applicationContext = applicationFactory.newApplication( assemblers );
        return applicationContext.newApplicationInstance( "Test application" );
    }

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addObjects( RestTester.class );
        module.addComposites( TestEntity.class );
        module.addServices( IndexedMemoryEntityStoreService.class ).identifiedBy( "store" );
        module.addServices( RDFQueryService.class );
        module.addServices( UuidIdentityGeneratorService.class );
    }

    @Override
    @Before public void setUp() throws Exception
    {
        super.setUp();
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            TestEntity testEntity1 = uow.newEntity( "1234", TestEntity.class );
            testEntity1.firstname().set( "Niclas" );
            testEntity1.lastname().set( "Hedhman" );

            TestEntity testEntity2 = uow.newEntity( "5678", TestEntity.class );
            testEntity2.firstname().set( "Alin" );
            testEntity2.lastname().set( "Dreghiciu" );
            uow.complete();
        }
        catch( Exception e )
        {
            uow.discard();
            throw e;
        }
    }

    @Test
    public void givenAnIdentityWhenExecutingGetCommandThenExpectTheCorrectXml()
        throws Exception
    {
        RestTester restTester = objectBuilderFactory.newObject( RestTester.class );
        String xml = restTester.getEntity( "1234" );
        assertEquals( "Incorrect XML produced", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><entity><type>org.qi4j.rest.RestTest$TestEntity</type><identity>1234</identity><properties><identity>1234</identity><firstname>Niclas</firstname><lastname>Hedhman</lastname></properties></entity>", xml );
    }

    @Test
    public void givenExistingIdentityWhenExecutingPutCommandThenNewValuesInEntity()
        throws Exception
    {
        RestTester restTester = objectBuilderFactory.newObject( RestTester.class );
        restTester.putEntity( "1234", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><entity><identity>1234</identity><properties><identity>1234</identity><firstname>Rickard</firstname><lastname>Oberg</lastname></properties></entity>" );
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        try
        {
            TestEntity entity = work.find( "1234", TestEntity.class );
            assertEquals( "FirstName not changed.", "Rickard", entity.firstname().get() );
            assertEquals( "LastName not changed.", "Oberg", entity.lastname().get() );
            work.complete();
        }
        catch( Exception e )
        {
            work.discard();
        }
    }

    @Test
    public void givenExistingIdentityWhenExecutingDeleteCommandThenEntityIsRemoved()
        throws Exception
    {
        RestTester restTester = objectBuilderFactory.newObject( RestTester.class );
        restTester.deleteEntity( "1234" );
        UnitOfWork work = unitOfWorkFactory.newUnitOfWork();
        try
        {
            TestEntity entity = work.find( "1234", TestEntity.class );
            assertNull( "Entity not removed.", entity );
            work.complete();
        }
        catch( Exception e )
        {
            work.discard();
        }
    }

    @Test
    public void givenAnTypeWhenExecutingGetCommandThenExpectTheCorrectXml()
        throws Exception
    {
        final RestTester restTester = objectBuilderFactory.newObject( RestTester.class );
        final String result = restTester.getEntities( TestEntity.class );
        assertThat( "Returned XML",
                    result,
                    is( equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?><entities><entity href=\"/entity/org.qi4j.rest.RestTest$TestEntity/1234\">1234</entity><entity href=\"/entity/org.qi4j.rest.RestTest$TestEntity/5678\">5678</entity></entities>" ) ) );
    }

    public static class RestTester
    {
        @Service RestServer server;

        public String getEntity( String id )
            throws IOException
        {
            URL url = new URL( "http://localhost:8182/entity/" + TestEntity.class.getName() + "/" + id );
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
            method.setPath( "/entity/" + TestEntity.class.getName() + "/" + identity );
            client.executeMethod( host, method );
        }

        public void deleteEntity( String identity )
            throws IOException
        {
            HttpClient client = new HttpClient();
            DeleteMethod method = new DeleteMethod();
            HostConfiguration host = new HostConfiguration();
            host.setHost( "localhost", 8182, "http" );
            method.setPath( "/entity/" + TestEntity.class.getName() + "/" + identity );
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

    public interface TestEntity extends EntityComposite, TestType
    {
    }

    public interface TestType
    {
        Property<String> firstname();

        Property<String> lastname();
    }
}
