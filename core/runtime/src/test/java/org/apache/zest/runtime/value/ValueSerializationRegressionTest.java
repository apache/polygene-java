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
package org.apache.zest.runtime.value;

import org.junit.Test;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.entity.Identity;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.value.ValueBuilder;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;

public class ValueSerializationRegressionTest extends AbstractZestTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.entities( SimpleEntity.class );
        module.entities( DualFaced.class );
        module.values( DualFaced.class );
        module.services( MemoryEntityStoreService.class );
        module.services( UuidIdentityGeneratorService.class );
        module.services( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
    }

    @Test
    public void givenNewValueWhenConvertingToEntityExpectNewEntityInStore()
        throws UnitOfWorkCompletionException
    {
        ValueBuilder<DualFaced> builder = valueBuilderFactory.newValueBuilder( DualFaced.class );
        builder.prototype().identity().set( "1234" );
        builder.prototype().name().set( "Hedhman" );
        DualFaced value = builder.newInstance();
    }

    public interface SimpleEntity extends Identity
    {
        Property<String> name();
    }

    public interface DualFaced extends Identity
    {
        Property<String> name();

        Association<SimpleEntity> simple();

        ManyAssociation<SimpleEntity> simples();

        NamedAssociation<SimpleEntity> namedSimples();
    }
}

