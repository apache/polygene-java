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

package org.apache.zest.runtime.association;

import org.apache.zest.api.association.Association;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.spi.uuid.UuidIdentityGeneratorService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class AssociationAssignmentTest extends AbstractZestTest
{

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( MemoryEntityStoreService.class );
        module.services( UuidIdentityGeneratorService.class );
        module.services( OrgJsonValueSerializationService.class )
            .taggedWith( ValueSerialization.Formats.JSON );
        module.entities( TheAssociatedType.class );
        module.entities( TheMainType.class );
    }

    @Test
    public void givenAssignmentOfAssociationAtCreationWhenDereferencingAssocationExpectCorrectValue()
        throws Exception
    {
        UnitOfWork work = uowf.newUnitOfWork();
        TheAssociatedType entity1 = work.newEntity( TheAssociatedType.class );
        EntityBuilder<TheMainType> builder = work.newEntityBuilder( TheMainType.class );
        builder.instance().assoc().set( entity1 );
        TheMainType entity2 = builder.newInstance();
        String id1 = entity1.identity().get();
        String id2 = entity2.identity().get();
        work.complete();
        assertThat(id1, notNullValue());
        assertThat(id2, notNullValue());

        work = uowf.newUnitOfWork();
        TheMainType entity3 = work.get(TheMainType.class, id2 );
        TheAssociatedType entity4 = entity3.assoc().get();
        assertThat( entity4.identity().get(), equalTo(id1));
        work.discard();
    }

    public interface TheAssociatedType extends EntityComposite
    {
    }

    public interface TheMainType extends EntityComposite
    {
        Association<TheAssociatedType> assoc();
    }
}
