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

import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.HasIdentity;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class EntityCompositeToStringTest extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        module.entities( Some.class );
        new EntityTestAssembler().assemble( module );
    }

    @Test
    public void givenEntityWhenToStringExpectStringIdentity()
    {
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            Some some = createSome( uow );
            assertThat( some.toString(), equalTo( some.identity().get().toString() ) );
        }
    }

    @Test
    public void givenEntityWhenPrintStateSystemPropertyAndToStringExpectState()
    {
        String propertyName = "polygene.entity.print.state";
        String previous = System.getProperty( propertyName, null );
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            System.setProperty( propertyName, "true" );
            Some some = createSome( uow );
            assertThat( some.toString(), allOf( containsString( "someString" ), containsString( "foo" ) ) );
        }
        finally
        {
            if( previous != null )
            {
                System.setProperty( propertyName, previous );
            }
            else
            {
                System.clearProperty( propertyName );
            }
        }
    }

    private Some createSome( UnitOfWork uow )
    {
        EntityBuilder<Some> builder = uow.newEntityBuilder( Some.class );
        builder.instance().someString().set( "foo" );
        return builder.newInstance();
    }

    interface Some extends HasIdentity
    {
        Property<String> someString();
    }
}
