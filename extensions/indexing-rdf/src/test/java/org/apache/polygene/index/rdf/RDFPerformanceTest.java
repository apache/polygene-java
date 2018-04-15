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

package org.apache.polygene.index.rdf;

/**
 * JAVADOC
 */

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.api.entity.EntityComposite;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.query.Query;
import org.apache.polygene.api.query.QueryExpressions;
import org.apache.polygene.api.time.SystemTime;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.Assembler;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationAssembler;
import org.apache.polygene.library.fileconfig.FileConfigurationOverride;
import org.apache.polygene.library.rdf.repository.NativeConfiguration;
import org.apache.polygene.spi.query.IndexExporter;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFPerformanceTest extends AbstractPolygeneTest
{
    private static final Logger LOG = LoggerFactory.getLogger( RDFPerformanceTest.class );
    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

    public interface ExampleEntity extends EntityComposite
    {
        @UseDefaults
        Property<String> someProperty();

        ManyAssociation<ExampleEntity> manyAssoc();
    }

    @Override
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );
        ModuleAssembly prefModule = module.layer().module( "PrefModule" );
        prefModule.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
        prefModule.forMixin( NativeConfiguration.class ).declareDefaults()
                  .tripleIndexes().set( "spoc,cspo" );
        prefModule.forMixin( NativeConfiguration.class ).declareDefaults()
                  .dataDirectory().set( new File( tmpDir.getRoot(), "rdf-data" ).getAbsolutePath() );
        new EntityTestAssembler().assemble( prefModule );

        module.entities( ExampleEntity.class );

        EntityTestAssembler testAss = new EntityTestAssembler();
        testAss.assemble( module );

        Assembler rdfAssembler = new RdfNativeSesameStoreAssembler();
        rdfAssembler.assemble( module );
    }


    private List<ExampleEntity> doCreate( int howMany )
    {
        List<ExampleEntity> result = new ArrayList<ExampleEntity>( howMany );

        List<ExampleEntity> entities = new ArrayList<ExampleEntity>();
        for (Integer x = 0; x < howMany; ++x)
        {
            ExampleEntity exampleEntity = this.unitOfWorkFactory.currentUnitOfWork().newEntity( ExampleEntity.class, StringIdentity.identityOf( "entity" + x ) );

            for (ExampleEntity entity : entities)
            {
                exampleEntity.manyAssoc().add( entity );
            }

            entities.add( exampleEntity );
            if (entities.size() > 10)
                entities.remove( 0 );

            result.add( exampleEntity );
        }

        return result;
    }

    private void doRemoveAll( List<ExampleEntity> entities )
    {
        for (ExampleEntity entity : entities)
        {
            this.unitOfWorkFactory.currentUnitOfWork().remove( this.unitOfWorkFactory.currentUnitOfWork().get( entity ) );
        }
    }

    private List<ExampleEntity> doList( int howMany )
    {
        List<ExampleEntity> list = new ArrayList<ExampleEntity>();
        UnitOfWork uow = this.unitOfWorkFactory.newUnitOfWork();
        Iterator<ExampleEntity> iter = uow.newQuery( this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class ) ).iterator();
        int found = 0;
        while (iter.hasNext())
        {
            found++;
            ExampleEntity exampleEntity = iter.next();
            if (exampleEntity != null)
                list.add( exampleEntity );
        }

        uow.discard();

        if (found != howMany)
        {
            LOG.warn( "Found " + found + " entities instead of " + howMany + "." );
        }

        return list;
    }


    private void doRemove( int howMany )
    {
        Iterator<ExampleEntity> iter = this.unitOfWorkFactory.currentUnitOfWork().newQuery( this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class )).maxResults( howMany ).iterator();
        Integer removed = 0;
        while (iter.hasNext())
        {
            this.unitOfWorkFactory.currentUnitOfWork().remove( iter.next() );
            ++removed;
        }

        if (removed != howMany)
        {
            LOG.warn( "Removed " + removed + " entities instead of " + howMany + "." );
        }
    }

    private void performTest( int howMany ) throws Exception
    {
        Instant startTest = SystemTime.now();

        UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
        Instant startingTime = SystemTime.now();
        List<ExampleEntity> entities = this.doCreate( howMany );
        LOG.info( "Time to create " + howMany + " entities: " + Duration.between(startingTime, SystemTime.now() ) );

        startingTime = SystemTime.now();
        creatingUOW.complete();
        LOG.info( "Time to complete creation uow: " + Duration.between(startingTime, SystemTime.now() ) );


        List<ExampleEntity> entityList = this.doList( howMany );

        startingTime = SystemTime.now();
        UnitOfWork uow = this.unitOfWorkFactory.newUnitOfWork();
        for (int i = 0; i < 1000; i++)
        {
            ExampleEntity entity50 = uow.get(ExampleEntity.class, StringIdentity.identityOf( "entity50" ) );
            Query<ExampleEntity> query = uow.newQuery( this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class ).
                    where( QueryExpressions.contains( QueryExpressions.templateFor( ExampleEntity.class ).manyAssoc(), entity50) ));
            System.out.println(query.count());
        }

        Instant endTest = SystemTime.now();
        LOG.info( "Time to query " + howMany + " entities: " + Duration.between(startTest, endTest) );

        UnitOfWork deletingUOW = this.unitOfWorkFactory.newUnitOfWork();
        startingTime = SystemTime.now();
        this.doRemoveAll( entityList );
//      this.doRemove(200);
        LOG.info( "Time to delete " + howMany + " entities: " + Duration.between(startingTime, SystemTime.now() ) );

        startingTime = SystemTime.now();
        deletingUOW.complete();

        endTest = SystemTime.now();
        LOG.info( "time to complete deletion uow: " + Duration.between(startingTime, endTest ) );
        LOG.info( "time to complete test: " + Duration.between(startingTime, endTest ) );

    }

    @Test
    public void dummy()
    {
        // Dummy test to make Maven happy
    }

    // @Test
    public void performanceTest200() throws Exception
    {
        this.performTest( 200 );
        this.performTest( 200 );
        this.performTest( 200 );
        this.performTest( 200 );
        this.performTest( 200 );
        this.performTest( 200 );
        this.performTest( 200 );
        this.performTest( 200 );

        IndexExporter indexerExporter =
                serviceFinder.findService( IndexExporter.class ).get();
        indexerExporter.exportReadableToStream( System.out );
    }

    @Ignore
    @Test
    public void performanceTest1000() throws Exception
    {
        this.performTest( 1000 );
    }

    @Ignore
    @Test
    public void performanceTest5000() throws Exception
    {
        this.performTest( 5000 );
    }

    @Ignore
    @Test
    public void performanceTest10000() throws Exception
    {
        this.performTest( 10000 );
    }

    @Ignore
    @Test
    public void performanceTest100000() throws Exception
    {
        this.performTest( 100000 );
    }
}
