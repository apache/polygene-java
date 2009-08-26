/*
 * Copyright 2009 Niclas Hedhman.
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
package org.qi4j.index.rdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilderFactory;
import static org.qi4j.api.query.QueryExpressions.orderBy;
import static org.qi4j.api.query.QueryExpressions.templateFor;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.index.rdf.assembly.NamedSparqlDescriptor;
import org.qi4j.index.rdf.assembly.RdfFactoryService;
import org.qi4j.index.rdf.model.Domain;
import org.qi4j.index.rdf.model.Female;
import org.qi4j.index.rdf.model.File;
import org.qi4j.index.rdf.model.Host;
import org.qi4j.index.rdf.model.Male;
import org.qi4j.index.rdf.model.Nameable;
import org.qi4j.index.rdf.model.Person;
import org.qi4j.index.rdf.model.Port;
import org.qi4j.index.rdf.model.Protocol;
import org.qi4j.index.rdf.model.QueryParam;
import org.qi4j.index.rdf.model.URL;
import org.qi4j.index.rdf.model.Address;
import org.qi4j.index.rdf.model.entities.AccountEntity;
import org.qi4j.index.rdf.model.entities.CatEntity;
import org.qi4j.index.rdf.model.entities.CityEntity;
import org.qi4j.index.rdf.model.entities.DomainEntity;
import org.qi4j.index.rdf.model.entities.FemaleEntity;
import org.qi4j.index.rdf.model.entities.MaleEntity;
import org.qi4j.library.rdf.entity.EntityStateSerializer;
import org.qi4j.library.rdf.entity.EntityTypeSerializer;
import org.qi4j.library.rdf.repository.MemoryRepositoryService;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.test.EntityTestAssembler;
import org.openrdf.rio.RDFFormat;

public class RdfNamedQueryTest
{

    private SingletonAssembler assembler;
    private QueryBuilderFactory qbf;
    private UnitOfWork unitOfWork;

    @Before
    public void setUp() throws UnitOfWorkCompletionException
    {
        assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module )
                throws AssemblyException
            {
                module.addEntities(
                    MaleEntity.class,
                    FemaleEntity.class,
                    CityEntity.class,
                    DomainEntity.class,
                    AccountEntity.class,
                    CatEntity.class
                );
                module.addValues(
                    URL.class,
                    Address.class,
                    Protocol.class,
                    Host.class,
                    Port.class,
                    File.class,
                    QueryParam.class
                );
                new EntityTestAssembler().assemble( module );
                module.addServices(
                    MemoryRepositoryService.class,
                    RdfFactoryService.class );
                NamedQueries namedQueries = new NamedQueries();
                for( int i = 0; i < query.length; i++ )
                {
                    addQuery( namedQueries, i );
                }
                module.addServices( RdfIndexerExporterComposite.class ).setMetaInfo( namedQueries );
                module.addObjects( EntityStateSerializer.class, EntityTypeSerializer.class );
            }

            private void addQuery( NamedQueries namedQueries, int index )
            {

                if( query[ index ].length() == 0 )
                {
                    return;
                }
                String queryName = String.format( "script%02d", index + 1 );
                namedQueries.addQuery( queryName, new NamedSparqlDescriptor( query[ index ] ) );
            }

        };
        Network.populate( assembler );
        unitOfWork = assembler.unitOfWorkFactory().newUnitOfWork();
        qbf = assembler.queryBuilderFactory();
    }

    @After
    public void tearDown()
    {
        if( unitOfWork != null )
        {
            unitOfWork.discard();
        }
    }

    @Test
    public void showNetwork()
        throws IOException
    {
        ServiceFinder serviceFinder = assembler.serviceFinder();
        RdfIndexerExporterComposite rdfIndexerExporter =
            serviceFinder.<RdfIndexerExporterComposite>findService( RdfIndexerExporterComposite.class ).get();
        rdfIndexerExporter.toRDF( System.out, RDFFormat.RDFXML );
    }

    private static void verifyUnorderedResults( final Iterable<? extends Nameable> results,
                                                final String... names )
    {
        final List<String> expected = new ArrayList<String>( Arrays.asList( names ) );

        for( Nameable entity : results )
        {
            String name = entity.name().get();
            assertTrue( name + " returned but not expected", expected.remove( name ) );
        }

        for( String notReturned : expected )
        {
            fail( notReturned + " was expected but not returned" );
        }

    }

    private static void verifyOrderedResults( final Iterable<? extends Nameable> results,
                                              final String... names )
    {
        final List<String> expected = new ArrayList<String>( Arrays.asList( names ) );
        final List<String> actual = new ArrayList<String>();
        for( Nameable result : results )
        {
            actual.add( result.name().get() );
        }

        assertThat( "Result is incorrect", actual, equalTo( expected ) );
    }

    @Test
    public void script01() throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script01" );
        System.out.println( "*** script01: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script02() throws EntityFinderException
    {
        final Query<Domain> query = qbf.newNamedQuery( Domain.class, unitOfWork, "script02" );
        System.out.println( "*** script02: " + query );
        verifyUnorderedResults( query, "Gaming" );
    }

    @Test
    public void script03() throws EntityFinderException
    {
        final Query<Nameable> query = qbf.newNamedQuery( Nameable.class, unitOfWork, "script03" );
        System.out.println( "*** script03: " + query );
        verifyUnorderedResults(
            query,
            "Joe Doe", "Ann Doe", "Jack Doe",
            "Penang", "Kuala Lumpur",
            "Cooking", "Gaming", "Programming", "Cars", "Felix"
        );
    }

    @Test
    public void script04() throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script04" );
        System.out.println( "*** script04: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script05() throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script05" );
        System.out.println( "*** script05: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script06() throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script06" );
        System.out.println( "*** script06: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script07() throws EntityFinderException
    {
        final Query<Nameable> query = qbf.newNamedQuery( Nameable.class, unitOfWork, "script07" );
        System.out.println( "*** script07: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script08() throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script08" );
        System.out.println( "*** script08: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Ann Doe" );
    }

    @Test
    public void script09() throws EntityFinderException
    {
        final Query<Female> query = qbf.newNamedQuery( Female.class, unitOfWork, "script09" );
        System.out.println( "*** script09: " + query );
        verifyUnorderedResults( query, "Ann Doe" );
    }

    @Test
    public void script10() throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script10" );
        System.out.println( "*** script10: " + query );
        verifyUnorderedResults( query, "Jack Doe", "Joe Doe" );
    }

    @Test
    public void script11() throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script11" );
        System.out.println( "*** script11: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script12() throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script12" );
        System.out.println( "*** script12: " + query );
        verifyUnorderedResults( query, "Ann Doe", "Jack Doe" );
    }

    @Test
    public void script13() throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script13" );
        System.out.println( "*** script13: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    public void script14() throws EntityFinderException
    {
        final Query<Male> query = qbf.newNamedQuery( Male.class, unitOfWork, "script14" );
        System.out.println( "*** script14: " + query );
        verifyUnorderedResults( query, "Joe Doe" );
    }

    @Test
    public void script15() throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script15" );
        System.out.println( "*** script15: " + query );
        verifyUnorderedResults( query, "Joe Doe", "Ann Doe" );
    }

    @Test
    public void script16() throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        final Query<Nameable> query = qbf.newNamedQuery( Nameable.class, unitOfWork, "script16" );
        query.orderBy( orderBy( nameable.name() ) );
        query.maxResults( 2 );
        System.out.println( "*** script16: " + query );
        verifyOrderedResults(
            query,
            "Ann Doe", "Cars"
        );
    }

    @Test
    public void script17() throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        final Query<Nameable> query = qbf.newNamedQuery( Nameable.class, unitOfWork, "script17" );
        query.orderBy( orderBy( nameable.name() ) );
        query.firstResult( 3 );
        query.maxResults( 3 );
        System.out.println( "*** script17: " + query );
        verifyOrderedResults(
            query,
            "Felix", "Gaming", "Jack Doe"
        );
    }

    @Test
    public void script18() throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        final Query<Nameable> query = qbf.newNamedQuery( Nameable.class, unitOfWork, "script18" );
        query.orderBy( orderBy( nameable.name() ) );
        System.out.println( "*** script18: " + query );
        verifyOrderedResults(
            query,
            "Ann Doe", "Cars", "Cooking", "Felix", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming"
        );
    }

    @Test
    public void script19() throws EntityFinderException
    {
        Nameable nameable = templateFor( Nameable.class );
        final Query<Nameable> query = qbf.newNamedQuery( Nameable.class, unitOfWork, "script19" );
        query.orderBy( orderBy( nameable.name() ) );
        System.out.println( "*** script19: " + query );
        verifyOrderedResults(
            query,
            "Felix", "Gaming", "Jack Doe", "Joe Doe", "Kuala Lumpur", "Penang", "Programming"
        );
    }

    @Test
    public void script20() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script20" );
        query.orderBy( orderBy( person.name(), OrderBy.Order.DESCENDING ) );
        System.out.println( "*** script20: " + query );
        verifyOrderedResults(
            query,
            "Joe Doe", "Ann Doe"
        );
    }

    @Test
    public void script21() throws EntityFinderException
    {
        Person person = templateFor( Person.class );
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script21" );
        query.orderBy( orderBy( person.placeOfBirth().get().name() ),
                       orderBy( person.yearOfBirth() ) );
        System.out.println( "*** script21: " + query );
        verifyOrderedResults(
            query,
            "Ann Doe", "Joe Doe", "Jack Doe"
        );
    }

    @Test
    public void script22()
        throws EntityFinderException
    {
        final Query<Nameable> query = qbf.newNamedQuery( Nameable.class, unitOfWork, "script22" );
        System.out.println( "*** script22: " + query );
        verifyUnorderedResults(
            query,
            "Jack Doe", "Joe Doe"
        );
    }

    @Ignore( "Skip this one for now. It sporadically fails sometimes." )
    @Test
    public void script23()
        throws EntityFinderException
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script23" );
        System.out.println( "*** script23: " + query );
        verifyOrderedResults( query, "Jack Doe" );
    }

    @Test
    public void script24() throws EntityFinderException
    {
        final Query<Domain> query = qbf.newNamedQuery( Domain.class, unitOfWork, "script24" );
        System.out.println( "*** script24: " + query );
        assertThat( query.find().name().get(), is( equalTo( "Gaming" ) ) );
    }

    @Test
    @Ignore( "Wait until indexing of complex values is implemented" )
    public void script29()
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script29" );
        System.out.println( "*** script29: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    @Ignore( "Wait till 0.7.0?" )
    public void script30()
    {
        final Query<Nameable> query = qbf.newNamedQuery( Nameable.class, unitOfWork, "script30" );
        System.out.println( "*** script30: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    @Test
    @Ignore( "Wait till 0.7.0?" )
    public void script31()
    {
        final Query<Person> query = qbf.newNamedQuery( Person.class, unitOfWork, "script31" );
        System.out.println( "*** script31: " + query );
        verifyUnorderedResults( query, "Jack Doe" );
    }

    private static String[] query =
        {
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "\n" +
            "}", //script01

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Domain>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:name ?v0. \n" +
            "FILTER (?v0 = \"Gaming\")\n" +
            "}", //script02

            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "\n" +
            "}", //script03

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns2: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:placeOfBirth ?v0. \n" +
            "?v0 ns2:name ?v1. \n" +
            "FILTER (?v1 = \"Kuala Lumpur\")\n" +
            "}", //script04

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns2: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:mother ?v0. \n" +
            "?v0 ns1:placeOfBirth ?v1. \n" +
            "?v1 ns2:name ?v2. \n" +
            "FILTER (?v2 = \"Kuala Lumpur\")\n" +
            "}", //script05

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:yearOfBirth ?v0. \n" +
            "FILTER (?v0 >= \"1973\")\n" +
            "}", //script06

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns2: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:yearOfBirth ?v0. \n" +
            "?entity ns1:placeOfBirth ?v1. \n" +
            "?v1 ns2:name ?v2. \n" +
            "FILTER ((?v0 >= \"1900\") && (?v2 = \"Penang\"))\n" +
            "}", //script07

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:yearOfBirth ?v0. \n" +
            "FILTER ((?v0 = \"1970\") || (?v0 = \"1975\"))\n" +
            "}", //script08

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Female>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:yearOfBirth ?v0. \n" +
            "FILTER ((?v0 = \"1970\") || (?v0 = \"1975\"))\n" +
            "}", //script09

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:yearOfBirth ?v0. \n" +
            "FILTER (!(?v0 = \"1975\"))\n" +
            "}", //script10

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "OPTIONAL {?entity ns1:email ?v0}. \n" +
            "FILTER (bound(?v0))\n" +
            "}", //script11

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "OPTIONAL {?entity ns1:email ?v0}. \n" +
            "FILTER (! bound(?v0))\n" +
            "}", //script12

            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Male#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "OPTIONAL {?entity ns1:wife ?v0}. \n" +
            "FILTER (bound(?v0))\n" +
            "}", //script13

            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Male#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Male>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "OPTIONAL {?entity ns1:wife ?v0}. \n" +
            "FILTER (! bound(?v0))\n" +
            "}", //script14

            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Male#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "OPTIONAL {?entity ns1:wife ?v0}. \n" +
            "FILTER (! bound(?v0))\n" +
            "}", //script15

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:name ?v0. \n" +
            "\n" +
            "} ORDER BY DESC (?v0)", //script16

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:name ?v0. \n" +
            "\n" +
            "}  ORDER BY DESC (?v0)", //script17

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:name ?v0. \n" +
            "\n" +
            "}\n" +
            "ORDER BY ASC(?v0)", //script18

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:name ?v0. \n" +
            "FILTER (?v0 > \"D\")\n" +
            "} ORDER BY ASC(?v0)", //script19

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns2: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:yearOfBirth ?v0. \n" +
            "?entity ns2:name ?v1. \n" +
            "FILTER (?v0 > \"1973\")\n" +
            "}\n" +
            "ORDER BY ASC(?v0)", //script20

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Person#> \n" +
            "PREFIX ns2: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Person>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:placeOfBirth ?v0. \n" +
            "?v0 ns2:name ?v1. \n" +
            "?entity ns1:yearOfBirth ?v2. \n" +
            "\n" +
            "}  ORDER BY DESC(?v1) DESC(?v2)", //script21

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:name ?v0. \n" +
            "FILTER regex(?v0,\"J.*Doe\")\n" +
            "}", //script22

            "", //script23

            "PREFIX ns1: <urn:qi4j:type:org.qi4j.index.rdf.model.Nameable#> \n" +
            "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT DISTINCT ?entityType ?identity\n" +
            "WHERE {\n" +
            "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.index.rdf.model.Domain>. \n" +
            "?entity rdf:type ?entityType. \n" +
            "?entity ns0:identity ?identity. \n" +
            "?entity ns1:name ?v0. \n" +
            "FILTER (?v0 = \"Gaming\")\n" +
            "}", //script24

            "", //script25
            "", //script26
            "", //script27
            "", //script28
            "", //script29
            "", //script30
            ""  //script31
        };
}