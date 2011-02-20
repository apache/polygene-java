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

package org.qi4j.indexing;

import java.io.File;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.jdbm.JdbmConfiguration;
import org.qi4j.entitystore.jdbm.assembly.JdbmEntityStoreAssembler;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.index.rdf.indexing.RdfIndexingService;
import org.qi4j.index.rdf.query.NamedSparqlDescriptor;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.spi.query.NamedQueries;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.test.EntityTestAssembler;

import static org.qi4j.api.query.QueryExpressions.*;

public class QueryPerformanceTest
    implements ApplicationAssembler
{
    private static final int NUMBER_OF_ENTITIES = 100000;
    private static final String LAYER_INFRASTRUCTURE = "LAYER_INFRASTRUCTURE";
    private static final String MODULE_PERSISTENCE = "MODULE_PERSISTENCE";
    private static final String LAYER_CONFIGURATION = "CONFIGURATION";
    private static final String LAYER_DOMAIN = "LAYER_DOMAIN";
    private static final String MODULE_DOMAIN = "MODULE_DOMAIN";
    private static final String MODULE_CONFIG = "MODULE_CONFIG";

    private ApplicationSPI application;
    private ServiceFinder serviceLocator;
    private UnitOfWorkFactory unitOfWorkFactory;
    private static final String QUERY1 = "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
                                         "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                                         "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                                         "PREFIX ns1: <urn:qi4j:type:org.qi4j.indexing.QueryPerformanceTest-Lead#> \n" +
                                         "SELECT ?entityType ?identity\n" +
                                         "WHERE {\n" +
                                         "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.indexing.QueryPerformanceTest-Lead>. \n" +
                                         "?entity rdf:type ?entityType. \n" +
                                         "?entity ns0:identity ?identity. \n" +
                                         "?entity ns1:name \"Lead64532\". \n" +
                                         "}";
    private static final String QUERY2 = "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
                                         "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                                         "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                                         "PREFIX ns1: <urn:qi4j:type:org.qi4j.indexing.QueryPerformanceTest-Lead#> \n" +
                                         "SELECT ?entityType ?identity\n" +
                                         "WHERE {\n" +
                                         "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.indexing.QueryPerformanceTest-Lead>. \n" +
                                         "?entity rdf:type ?entityType. \n" +
                                         "?entity ns0:identity ?identity. \n" +
                                         "?entity ns1:name \"Lead98276\". \n" +
                                         "}";
    private static final String QUERY3 = "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
                                         "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                                         "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                                         "PREFIX ns1: <urn:qi4j:type:org.qi4j.indexing.QueryPerformanceTest-Lead#> \n" +
                                         "SELECT ?entityType ?identity\n" +
                                         "WHERE {\n" +
                                         "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.indexing.QueryPerformanceTest-Lead>. \n" +
                                         "?entity rdf:type ?entityType. \n" +
                                         "?entity ns0:identity ?identity. \n" +
                                         "?entity ns1:name \"Lead2\". \n" +
                                         "}";
    private static final String QUERY4 = "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
                                         "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                                         "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                                         "PREFIX ns1: <urn:qi4j:type:org.qi4j.indexing.QueryPerformanceTest-Lead#> \n" +
                                         "SELECT ?entityType ?identity\n" +
                                         "WHERE {\n" +
                                         "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.indexing.QueryPerformanceTest-Lead>. \n" +
                                         "?entity rdf:type ?entityType. \n" +
                                         "?entity ns0:identity ?identity. \n" +
                                         "?entity ns1:name \"Lead14332\". \n" +
                                         "}";
    private static final String QUERY5 = "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n" +
                                         "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                                         "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                                         "PREFIX ns1: <urn:qi4j:type:org.qi4j.indexing.QueryPerformanceTest-Lead#> \n" +
                                         "SELECT ?entityType ?identity\n" +
                                         "WHERE {\n" +
                                         "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.indexing.QueryPerformanceTest-Lead>. \n" +
                                         "?entity rdf:type ?entityType. \n" +
                                         "?entity ns0:identity ?identity. \n" +
                                         "?entity ns1:name \"Lead632\". \n" +
                                         "}";
    private File indexingDataDir;
    private Module module;

    @Before
    public void setup()
        throws Exception
    {
        try
        {
            Energy4Java qi4j = new Energy4Java();
            application = qi4j.newApplication( this );
            module = application.findModule( LAYER_DOMAIN, MODULE_DOMAIN );
            serviceLocator = module.serviceFinder();
            unitOfWorkFactory = module.unitOfWorkFactory();
            application.activate();
            ServiceReference<RdfIndexingService> indexer =
                serviceLocator.<RdfIndexingService>findService( RdfIndexingService.class );

            indexingDataDir = indexer.get().dataDir();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown()
        throws Exception
    {
        if( application != null )
        {
            System.out.println( "Shutting Down test." );
            application.passivate();
        }
        new File( "target/jdbmstore.data.db" ).delete();
        new File( "target/jdbmstore.data.log" ).delete();

        delete( indexingDataDir );
    }

    private void delete( File f )
    {
        if( f == null )
        {
            return;
        }
        File[] files = f.listFiles();
        if( files == null )
        {
            return;
        }
        for( File file : files )
        {
            if( file.isDirectory() )

            {
                delete( file );
            }
            else
            {
                file.delete();
            }
        }
        f.delete();
    }

    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException
    {
        ApplicationAssembly applicationAssembly = applicationFactory.newApplicationAssembly();
        LayerAssembly infra = createInfrastructureLayer( applicationAssembly );
        LayerAssembly domain = createDomainLayer( applicationAssembly );
        LayerAssembly config = createConfigurationLayer( applicationAssembly );
        infra.uses( config );
        domain.uses( infra );
        return applicationAssembly;
    }

    @Test
    public void testIndexing1()
        throws Exception
    {
        LeadRepository leadRepo = populateEntityStore();
        measureFluentQuery( leadRepo, "Lead64531" );
        measureFluentQuery( leadRepo, "Lead98275" );
        measureFluentQuery( leadRepo, "Lead3" );
        measureFluentQuery( leadRepo, "Lead14331" );
        measureFluentQuery( leadRepo, "Lead631" );
    }

    @Test
    public void testIndexing2()
        throws Exception
    {
        LeadRepository leadRepo = populateEntityStore();
        measureNamedQuery( leadRepo, "1" );
        measureNamedQuery( leadRepo, "2" );
        measureNamedQuery( leadRepo, "3" );
        measureNamedQuery( leadRepo, "4" );
        measureNamedQuery( leadRepo, "5" );
    }

    private LeadRepository populateEntityStore()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        ServiceReference<LeadRepository> leadRepoRef = serviceLocator.findService( LeadRepositoryService.class );
        LeadRepository leadRepo = leadRepoRef.get();
        if( leadRepo.findByName( "Lead99999" ) == null )
        {
            ServiceReference<LeadEntityFactoryService> leadFactoryRef = serviceLocator.findService( LeadEntityFactoryService.class );
            LeadEntityFactory leadFactory = leadFactoryRef.get();
            long start, end;
            start = System.currentTimeMillis();
            for( int i = 1; i < NUMBER_OF_ENTITIES; i++ )
            {
                if( ( i % 10000 ) == 0 )
                {
                    System.out.print( "\r" + i );
                    uow.complete();
                    uow = unitOfWorkFactory.newUnitOfWork();
                }
                leadFactory.create( "Lead" + i );
            }
            System.out.println();
            uow.complete();
            end = System.currentTimeMillis();
            System.out.println( "Population time: " + ( end - start ) );
        }
        return leadRepo;
    }

    private void measureFluentQuery( LeadRepository leadRepo, String nameOfEntity )
        throws Exception
    {
        UnitOfWork uow;
        long start;
        long end;
        uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            start = System.currentTimeMillis();
            Lead lead = leadRepo.findByName( nameOfEntity );
            end = System.currentTimeMillis();
            if( lead == null )
            {
                Assert.fail( "Entity was not found or more than one entity was found." );
            }
            System.out.println( "Lead: " + lead );
            System.out.println( "Retrieval time of " + lead.name().get() + " by name: " + ( end - start ) );
            uow.complete();
        }
        catch( Exception e )
        {
            uow.discard();
            throw e;
        }
    }

    private void measureNamedQuery( LeadRepository leadRepo, String queryName )
        throws Exception
    {
        UnitOfWork uow;
        long start;
        long end;
        uow = unitOfWorkFactory.newUnitOfWork();
        try
        {
            start = System.currentTimeMillis();
            Lead lead = leadRepo.findByFixedQuery( queryName );
            end = System.currentTimeMillis();
            if( lead == null )
            {
                Assert.fail( "Entity was not found or more than one entity was found." );
            }
            System.out.println( "Lead: " + lead );
            System.out.println( "Retrieval time of " + lead.name().get() + " by name: " + ( end - start ) );
            uow.complete();
        }
        catch( Exception e )
        {
            uow.discard();
            throw e;
        }
    }

    private LayerAssembly createDomainLayer( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly domainLayer = applicationAssembly.layerAssembly( LAYER_DOMAIN );
        ModuleAssembly module = domainLayer.moduleAssembly( MODULE_DOMAIN );
        module.addServices( LeadRepositoryService.class );
        module.addServices( LeadEntityFactoryService.class );
        module.addEntities( LeadEntity.class );
        return domainLayer;
    }

    private LayerAssembly createInfrastructureLayer( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly infrastructureLayer = applicationAssembly.layerAssembly( LAYER_INFRASTRUCTURE );

        // Persistence module
        ModuleAssembly module = infrastructureLayer.moduleAssembly( MODULE_PERSISTENCE );

        // Indexing

        NamedQueries namedQueries = new NamedQueries();
        namedQueries.addQuery( new NamedSparqlDescriptor( "1", QUERY1 ) );
        namedQueries.addQuery( new NamedSparqlDescriptor( "2", QUERY2 ) );
        namedQueries.addQuery( new NamedSparqlDescriptor( "3", QUERY3 ) );
        namedQueries.addQuery( new NamedSparqlDescriptor( "4", QUERY4 ) );
        namedQueries.addQuery( new NamedSparqlDescriptor( "5", QUERY5 ) );

        new RdfNativeSesameStoreAssembler( namedQueries ).assemble( module );

        // Entity store
        new JdbmEntityStoreAssembler( Visibility.application ).assemble( module );

        return infrastructureLayer;
    }

    private LayerAssembly createConfigurationLayer( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly layer = applicationAssembly.layerAssembly( LAYER_CONFIGURATION );
        ModuleAssembly module = layer.moduleAssembly( MODULE_CONFIG );
        module.addEntities( NativeConfiguration.class ).visibleIn( Visibility.application );
        module.addEntities( JdbmConfiguration.class ).visibleIn( Visibility.application );
        new EntityTestAssembler().assemble( module );
        return layer;
    }

    public interface Lead
    {
        Property<String> name();
    }

    public interface LeadEntity
        extends Lead, EntityComposite
    {
    }

    @Mixins( LeadEntityFactoryMixin.class )
    public interface LeadEntityFactoryService
        extends LeadEntityFactory, ServiceComposite
    {
    }

    public interface LeadEntityFactory
    {
        Lead create( String name );
    }

    public static class LeadEntityFactoryMixin
        implements LeadEntityFactory
    {
        @Structure
        UnitOfWorkFactory uowf;

        public Lead create( String name )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            EntityBuilder<LeadEntity> builder = uow.newEntityBuilder( LeadEntity.class );
            Lead prototype = builder.instanceFor( LeadEntity.class );
            prototype.name().set( name );
            return builder.newInstance();
        }
    }

    public interface LeadRepository
    {
        Lead findByFixedQuery( String name );

        Lead findByName( String name );
    }

    @Mixins( LeadRepositoryMixin.class )
    public interface LeadRepositoryService
        extends LeadRepository, ServiceComposite
    {
    }

    public static class LeadRepositoryMixin
        implements LeadRepository
    {
        @Structure
        private UnitOfWorkFactory uowf;

        @Structure
        private QueryBuilderFactory qbf;

        public Lead findByFixedQuery( String name )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Query<Lead> query = qbf.newNamedQuery( Lead.class, uow, name );
            return query.find();
        }

        public Lead findByName( String name )
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            QueryBuilder<Lead> builder = qbf.newQueryBuilder( Lead.class );
            Lead template = templateFor( Lead.class );

            Query<Lead> query = builder.where( eq( template.name(), name ) ).newQuery( uow );
            return query.find();
        }
    }
}