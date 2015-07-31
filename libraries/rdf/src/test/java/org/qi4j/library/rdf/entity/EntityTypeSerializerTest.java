/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.rdf.DcRdf;
import org.qi4j.library.rdf.Qi4jEntityType;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.spi.entitystore.EntityStore;
import org.qi4j.test.AbstractQi4jTest;

import java.io.PrintWriter;
import org.qi4j.test.EntityTestAssembler;


/**
 * JAVADOC
 */
public class EntityTypeSerializerTest
    extends AbstractQi4jTest
{
    @Service EntityStore entityStore;
    @Uses EntityTypeSerializer serializer;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( TestEntity.class );
        module.values( TestValue.class, Test2Value.class );
        module.objects( EntityTypeSerializer.class, EntityTypeSerializerTest.class );
    }

    @Override @Before
    public void setUp() throws Exception
    {
        super.setUp();

        createDummyData();

        module.injectTo( this );
    }

    @Test
    public void testEntityTypeSerializer() throws RDFHandlerException
    {

        EntityDescriptor entityDescriptor = module.entityDescriptor(TestEntity.class.getName());

        Iterable<Statement> graph = serializer.serialize( entityDescriptor );

        String[] prefixes = new String[]{ "rdf", "dc", " vc", "qi4j" };
        String[] namespaces = new String[]{ Rdfs.RDF, DcRdf.NAMESPACE, "http://www.w3.org/2001/vcard-rdf/3.0#", Qi4jEntityType.NAMESPACE };

        new RdfXmlSerializer().serialize( graph, new PrintWriter( System.out ), prefixes, namespaces );
    }

    void createDummyData() throws UnitOfWorkCompletionException
    {
        UnitOfWork unitOfWork = module.newUnitOfWork();
        try
        {
            ValueBuilder<Test2Value> vb2 = module.newValueBuilder( Test2Value.class );
            vb2.prototype().data().set( "Zout" );

            ValueBuilder<TestValue> valueBuilder = module.newValueBuilder( TestValue.class );
            valueBuilder.prototype().test1().set( 4L );
            valueBuilder.prototype().test3().set( vb2.newInstance() );
            TestValue testValue = valueBuilder.newInstance();

            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder(TestEntity.class, "test1");
            TestEntity rickardTemplate = builder.instance();
            rickardTemplate.name().set( "Rickard" );
            rickardTemplate.title().set( "Mr" );
            rickardTemplate.value().set( testValue );
            TestEntity testEntity = builder.newInstance();

            EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder(TestEntity.class, "test2");
            TestEntity niclasTemplate = builder2.instance();
            niclasTemplate.name().set( "Niclas" );
            niclasTemplate.title().set( "Mr" );
            niclasTemplate.association().set( testEntity );
            niclasTemplate.manyAssoc().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            valueBuilder = module.newValueBuilderWithPrototype( testValue );
            valueBuilder.prototype().test1().set( 5L );
            testValue = valueBuilder.newInstance();
            niclasTemplate.value().set( testValue );
            TestEntity testEntity2 = builder2.newInstance();
            unitOfWork.complete();
        }
        finally
        {
            unitOfWork.discard();
        }
    }
}