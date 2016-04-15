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

package org.apache.zest.library.rest.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.api.unitofwork.NoSuchEntityException;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.ApplicationAssemblerAdapter;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.test.AbstractZestTest;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class RestTest
    extends AbstractZestTest
{

    @Override
    protected ApplicationDescriptor newApplication()
        throws AssemblyException
    {
        return zest.newApplicationModel( new ApplicationAssemblerAdapter(
            new Assembler[][][]
                {
                    {
                        {
                            RestTest.this,
                            new RestAssembler(),
                            new RdfMemoryStoreAssembler(),
                            new DefaultUnitOfWorkAssembler()
                        }
                    }
                } )
        {}  // subclassing ApplicationAssemblerAdapter
        );
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.objects( RestTest.class, RestTester.class );
        module.entities( PersonEntity.class );
        module.services( RestServerComposite.class ).instantiateOnStartup();
        module.services( MemoryEntityStoreService.class ).identifiedBy( "store" );
        module.services( UuidIdentityGeneratorService.class );
    }

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            EntityBuilder<PersonEntity> builder1 = uow.newEntityBuilder( PersonEntity.class, "P2" );
            PersonEntity maryDoe = builder1.instance();
            maryDoe.firstname().set( "Mary" );
            maryDoe.lastname().set( "Doe" );
            maryDoe = builder1.newInstance();

            EntityBuilder<PersonEntity> builder2 = uow.newEntityBuilder( PersonEntity.class, "P1" );
            PersonEntity joeDoe = builder2.instance();
            joeDoe.firstname().set( "Joe" );
            joeDoe.lastname().set( "Doe" );
            joeDoe.mother().set( maryDoe );
            builder2.newInstance();

            uow.complete();
        }
        finally
        {
            uow.discard();
        }
    }

    @Test
    public void givenAnIdentityWhenExecutingGetCommandThenExpectTheCorrectRdf()
        throws Exception
    {
        RestTester restTester = objectFactory.newObject( RestTester.class );
        String rdf = restTester.getEntity( "P1" );
        // System.out.println( rdf.replaceAll( "\n", "\\\\n" ).replaceAll( "\"", "\\\\\"" ) );
        assertThat( "Incorrect RDF produced", rdf, anyOf(
            // Open JDK 8 & Valid
            equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n	xmlns:zest=\"http://zest.apache.org/rdf/model/1.0/\"\n	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n	xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<org.apache.zest.library.rest.admin.RestTest-PersonEntity xmlns=\"urn:zest:type:\" rdf:about=\"urn:zest:entity:P1\">\n	<lastname xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\">Doe</lastname>\n	<firstname xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\">Joe</firstname>\n	<identity xmlns=\"urn:zest:type:org.apache.zest.api.entity.Identity#\">P1</identity>\n	<mother xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\" rdf:resource=\"urn:zest:entity:P2\"/>\n</org.apache.zest.library.rest.admin.RestTest-PersonEntity>\n\n</rdf:RDF>" ),
            equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n	xmlns:zest=\"http://zest.apache.org/rdf/model/1.0/\"\n	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n	xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<org.apache.zest.library.rest.admin.RestTest-PersonEntity xmlns=\"urn:zest:type:\" rdf:about=\"urn:zest:entity:P1\">\n	<identity xmlns=\"urn:zest:type:org.apache.zest.api.entity.Identity#\">P1</identity>\n	<firstname xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\">Joe</firstname>\n	<lastname xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\">Doe</lastname>\n	<mother xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\" rdf:resource=\"urn:zest:entity:P2\"/>\n</org.apache.zest.library.rest.admin.RestTest-PersonEntity>\n\n</rdf:RDF>" ),
            // Sun JDK 6 / Oracle JDK 7 & Valid
            equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n	xmlns:zest=\"http://zest.apache.org/rdf/model/1.0/\"\n	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n	xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<org.apache.zest.library.rest.admin.RestTest-PersonEntity xmlns=\"urn:zest:type:\" rdf:about=\"urn:zest:entity:P1\">\n	<firstname xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\">Joe</firstname>\n	<lastname xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\">Doe</lastname>\n	<identity xmlns=\"urn:zest:type:org.apache.zest.api.entity.Identity#\">P1</identity>\n	<mother xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\" rdf:resource=\"urn:zest:entity:P2\"/>\n</org.apache.zest.library.rest.admin.RestTest-PersonEntity>\n\n</rdf:RDF>" ),
            // IBM JDK 6 & Valid
            equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n	xmlns:zest=\"http://zest.apache.org/rdf/model/1.0/\"\n	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n	xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<org.apache.zest.library.rest.admin.RestTest-PersonEntity xmlns=\"urn:zest:type:\" rdf:about=\"urn:zest:entity:P1\">\n	<identity xmlns=\"urn:zest:type:org.apache.zest.api.entity.Identity#\">P1</identity>\n	<lastname xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\">Doe</lastname>\n	<firstname xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\">Joe</firstname>\n	<mother xmlns=\"urn:zest:type:org.apache.zest.library.rest.admin.RestTest-Person#\" rdf:resource=\"urn:zest:entity:P2\"/>\n</org.apache.zest.library.rest.admin.RestTest-PersonEntity>\n\n</rdf:RDF>" ) ) );
    }

    @Test
    public void givenExistingIdentityWhenExecutingPutCommandThenNewValuesInEntity()
        throws Throwable
    {
        RestTester restTester = objectFactory.newObject( RestTester.class );
        Map<String, String> properties = new HashMap<String, String>();
        properties.put( "identity", "P1" );
        properties.put( "firstname", "Jack" );
        properties.put( "lastname", "Doe" );
        restTester.putEntity( "P1", properties );
        UnitOfWork work = uowf.newUnitOfWork();
        try
        {
            PersonEntity entity = work.get( PersonEntity.class, "P1" );
            assertEquals( "FirstName not changed.", "Jack", entity.firstname().get() );
            assertEquals( "LastName not changed.", "Doe", entity.lastname().get() );
            work.complete();
        }
        finally
        {
            work.discard();
        }
    }

    @Test
    public void givenExistingIdentityWhenExecutingDeleteCommandThenEntityIsRemoved()
        throws Throwable
    {
        RestTester restTester = objectFactory.newObject( RestTester.class );
        restTester.deleteEntity( "P1" );
        UnitOfWork work = uowf.newUnitOfWork();
        try
        {
            PersonEntity entity = null;
            try
            {
                entity = work.get( PersonEntity.class, "P1" );
            }
            catch( NoSuchEntityException expected )
            {
                // expected
            }
            assertNull( "Entity not removed.", entity );
            work.complete();
        }
        finally
        {
            work.discard();
        }
    }

    @Test
    public void givenExistingEntitiesWhenExecutingGetCommandThenExpectTheCorrectRdf()
        throws Exception
    {
        final RestTester restTester = objectFactory.newObject( RestTester.class );
        final String result = restTester.getEntities().replace( "\r", "" );
        assertThat(
            "Returned RDF", result,
            anyOf(
                equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n\txmlns=\"urn:zest:\"\n\txmlns:zest=\"http://zest.apache.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<zest:entity rdf:about=\"/entity/P2.rdf\"/>\n<zest:entity rdf:about=\"/entity/P1.rdf\"/>\n</rdf:RDF>\n" ),
                equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\\\"no\\\"?>\n<rdf:RDF\n\txmlns=\"urn:zest:\"\n\txmlns:zest=\"http://zest.apache.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<zest:entity rdf:about=\"/entity/P2.rdf\"/>\n<zest:entity rdf:about=\"/entity/P1.rdf\"/>\n</rdf:RDF>\n" ),
                equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rdf:RDF\n\txmlns=\"urn:zest:\"\n\txmlns:zest=\"http://zest.apache.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<zest:entity rdf:about=\"/entity/P1.rdf\"/>\n<zest:entity rdf:about=\"/entity/P2.rdf\"/>\n</rdf:RDF>\n" ),
                equalTo( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\\\"no\\\"?>\n<rdf:RDF\n\txmlns=\"urn:zest:\"\n\txmlns:zest=\"http://zest.apache.org/rdf/model/1.0/\"\n\txmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n\txmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">\n<zest:entity rdf:about=\"/entity/P1.rdf\"/>\n<zest:entity rdf:about=\"/entity/P2.rdf\"/>\n</rdf:RDF>\n" ) ) );
    }

    public static class RestTester
    {

        @Service
        private RestServer server;

        public String getEntity( String identity )
            throws IOException
        {
            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod( "http://localhost:8182/entity/" + identity + ".rdf" );
            method.addRequestHeader( "Accept", "application/rdf+xml" );
            try
            {
                int status = client.executeMethod( method );
                if( status != 200 )
                {
                    throw new RuntimeException( "EntityResource returned status code: '" + status + "' and message: '" + method
                        .getStatusText() + "'" );
                }
                InputStream input = method.getResponseBodyAsStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                copyStream( input, baos );
                return baos.toString( "UTF-8" );
            }
            finally
            {
                method.releaseConnection();
            }
        }

        public void putEntity( String identity, Map<String, String> params )
            throws IOException
        {
            HttpClient client = new HttpClient();
            PostMethod method = new PostMethod( "http://localhost:8182/entity/" + identity );
            for( Map.Entry<String, String> entry : params.entrySet() )
            {
                method.addParameter( entry.getKey(), entry.getValue() );
            }
            try
            {
                int status = client.executeMethod( method );
                if( status != 205 )
                {
                    throw new RuntimeException( "EntityResource returned status code: '" + status + "' and message: '" + method
                        .getStatusText() + "'" );
                }
            }
            finally
            {
                method.releaseConnection();
            }
        }

        public void deleteEntity( String identity )
            throws IOException
        {
            HttpClient client = new HttpClient();
            DeleteMethod method = new DeleteMethod( "http://localhost:8182/entity/" + identity );
            try
            {
                int status = client.executeMethod( method );
                if( status != 204 )
                {
                    throw new RuntimeException( "EntityResource returned status code: '" + status + "' and message: '" + method
                        .getStatusText() + "'" );
                }
            }
            finally
            {
                method.releaseConnection();
            }
        }

        public String getEntities()
            throws IOException
        {
            HttpClient client = new HttpClient();
            GetMethod method = new GetMethod( "http://localhost:8182/entity.rdf" );
            method.addRequestHeader( "Accept", "application/rdf+xml" );
            try
            {
                int status = client.executeMethod( method );
                if( status != 200 )
                {
                    throw new RuntimeException( "EntityResource returned status code: '" + status + "' and message: '" + method
                        .getStatusText() + "'" );
                }
                InputStream input = method.getResponseBodyAsStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                copyStream( input, baos );
                return baos.toString( "UTF-8" );
            }
            finally
            {
                method.releaseConnection();
            }
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

    public interface PersonEntity
        extends EntityComposite, Person
    {
    }

    public interface Person
    {

        Property<String> firstname();

        Property<String> lastname();

        @Optional
        Association<Person> mother();
    }
}
