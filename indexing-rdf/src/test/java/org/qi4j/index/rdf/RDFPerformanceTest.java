/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.index.rdf;

/**
 * JAVADOC
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.property.Property;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.entitystore.prefs.assembly.PreferenceEntityStoreAssembler;
import org.qi4j.index.rdf.assembly.RdfNativeSesameStoreAssembler;
import org.qi4j.library.fileconfig.FileConfiguration;
import org.qi4j.library.rdf.repository.NativeConfiguration;
import org.qi4j.spi.query.IndexExporter;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFPerformanceTest extends AbstractQi4jTest
{
    private static Logger _log = LoggerFactory.getLogger( RDFPerformanceTest.class );

    public interface ExampleEntity extends EntityComposite
    {
        @UseDefaults
        Property<String> someProperty();

        ManyAssociation<ExampleEntity> manyAssoc();
    }

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.services( FileConfiguration.class );
        ModuleAssembly prefModule = module.layer().module( "PrefModule" );
        prefModule.entities( NativeConfiguration.class ).visibleIn( Visibility.application );
        prefModule.forMixin( NativeConfiguration.class ).declareDefaults().tripleIndexes().set( "spoc,cspo" );
        prefModule.services( MemoryEntityStoreService.class );

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
            ExampleEntity exampleEntity = this.unitOfWorkFactory.currentUnitOfWork().newEntity( ExampleEntity.class, "entity" + x );

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
        Iterator<ExampleEntity> iter = this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class ).newQuery( uow ).iterator();
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
            _log.warn( "Found " + found + " entities instead of " + howMany + "." );
        }

        return list;
    }


    private void doRemove( int howMany )
    {
        Iterator<ExampleEntity> iter = this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class ).newQuery( this.unitOfWorkFactory.currentUnitOfWork() ).maxResults( howMany ).iterator();
        Integer removed = 0;
        while (iter.hasNext())
        {
            this.unitOfWorkFactory.currentUnitOfWork().remove( iter.next() );
            ++removed;
        }

        if (removed != howMany)
        {
            _log.warn( "Removed " + removed + " entities instead of " + howMany + "." );
        }
    }

    private void performTest( int howMany ) throws Exception
    {
        long startTest = System.currentTimeMillis();

        UnitOfWork creatingUOW = this.unitOfWorkFactory.newUnitOfWork();
        Long startingTime = System.currentTimeMillis();
        List<ExampleEntity> entities = this.doCreate( howMany );
        _log.info( "Time to create " + howMany + " entities (ms): " + (System.currentTimeMillis() - startingTime) );

        startingTime = System.currentTimeMillis();
        creatingUOW.complete();
        _log.info( "Time to complete creation uow (ms): " + (System.currentTimeMillis() - startingTime) );


        List<ExampleEntity> entityList = null;
        entityList = this.doList( howMany );

        startingTime = System.currentTimeMillis();
        UnitOfWork uow = this.unitOfWorkFactory.newUnitOfWork();
        for (int i = 0; i < 1000; i++)
        {
            Query<ExampleEntity> query = this.queryBuilderFactory.newQueryBuilder( ExampleEntity.class ).
                    where( QueryExpressions.contains( QueryExpressions.templateFor( ExampleEntity.class ).manyAssoc(), uow.get( ExampleEntity.class, "entity50" ) ) ).newQuery( uow );
            System.out.println(query.count());
        }

        long endTest = System.currentTimeMillis();
        _log.info( "Time to query " + howMany + " entities (ms): " + (endTest - startingTime) );

        UnitOfWork deletingUOW = this.unitOfWorkFactory.newUnitOfWork();
        startingTime = System.currentTimeMillis();
        this.doRemoveAll( entityList );
//      this.doRemove(200);
        _log.info( "Time to delete " + howMany + " entities (ms): " + (System.currentTimeMillis() - startingTime) );

        startingTime = System.currentTimeMillis();
        deletingUOW.complete();

        endTest = System.currentTimeMillis();

        _log.info( "time to complete deletion uow (ms): " + (endTest - startingTime) );

        _log.info( "time to complete test (ms): " + (endTest - startTest) );

    }

    @Test
    public void dummy()
    {
        // Dummy test to make Maven happy
    }

    //    @Test
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
                serviceLocator.<IndexExporter>findService( IndexExporter.class ).get();
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
