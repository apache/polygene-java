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

package org.apache.polygene.library.rdf.entity;

import java.io.PrintWriter;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityDescriptor;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.injection.scope.Uses;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.library.rdf.DcRdf;
import org.apache.polygene.library.rdf.PolygeneEntityType;
import org.apache.polygene.library.rdf.Rdfs;
import org.apache.polygene.library.rdf.serializer.RdfXmlSerializer;
import org.apache.polygene.spi.entitystore.EntityStore;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;


/**
 * JAVADOC
 */
public class EntityTypeSerializerTest
    extends AbstractPolygeneTest
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

    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        createDummyData();
    }

    @Test
    public void testEntityTypeSerializer() throws RDFHandlerException
    {

        EntityDescriptor entityDescriptor = module.entityDescriptor(TestEntity.class.getName());

        Iterable<Statement> graph = serializer.serialize( entityDescriptor );

        String[] prefixes = new String[]{ "rdf", "dc", " vc", "polygene" };
        String[] namespaces = new String[]{ Rdfs.RDF, DcRdf.NAMESPACE, "http://www.w3.org/2001/vcard-rdf/3.0#", PolygeneEntityType.NAMESPACE };

        new RdfXmlSerializer().serialize( graph, new PrintWriter( System.out ), prefixes, namespaces );
    }

    void createDummyData() throws UnitOfWorkCompletionException
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            ValueBuilder<Test2Value> vb2 = valueBuilderFactory.newValueBuilder( Test2Value.class );
            vb2.prototype().data().set( "Zout" );

            ValueBuilder<TestValue> valueBuilder = valueBuilderFactory.newValueBuilder( TestValue.class );
            valueBuilder.prototype().test1().set( 4L );
            valueBuilder.prototype().test3().set( vb2.newInstance() );
            TestValue testValue = valueBuilder.newInstance();

            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder(TestEntity.class, StringIdentity.identityOf( "test1" ) );
            TestEntity rickardTemplate = builder.instance();
            rickardTemplate.name().set( "Rickard" );
            rickardTemplate.title().set( "Mr" );
            rickardTemplate.value().set( testValue );
            TestEntity testEntity = builder.newInstance();

            EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder(TestEntity.class, StringIdentity.identityOf( "test2" ) );
            TestEntity niclasTemplate = builder2.instance();
            niclasTemplate.name().set( "Niclas" );
            niclasTemplate.title().set( "Mr" );
            niclasTemplate.association().set( testEntity );
            niclasTemplate.manyAssoc().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            niclasTemplate.group().add( 0, testEntity );
            valueBuilder = valueBuilderFactory.newValueBuilderWithPrototype( testValue );
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