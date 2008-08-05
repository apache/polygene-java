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

import java.io.PrintWriter;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.injection.scope.Service;
import org.qi4j.library.constraints.annotation.NotEmpty;
import org.qi4j.library.rdf.serializer.RdfXmlSerializer;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class EntitySerializerTest
    extends AbstractQi4jTest
{
    @Service EntitySerializer serializer;
    @Service EntityParser parser;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( MemoryEntityStoreService.class, EntitySerializerService.class, EntityParserService.class );
        module.addEntities( TestEntity.class );
        module.addObjects( EntitySerializerTest.class );
    }

    @Override @Before public void setUp() throws Exception
    {
        super.setUp();

        createDummyData();
    }

    @Test
    public void testEntitySerializer() throws RDFHandlerException
    {
        objectBuilderFactory.newObjectBuilder( EntitySerializerTest.class ).injectTo( this );

        Iterable<Statement> graph = serializer.serialize( new QualifiedIdentity( "test1", TestEntity.class.getName() ) );

        new RdfXmlSerializer().serialize( graph, new PrintWriter( System.out ) );

        parser.parse( graph );
    }

    void createDummyData()
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( "test1", TestEntity.class );
            builder.stateOfComposite().name().set( "Rickard" );
            TestEntity testEntity = builder.newInstance();
            unitOfWork.complete();
        }
        catch( Exception e )
        {
            unitOfWork.discard();
        }

    }

    public interface TestEntity
        extends EntityComposite
    {
        @NotEmpty Property<String> name();
    }
}
