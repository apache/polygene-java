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
package org.apache.zest.regression.qi94;

import org.junit.Test;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.junit.Assert.assertEquals;

public class IssueTest
    extends AbstractZestTest
{
    @SuppressWarnings( "unchecked" )
    public void assemble( ModuleAssembly aModule )
        throws AssemblyException
    {
        aModule.entities( Item.class, ItemType.class );
        new EntityTestAssembler().assemble( aModule );
    }

    @Test
    public void entityBuilderAssociationTypeIsNotNull()
    {
        UnitOfWork uow = uowf.newUnitOfWork();
        try
        {
            EntityBuilder<Item> builder = uow.newEntityBuilder( Item.class );
            assertEquals( ItemType.class, zest.api()
                .entityDescriptorFor( builder.instance() )
                .state()
                .getAssociationByName( "typeOfItem" )
                .type() );
        }
        finally
        {
            uow.discard();
        }
    }

    interface Item
        extends EntityComposite
    {
        Association<ItemType> typeOfItem();
    }

    interface ItemType
        extends EntityComposite
    {
        Property<String> name();
    }
}
