/*
 * Copyright (c) 2009-2013, Niclas Hedhman. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
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
package org.qi4j.test.performance.indexing.rdf;

import java.io.File;
import org.apache.derby.iapi.services.io.FileUtil;
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
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreAssembler;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.index.rdf.indexing.RdfIndexingService;
import org.qi4j.index.rdf.query.SesameExpressions;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.test.EntityTestAssembler;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationAssembler;

import static org.qi4j.api.query.QueryExpressions.eq;
import static org.qi4j.api.query.QueryExpressions.templateFor;

@SuppressWarnings( "ResultOfMethodCallIgnored" )
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

    private Application application;
    private Module module;
    private static final String QUERY1 = "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
                                         + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
                                         + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                                         + "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.performance.indexing.rdf.QueryPerformanceTest-Lead#> \n"
                                         + "SELECT ?entityType ?identity\n"
                                         + "WHERE {\n"
                                         + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.performance.indexing.rdf.QueryPerformanceTest-Lead>. \n"
                                         + "?entity rdf:type ?entityType. \n"
                                         + "?entity ns0:identity ?identity. \n"
                                         + "?entity ns1:name \"Lead64532\". \n"
                                         + "}";
    private static final String QUERY2 = "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
                                         + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
                                         + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                                         + "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.performance.indexing.rdf.QueryPerformanceTest-Lead#> \n"
                                         + "SELECT ?entityType ?identity\n"
                                         + "WHERE {\n"
                                         + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.performance.indexing.rdf.QueryPerformanceTest-Lead>. \n"
                                         + "?entity rdf:type ?entityType. \n"
                                         + "?entity ns0:identity ?identity. \n"
                                         + "?entity ns1:name \"Lead98276\". \n"
                                         + "}";
    private static final String QUERY3 = "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
                                         + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
                                         + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                                         + "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.performance.indexing.rdf.QueryPerformanceTest-Lead#> \n"
                                         + "SELECT ?entityType ?identity\n"
                                         + "WHERE {\n"
                                         + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.performance.indexing.rdf.QueryPerformanceTest-Lead>. \n"
                                         + "?entity rdf:type ?entityType. \n"
                                         + "?entity ns0:identity ?identity. \n"
                                         + "?entity ns1:name \"Lead2\". \n"
                                         + "}";
    private static final String QUERY4 = "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
                                         + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
                                         + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                                         + "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.performance.indexing.rdf.QueryPerformanceTest-Lead#> \n"
                                         + "SELECT ?entityType ?identity\n"
                                         + "WHERE {\n"
                                         + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.performance.indexing.rdf.QueryPerformanceTest-Lead>. \n"
                                         + "?entity rdf:type ?entityType. \n"
                                         + "?entity ns0:identity ?identity. \n"
                                         + "?entity ns1:name \"Lead14332\". \n"
                                         + "}";
    private static final String QUERY5 = "PREFIX ns0: <urn:qi4j:type:org.qi4j.api.entity.Identity#> \n"
                                         + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
                                         + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                                         + "PREFIX ns1: <urn:qi4j:type:org.qi4j.test.performance.indexing.rdf.QueryPerformanceTest-Lead#> \n"
                                         + "SELECT ?entityType ?identity\n"
                                         + "WHERE {\n"
                                         + "?entityType rdfs:subClassOf <urn:qi4j:type:org.qi4j.test.performance.indexing.rdf.QueryPerformanceTest-Lead>. \n"
                                         + "?entity rdf:type ?entityType. \n"
                                         + "?entity ns0:identity ?identity. \n"
                                         + "?entity ns1:name \"Lead632\". \n"
                                         + "}";
    private File indexingDataDir;

    @Before
    public void setup()
        throws Exception
    {
        Energy4Java qi4j = new Energy4Java();
        application = qi4j.newApplication( this );
        module = application.findModule( LAYER_DOMAIN, MODULE_DOMAIN );
        application.activate();
        indexingDataDir = module.findService( RdfIndexingService.class ).get().dataDir();
    }

    @After
    public void tearDown()
        throws Exception
    {
        try
        {
            if( application != null )
            {
                System.out.println( "Shutting Down test." );
                application.passivate();
            }
        }
        finally
        {
            FileUtil.removeDirectory( indexingDataDir );
        }
    }

    @Override
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
    public void testIndexingFluentQueries()
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
    public void testIndexingNamedQueries()
        throws Exception
    {
        LeadRepository leadRepo = populateEntityStore();
        measureNamedQuery( leadRepo, QUERY1 );
        measureNamedQuery( leadRepo, QUERY2 );
        measureNamedQuery( leadRepo, QUERY3 );
        measureNamedQuery( leadRepo, QUERY4 );
        measureNamedQuery( leadRepo, QUERY5 );
    }

    private LeadRepository populateEntityStore()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = module.newUnitOfWork();
        try
        {
            LeadRepository leadRepo = module.findService( LeadRepositoryService.class ).get();
            if( leadRepo.findByName( "Lead99999" ) == null )
            {
                ServiceReference<LeadEntityFactoryService> leadFactoryRef = module.findService( LeadEntityFactoryService.class );
                LeadEntityFactory leadFactory = leadFactoryRef.get();
                long start, end;
                start = System.currentTimeMillis();
                for( int i = 1; i < NUMBER_OF_ENTITIES; i++ )
                {
                    if( ( i % 10000 ) == 0 )
                    {
                        System.out.print( "\r" + i );
                        uow.complete();
                        uow = module.newUnitOfWork();
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
        finally
        {
            if( uow != null && uow.isOpen() )
            {
                uow.discard();
            }
        }
    }

    private void measureFluentQuery( LeadRepository leadRepo, String nameOfEntity )
        throws Exception
    {
        long start;
        long end;
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            start = System.currentTimeMillis();
            Lead lead = leadRepo.findByName( nameOfEntity );
            end = System.currentTimeMillis();
            if( lead == null )
            {
                Assert.fail( "Entity was not found or more than one entity was found." );
                return;
            }
            System.out.println( "Lead: " + lead );
            System.out.println( "Retrieval time of " + lead.name().get() + " by name: " + ( end - start ) );
            uow.complete();
        }
    }

    private void measureNamedQuery( LeadRepository leadRepo, String queryName )
        throws Exception
    {
        long start;
        long end;
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            start = System.currentTimeMillis();
            Lead lead = leadRepo.findByFixedQuery( queryName );
            end = System.currentTimeMillis();
            if( lead == null )
            {
                Assert.fail( "Entity was not found or more than one entity was found." );
                return;
            }
            System.out.println( "Lead: " + lead );
            System.out.println( "Retrieval time of " + lead.name().get() + " by name: " + ( end - start ) );
            uow.complete();
        }
    }

    private LayerAssembly createDomainLayer( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly domainLayer = applicationAssembly.layer( LAYER_DOMAIN );
        ModuleAssembly domainModule = domainLayer.module( MODULE_DOMAIN );
        domainModule.addServices( LeadRepositoryService.class );
        domainModule.addServices( LeadEntityFactoryService.class );
        domainModule.entities( LeadEntity.class );
        return domainLayer;
    }

    private LayerAssembly createInfrastructureLayer( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly infrastructureLayer = applicationAssembly.layer( LAYER_INFRASTRUCTURE );

        // Persistence module
        ModuleAssembly persistenceModule = infrastructureLayer.module( MODULE_PERSISTENCE );

        // Indexing
        new RdfNativeSesameStoreAssembler().assemble( persistenceModule );

        // Entity store
        new OrgJsonValueSerializationAssembler().assemble( persistenceModule );
        new MemoryEntityStoreAssembler().visibleIn( Visibility.application ).assemble( persistenceModule );

        return infrastructureLayer;
    }

    private LayerAssembly createConfigurationLayer( ApplicationAssembly applicationAssembly )
        throws AssemblyException
    {
        LayerAssembly layer = applicationAssembly.layer( LAYER_CONFIGURATION );
        ModuleAssembly configModule = layer.module( MODULE_CONFIG );
        configModule.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
        new EntityTestAssembler().assemble( configModule );
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
        private Module module;

        @Override
        public Lead create( String name )
        {
            UnitOfWork uow = module.currentUnitOfWork();
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
        private Module module;

        @Override
        public Lead findByFixedQuery( String queryString )
        {
            UnitOfWork uow = module.currentUnitOfWork();
            Query<Lead> query = uow.newQuery( module.newQueryBuilder( Lead.class ).where( SesameExpressions.sparql( queryString ) ) );
            return query.find();
        }

        @Override
        public Lead findByName( String name )
        {
            UnitOfWork uow = module.currentUnitOfWork();
            QueryBuilder<Lead> builder = module.newQueryBuilder( Lead.class );
            Lead template = templateFor( Lead.class );

            Query<Lead> query = uow.newQuery( builder.where( eq( template.name(), name ) ) );
            return query.find();
        }
    }

}
