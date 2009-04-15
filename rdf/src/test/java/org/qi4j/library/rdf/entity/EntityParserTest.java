/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.rdf.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.helpers.EntityTypeRegistryService;
import org.qi4j.test.AbstractQi4jTest;

import java.util.Collections;

/**
 * JAVADOC
 */
public class EntityParserTest
    extends AbstractQi4jTest
{
    @Service private EntityStore entityStore;
    @Uses private EntityStateSerializer serializer;
    @Uses private EntityStateParser parser;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( MemoryEntityStoreService.class, EntityTypeRegistryService.class );
        module.addEntities( TestEntity.class );
        module.addValues( TestValue.class );
        module.addObjects( EntityStateSerializer.class, EntityStateParser.class, EntityParserTest.class );
    }

    @Override @Before public void setUp() throws Exception
    {
        super.setUp();

        createDummyData();
    }

    @Test
    public void testEntityParser() throws RDFHandlerException
    {
        objectBuilderFactory.newObjectBuilder( EntityParserTest.class ).injectTo( this );

        EntityReference entityReference = new EntityReference( "test2" );
        EntityState entityState = entityStore.getEntityState(entityReference);

        Iterable<Statement> graph = serializer.serialize( entityState );

        parser.parse( graph, entityState );

        entityStore.prepare( Collections.EMPTY_LIST, Collections.singleton( entityState ), Collections.EMPTY_LIST ).commit();

        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            TestEntity entity = unitOfWork.get(TestEntity.class, "test1");
            TestEntity entity2 = unitOfWork.get(TestEntity.class, "test2");
            assertThat( "values are ok", entity2.name().get(), equalTo( "Niclas" ) );
            assertThat( "values are ok", entity2.association().get(), equalTo( entity ) );
            // TODO test that Value Composites are parsed correctly
            final Property<Long> longProperty = entity2.value().get().test1();
            final Property<String> stringProperty = entity2.value().get().test2();
            unitOfWork.complete();
        }
        catch( Exception e )
        {
            unitOfWork.discard();
        }
    }

    void createDummyData()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<TestValue> valueBuilder = valueBuilderFactory.newValueBuilder( TestValue.class );
        valueBuilder.prototype().test1().set( 4L );
        TestValue testValue = valueBuilder.newInstance();

        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder(TestEntity.class, "test1");
        builder.prototype().name().set( "Rickard" );
        builder.prototype().title().set( "Developer" );
        builder.prototype().value().set( testValue );
        TestEntity testEntity = builder.newInstance();

        EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder(TestEntity.class, "test2");
        builder2.prototype().name().set( "Niclas" );
        builder2.prototype().title().set( "Developer" );
        builder2.prototype().association().set( testEntity );
        builder2.prototype().manyAssoc().add( 0, testEntity );
        builder2.prototype().group().add( 0, testEntity );
        builder2.prototype().group().add( 0, testEntity );
        builder2.prototype().group().add( 0, testEntity );
        builder2.prototype().value().set( testValue );
        builder2.newInstance();
        unitOfWork.complete();
    }
}