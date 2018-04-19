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
 */
package org.apache.polygene.spi.entitystore;

import org.apache.polygene.api.entity.EntityBuilder;
import org.apache.polygene.api.identity.Identity;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.serialization.Serialization;
import org.apache.polygene.api.service.qualifier.Tagged;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.usecase.UsecaseBuilder;
import org.apache.polygene.api.value.ValueBuilder;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.Test;

public class Polygene142Test extends AbstractPolygeneTest
{
    @Override
    public void assemble( ModuleAssembly module )
    {
        module.values( Regression142Type.class );
        module.entities( Regression142Type.class );
        new EntityTestAssembler().assemble( module );
    }

    @Service
    @Tagged( Serialization.Format.JSON )
    private Serialization serialization;

    @Test
    public void polygene142RegressionTest()
        throws Exception
    {
        Regression142Type value;
        {
            ValueBuilder<Regression142Type> builder = valueBuilderFactory.newValueBuilder( Regression142Type.class );
            builder.prototype().price().set( 23.45 );
            builder.prototype().testenum().set( Regression142Enum.B );
            value = builder.newInstance();
            String serialized = serialization.serialize( value );
            System.out.println( serialized ); // ok
            value = serialization.deserialize( module, Regression142Type.class, serialized ); // ok
        }
        {
            Identity valueId = StringIdentity.identityOf( "abcdefg" );
            {
                try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "create" ) ) )
                {
                    EntityBuilder<Regression142Type> builder = uow.newEntityBuilder( Regression142Type.class, valueId );
                    builder.instance().price().set( 45.67 );
                    builder.instance().testenum().set( Regression142Enum.A );
                    value = builder.newInstance();
                    System.out.println( value.testenum().get() );
                    uow.complete();
                }
                catch( Exception e_ )
                {
                    e_.printStackTrace();
                }
            }
            {
                try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "read" ) ) )
                {
                    value = uow.get( Regression142Type.class, valueId );
                    System.out.println( value.price().get() );
                    System.out.println( value.testenum().get() ); // FAIL
                }
            }
        }
    }

    private enum Regression142Enum
    {
        A,
        B,
        C,
        D
    }

    interface Regression142Type
    {
        Property<Double> price();

        Property<Regression142Enum> testenum();
    }
}
