/*
 * Copyright (c) 2015 Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.runtime.entity;

import java.util.Arrays;
import java.util.Collections;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * EntityBuilder With State Test.
 */
public class EntityBuilderWithStateTest
    extends AbstractZestTest
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
        final String associatedIdentity;
        try( UnitOfWork uow = uowf.newUnitOfWork() )
        {
            EntityBuilder<SomeEntity> builder = uow.newEntityBuilder( SomeEntity.class );
            builder.instance().prop().set( "Associated" );
            SomeEntity entity = builder.newInstance();
            associatedIdentity = entity.identity().get();
            uow.complete();
        }
        try( UnitOfWork uow = uowf.newUnitOfWork() )
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
                        return EntityReference.parseEntityReference( associatedIdentity );
                    }
                    return null;
                },
                descriptor -> {
                    if( "manyAss".equals( descriptor.qualifiedName().name() ) )
                    {
                        return Arrays.asList( EntityReference.parseEntityReference( associatedIdentity ) );
                    }
                    return null;
                },
                descriptor -> {
                    if( "namedAss".equals( descriptor.qualifiedName().name() ) )
                    {
                        return Collections.singletonMap(
                            "foo",
                            EntityReference.parseEntityReference( associatedIdentity )
                        );
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
        extends Identity
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
