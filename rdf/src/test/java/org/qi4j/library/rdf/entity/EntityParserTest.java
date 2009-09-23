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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.spi.entitystore.EntityStoreUnitOfWork;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

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
        new EntityTestAssembler().assemble( module );
        module.addEntities( TestEntity.class );
        module.addValues( TestValue.class, Test2Value.class );
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
        Usecase usecase = UsecaseBuilder.newUsecase( "Test" );
        EntityStoreUnitOfWork work = entityStore.newUnitOfWork( usecase, moduleInstance );
        EntityState entityState = work.getEntityState( entityReference );

        Iterable<Statement> graph = serializer.serialize( entityState );

        parser.parse( graph, entityState );

        work.apply().commit();

        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            TestEntity entity = unitOfWork.get( TestEntity.class, "test1" );
            TestEntity entity2 = unitOfWork.get( TestEntity.class, "test2" );
            assertThat( "values are ok", entity2.name().get(), equalTo( "Niclas" ) );
            assertThat( "values are ok", entity2.association().get(), equalTo( entity ) );
            final Property<Long> longProperty = entity2.value().get().test1();
            assertThat( "values are ok", longProperty.get(), equalTo( 4L ) );
            final Property<String> stringProperty = entity2.value().get().test2();
            assertThat( "values are ok", stringProperty.get(), equalTo( null ) );
            final Property<Test2Value> valueProperty = entity2.value().get().test3();
            final Property<String> dataProperty = valueProperty.get().data();
            assertThat( "values are ok", dataProperty.get(), equalTo( "Zout" ) );
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
        ValueBuilder<Test2Value> vb2 = valueBuilderFactory.newValueBuilder( Test2Value.class );
        vb2.prototype().data().set( "Zout" );

        ValueBuilder<TestValue> valueBuilder = valueBuilderFactory.newValueBuilder( TestValue.class );
        valueBuilder.prototype().test1().set( 4L );
        valueBuilder.prototype().test3().set( vb2.newInstance() );
        TestValue testValue = valueBuilder.newInstance();

        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( TestEntity.class, "test1" );
        builder.instance().name().set( "Rickard" );
        builder.instance().title().set( "Developer" );
        builder.instance().value().set( testValue );
        TestEntity testEntity = builder.newInstance();

        EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder( TestEntity.class, "test2" );
        builder2.instance().name().set( "Niclas" );
        builder2.instance().title().set( "Developer" );
        builder2.instance().association().set( testEntity );
        builder2.instance().manyAssoc().add( 0, testEntity );
        builder2.instance().group().add( 0, testEntity );
        builder2.instance().group().add( 0, testEntity );
        builder2.instance().group().add( 0, testEntity );
        builder2.instance().value().set( testValue );
        builder2.newInstance();
        unitOfWork.complete();
    }
}