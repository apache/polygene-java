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
package org.apache.polygene.runtime.entity;

import java.util.Collections;
import java.util.stream.Stream;
import org.apache.polygene.api.association.Association;
import org.apache.polygene.api.association.ManyAssociation;
import org.apache.polygene.api.association.NamedAssociation;
import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.entity.EntityReference;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * EntityBuilder With State Test.
 */
public class EntityBuilderWithStateTest
    extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );
        module.entities( SomeEntity.class );
    }

    @Test
    public void test()
        throws UnitOfWorkCompletionException
    {
        final Identity associatedIdentity;
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            EntityBuilder<SomeEntity> builder = uow.newEntityBuilder( SomeEntity.class );
            builder.instance().prop().set( "Associated" );
            SomeEntity entity = builder.newInstance();
            associatedIdentity = entity.identity().get();
            uow.complete();
        }
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            SomeEntity entity = uow.newEntityBuilderWithState(
                SomeEntity.class,
                descriptor -> {
                    if( "prop".equals( descriptor.qualifiedName().name() ) )
                    {
                        return "Foo";
                    }
                    return null;
                },
                descriptor -> {
                    if( "ass".equals( descriptor.qualifiedName().name() ) )
                    {
                        return EntityReference.create( associatedIdentity );
                    }
                    return null;
                },
                descriptor -> {
                    if( "manyAss".equals( descriptor.qualifiedName().name() ) )
                    {
                        return Stream.of( EntityReference.create( associatedIdentity ) );
                    }
                    return null;
                },
                descriptor -> {
                    if( "namedAss".equals( descriptor.qualifiedName().name() ) )
                    {
                        return Collections.singletonMap(
                            "foo",
                            EntityReference.create( associatedIdentity )
                        ).entrySet().stream();
                    }
                    return null;
                }
            ).newInstance();
            assertThat( entity.prop().get(), equalTo( "Foo" ) );
            assertThat( entity.ass().get().identity().get(), equalTo( associatedIdentity ) );
            assertThat( entity.manyAss().get( 0 ).identity().get(), equalTo( associatedIdentity ) );
            assertThat( entity.namedAss().get( "foo" ).identity().get(), equalTo( associatedIdentity ) );
            uow.complete();
        }
    }

    public interface SomeEntity
        extends HasIdentity
    {
        Property<String> prop();

        @Optional
        Association<SomeEntity> ass();

        @Optional
        ManyAssociation<SomeEntity> manyAss();

        @Optional
        NamedAssociation<SomeEntity> namedAss();
    }
}
