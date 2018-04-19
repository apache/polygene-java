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
package org.apache.polygene.api.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.apache.polygene.api.common.UseDefaults;
import org.apache.polygene.api.identity.StringIdentity;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.unitofwork.UnitOfWork;
import org.apache.polygene.api.value.ValueDescriptor;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.spi.module.ModuleSpi;
import org.apache.polygene.spi.type.ValueTypeFactory;
import org.apache.polygene.test.AbstractPolygeneTest;
import org.apache.polygene.test.EntityTestAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

public class ValueTypeFactoryTest extends AbstractPolygeneTest
{
    private ValueTypeFactory valueTypeFactory;

    @Override
    public void assemble( ModuleAssembly module )
    {
        module.values( SomeState.class );
        module.entities( SomeState.class );
        new EntityTestAssembler().assemble( module );
    }

    interface SomeState
    {
        @UseDefaults
        Property<List<String>> list();

        @UseDefaults
        Property<Map<String, Integer>> map();
    }

    @BeforeEach
    public void setup()
    {
        valueTypeFactory = ( (ModuleSpi) module.instance() ).valueTypeFactory();
    }

    @Test
    public void plainValues()
    {
        assertThat( valueTypeFactory.valueTypeOf( module, String.class ), equalTo( ValueType.STRING ) );
        assertThat( valueTypeFactory.valueTypeOf( module, "" ), equalTo( ValueType.STRING ) );
    }

    @Test
    public void enums()
    {
        assertThat( valueTypeFactory.valueTypeOf( module, TimeUnit.class ), instanceOf( EnumType.class ) );
        assertThat( valueTypeFactory.valueTypeOf( module, TimeUnit.DAYS ), instanceOf( EnumType.class ) );
    }

    @Test
    public void collections()
    {
        assertThat( valueTypeFactory.valueTypeOf( module, LinkedHashSet.class ),
                    instanceOf( CollectionType.class ) );

        List<String> list = new ArrayList<>();
        ValueType listValueType = valueTypeFactory.valueTypeOf( module, list );
        assertThat( listValueType, instanceOf( CollectionType.class ) );
        assertThat( ( (CollectionType) listValueType ).collectedType(), equalTo( ValueType.OBJECT ) );
    }

    @Test
    public void maps()
    {
        assertThat( valueTypeFactory.valueTypeOf( module, TreeMap.class ), instanceOf( MapType.class ) );

        HashMap<String, Integer> map = new HashMap<>();
        ValueType mapValueType = valueTypeFactory.valueTypeOf( module, map );
        assertThat( mapValueType, instanceOf( MapType.class ) );
        assertThat( ( (MapType) mapValueType ).keyType(), equalTo( ValueType.OBJECT ) );
        assertThat( ( (MapType) mapValueType ).valueType(), equalTo( ValueType.OBJECT ) );
    }

    @Test
    public void valueComposites()
    {
        assertThat( valueTypeFactory.valueTypeOf( module, SomeState.class ),
                    instanceOf( ValueCompositeType.class ) );
        assertThat( valueTypeFactory.valueTypeOf( module, valueBuilderFactory.newValue( SomeState.class ) ),
                    instanceOf( ValueCompositeType.class ) );
    }

    @Test
    public void entityComposites()
    {
        assertThat( valueTypeFactory.valueTypeOf( module, SomeState.class ),
                    instanceOf( StatefulAssociationValueType.class ) );
        try( UnitOfWork uow = unitOfWorkFactory.newUnitOfWork() )
        {
            assertThat(
                valueTypeFactory.valueTypeOf( module, uow.newEntity( SomeState.class, StringIdentity.identityOf( "abc" ) ) ),
                instanceOf( EntityCompositeType.class ) );
        }
    }

    @Test
    public void genericsAreResolvedOnValueCompositeProperties()
    {
        ValueDescriptor descriptor = module.typeLookup().lookupValueModel( SomeState.class );
        assertThat( descriptor.state().findPropertyModelByName( "list" ).valueType(),
                    equalTo( CollectionType.listOf( ValueType.STRING ) ) );
        assertThat( descriptor.state().findPropertyModelByName( "map" ).valueType(),
                    equalTo( MapType.of( ValueType.STRING, ValueType.INTEGER ) ) );
    }
}
